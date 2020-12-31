package com.haopeng.shuiyin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaScannerConnection;
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

import com.haopeng.shuiyin.http.Converter.ResponseConverFactory;
import com.haopeng.shuiyin.http.bean.ResponseBean;
import com.haopeng.shuiyin.download.download.DownloadListener;
import com.haopeng.shuiyin.download.download.DownloadManager;
import com.haopeng.shuiyin.download.download.DownloadTask;
import com.haopeng.shuiyin.http.entity.HttpData;
import com.haopeng.shuiyin.http.retrofit.HttpService;
import com.haopeng.shuiyin.utils.AlbumNofityUtils;
import com.haopeng.shuiyin.utils.LoggingInterceptor;
import com.haopeng.shuiyin.utils.PermissionsUtils;
import com.haopeng.shuiyin.utils.StringUtils;

import java.io.File;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Context mContext;
    private Button btnGetUrl, btnPrase, btnDownload, btnClean;
    private EditText edtUrl;
    private TextView tvUrl;
    private View viewLayer;
    private ProgressBar loading, loadingH;

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
                    String url = (String)msg.obj;
                    if (url != null) {
                        if (!StringUtils.isHttpUrl(url)) {
                            Toast.makeText(getApplicationContext(), "请输入有效的地址!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "解析成功！", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "异常错误！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_DOWN_SUC:

                    String videoPath = (String) msg.obj;
                    if (videoPath != null) {
                        loadingH.setProgress(100);
                        Toast.makeText(getApplicationContext(), "视频已保存到" + videoPath + "", Toast.LENGTH_LONG).show();
                        AlbumNofityUtils.insertVideoToMediaStore(mContext, videoPath, System.currentTimeMillis(), 0, 0, 0);
                    } else {
                        loadingH.setProgress(0);
                        Toast.makeText(getApplicationContext(), "下载失败!", Toast.LENGTH_SHORT).show();
                    }
                    loadingH.setVisibility(GONE);
                    viewLayer.setVisibility(GONE);
                    loadingH.setProgress(0);
                    break;
                case MSG_DOWN_PRO:
                    int progress = (int) msg.obj;
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
        PATH_VIDEO = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Shuiyin" + File.separator;
        Log.i(TAG, "path: " + PATH_VIDEO);

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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_prase://粘贴
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    String pasteData = (String) item.getText();
                    if (StringUtils.isValid(pasteData)) {
                        edtUrl.setText(pasteData);
                        Log.i(TAG, pasteData);
                    } else {
                        Toast.makeText(getApplicationContext(), "还没有可粘贴的连接哦~", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "还没有可粘贴的连接哦~", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_get_url://解析
                loading.setVisibility(VISIBLE);
                viewLayer.setVisibility(VISIBLE);
                if (!StringUtils.isValid(edtUrl.getText().toString()) && StringUtils.isHttpUrl(targetUrl)) {
                    Toast.makeText(getApplicationContext(), "地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                analysisUrl(edtUrl.getText().toString());
                break;
            case R.id.btn_download:
                if (!StringUtils.isValid(targetUrl) || !StringUtils.isHttpUrl(targetUrl)) {
                    Toast.makeText(getApplicationContext(), "需要对链接进行解析才可以下载哦~", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadingH.setVisibility(VISIBLE);
                viewLayer.setVisibility(VISIBLE);
                Message msg = Message.obtain();
                msg.what = MSG_DOWN_SUC;

                long id = System.currentTimeMillis();
                String videoPath = PATH_VIDEO + id + SUFFIX;
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
                        msgPro.obj = (int) progress;
                        Log.i(TAG, "onProgress: " + progress);
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


    //解析地址
    private void analysisUrl(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://kyzhiban.cn/")
                .addConverterFactory(ResponseConverFactory.create())
                .client(okHttpClient)
                .build();

        HttpService service = retrofit.create(HttpService.class);
        Map params = new TreeMap<String, String>();
        params.put("url", url);
        Log.i(TAG,"url: " + url);
        service.getTargetUrl(params).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Message message = Message.obtain();
                message.what = MSG_URL_BACK;
                if (response.isSuccessful()) {
                    String json = response.body().toString();
                    Log.i(TAG,"json: "+ json);
//                    ResponseBean bean = response.body().getData();
                    targetUrl = response.body().toString();
                    Log.i(TAG, "target url: " + targetUrl);
                    message.obj = targetUrl;
                    mHandler.sendMessage(message);
                } else {
                    mHandler.sendMessage(message);
//                    throw new IOException("Unexpected code " + response);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Message message = Message.obtain();
                message.what = MSG_URL_BACK;
                mHandler.sendMessage(message);
            }
        });
//        okHttpClient.newCall(request).enqueue(new Callback() {
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                ResponseBody responseBody = response.body();
//                Message message = Message.obtain();
//                message.what = MSG_URL_BACK;
//                if (response.isSuccessful()) {
//                    String json = response.toString();
//                    Log.i(TAG,"json: "+ json);
//                    Gson gson = new Gson();
//                    ResponseBean bean = gson.fromJson(json, ResponseBean.class);
//                    targetUrl = bean.getUrl();
//                    Log.i(TAG, "target url: " + targetUrl);
//                    message.obj = targetUrl;
//                    message.what = MSG_URL_BACK;
//                    mHandler.sendMessage(message);
//                } else {
//                    mHandler.sendMessage(message);
////                    throw new IOException("Unexpected code " + response);
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                Message message = Message.obtain();
//                message.what = MSG_URL_BACK;
//                mHandler.sendMessage(message);
//            }
//        });
    }
}