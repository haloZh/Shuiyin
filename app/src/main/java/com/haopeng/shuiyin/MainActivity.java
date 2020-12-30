package com.haopeng.shuiyin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.haopeng.shuiyin.download.download.DownloadListener;
import com.haopeng.shuiyin.download.download.DownloadManager;
import com.haopeng.shuiyin.download.download.DownloadTask;
import com.haopeng.shuiyin.utils.AlbumNofityUtils;
import com.haopeng.shuiyin.utils.ExecutorUtils;
import com.haopeng.shuiyin.utils.PermissionsUtils;
import com.haopeng.shuiyin.utils.StringUtils;

import java.io.File;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Context mContext;
    private Button btnGetUrl, btnPrase, btnDownload, btnClean;
    private EditText edtUrl;
    private TextView tvUrl;
    private View viewLayer;
    private ProgressBar loading,loadingH;

    private String targetUrl;

    private Runnable runnable;

    private static final int MSG_URL_BACK = 101;
    private static final int MSG_DOWN_SUC = 102;
    private static final int MSG_DOWN_PRO = 103;
    private static String PATH_VIDEO;
    private static final String SUFFIX = ".mp4";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_URL_BACK:
                    loading.setVisibility(GONE);
                    viewLayer.setVisibility(GONE);
                    String url = (String) msg.obj;
                    if (!StringUtils.isHttpUrl(url)) {
                        Toast.makeText(getApplicationContext(), "请输入有效的地址!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "解析成功！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_DOWN_SUC:

                    String videoPath = (String)msg.obj;
                    if(videoPath != null) {
                        loadingH.setProgress(100);
                        Toast.makeText(getApplicationContext(), "视频已保存到" + videoPath +"", Toast.LENGTH_LONG).show();
                        AlbumNofityUtils.insertVideoToMediaStore(mContext,videoPath,System.currentTimeMillis(),0,0,0);
                    }else{
                        loadingH.setProgress(0);
                        Toast.makeText(getApplicationContext(), "下载失败!", Toast.LENGTH_SHORT).show();
                    }
                    loadingH.setVisibility(GONE);
                    viewLayer.setVisibility(GONE);
                    loadingH.setProgress(0);
                    break;
                case MSG_DOWN_PRO:
                    int progress = (int)msg.obj;
                    loadingH.setProgress(progress);
                    break;
            }
        }
    };


    private MediaScannerConnection mMediaScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        PermissionsUtils.getInstance().checkPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, new PermissionsUtils.IPermissionsResult() {

            @Override
            public void passPermissions() {
                Log.i(TAG, "passPermissions");
            }

            @Override
            public void forbidPermissions() {
                Log.i(TAG, "forbidPermissions");
            }
        });
        PATH_VIDEO =  Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "Shuiyin" + File.separator;
        Log.i(TAG,"path: "+PATH_VIDEO);
        initPython();

        initView();
    }

    private void initView() {
        btnGetUrl = findViewById(R.id.btn_get_url);
        btnPrase = findViewById(R.id.btn_prase);
        btnDownload = findViewById(R.id.btn_download);
        btnClean = findViewById(R.id.btn_clear);
        edtUrl = findViewById(R.id.edt_url);
        tvUrl = findViewById(R.id.tv_url);
        viewLayer = findViewById(R.id.v_layer);
        loading = findViewById(R.id.pb);
        loadingH = findViewById(R.id.pb_h);

        btnGetUrl.setOnClickListener(this);
        btnPrase.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        btnClean.setOnClickListener(this);
        viewLayer.setOnClickListener(this);

    }

    // 初始化Python环境
    void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }

    /***
     * 调用python代码
     * 参考https://blog.csdn.net/wwb1990/article/details/104051068
     * @param originUrl
     */
    private void callPythonCode(String originUrl) {
        Python py = Python.getInstance();
        PyObject pobj =
                py.getModule("douyin").callAttr("main", originUrl);
        targetUrl = pobj.toJava(String.class);
        Log.i(TAG, "下载地址：" + targetUrl);

        Message msg = Message.obtain();
        msg.what = MSG_URL_BACK;
        msg.obj = targetUrl;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_prase://粘贴
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = clipboard.getPrimaryClip();
                if(clipData != null && clipData.getItemCount()>0) {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    String pasteData = (String) item.getText();
                    if (StringUtils.isValid(pasteData)) {
                        edtUrl.setText(pasteData);
                        Log.i(TAG, pasteData);
                    } else {
                        Toast.makeText(getApplicationContext(), "还没有可粘贴的连接哦~", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "还没有可粘贴的连接哦~", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_get_url://解析
                loading.setVisibility(VISIBLE);
                viewLayer.setVisibility(VISIBLE);
                if (!StringUtils.isValid(edtUrl.getText().toString())  && StringUtils.isHttpUrl(targetUrl)) {
                    Toast.makeText(getApplicationContext(), "地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                ExecutorUtils.excute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            callPythonCode(edtUrl.getText().toString());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
                break;
            case R.id.btn_download:
                if(!StringUtils.isValid(targetUrl) || !StringUtils.isHttpUrl(targetUrl)){
                    Toast.makeText(getApplicationContext(),"需要对链接进行解析才可以下载哦~",Toast.LENGTH_SHORT).show();
                    return;
                }

                loadingH.setVisibility(VISIBLE);
                viewLayer.setVisibility(VISIBLE);
                Message msg = Message.obtain();
                msg.what = MSG_DOWN_SUC;

                long id = System.currentTimeMillis();
                String videoPath = PATH_VIDEO + id +SUFFIX;
                DownloadTask task = new DownloadTask(id, PATH_VIDEO, targetUrl);
                DownloadManager.getInstance().addTask(task, new DownloadListener() {
                    @Override
                    public void onSuccess(DownloadTask t) {
                        Log.i(TAG, "onSuccess");
                        msg.obj = videoPath;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(DownloadTask t) {
                        Log.i(TAG, "onFailure: " + t.exception.getMessage());
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onPause(DownloadTask t) {
                        Log.i(TAG, "onPause");
                    }

                    @Override
                    public void onProgress(float progress) {

                        Message msgPro = Message.obtain();
                        msgPro.what = MSG_DOWN_PRO;
                        msgPro.obj = (int)progress;
                        Log.i(TAG, "onProgress: "+progress);
                        mHandler.sendMessage(msgPro);
                    }
                });

                break;
            case R.id.btn_clear:
                edtUrl.setText("");
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        DownloadManager.getInstance().pauseAll();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}