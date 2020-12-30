package com.haopeng.shuiyin.download.download;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread {
    public boolean stop = false;
    public boolean next = false;
    private DownloadTask task;

    public DownloadThread() {
    }

    @Override
    public void run() {
        while (!stop) {
            task = DownloadManager.getInstance().getWaitTask();
            next = false;
            if (task != null) {
                downloading();
            } else {
                stopThread();
            }
        }
        Log.e("DownloadThread", "run stop");
    }

    private void downloading() {
        getFileFromServer(task.url, task.path);
    }

    public void stopThread() {
        stop = true;
        interrupt();
    }

    public void next() {
        next = true;
    }

    // 服务器端获取信息
    public void getFileFromServer(String path, String fileName) {
        // 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
        BufferedInputStream bis;
        InputStream is;
        HttpURLConnection conn;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Range", "bytes=" + task.endPostion + "-");
            // 自带获取到文件的大小
            long max = conn.getContentLength();
            is = conn.getInputStream();
            // 新建一个文件路径以及保存的软件名称
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            bis = new BufferedInputStream(is);
            file.seek(task.endPostion);
            byte[] buffer = new byte[2048];
            int len = 0;
            int count = 0;
            Log.i("progress start", task.endPostion + " / " + max);
            while (!stop && (len = bis.read(buffer)) != -1) {
                file.write(buffer, 0, len);
                task.endPostion += len;
                count++;
                if (count % 100 == 0) {
                    setProgress(task.endPostion, max);
                }
                if (next) {
                    break;
                }
            }
            if (file != null) {
                file.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (is != null) {
                is.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
            if (len == -1) {
                DownloadManager.getInstance().completed(task);
            } else {
                DownloadManager.getInstance().paused(task);
            }
        } catch (IOException e) {
            task.endPostion = 0;
            task.exception = e;
            DownloadManager.getInstance().failed(task);
        } catch (Exception e) {
            task.endPostion = 0;
            task.exception = e;
            DownloadManager.getInstance().failed(task);
        }
    }

    public void setProgress(long value, long max) {
        float progress = (float) value / (float)max * 100;
//        Log.i("progress", "value: " + value + "max: " + max + "progress: " + progress);
        DownloadManager.getInstance().progress(progress);
    }

    public boolean isWork() {
        if (!isAlive() && stop) {
            return false;
        } else {
            return true;
        }
    }
}
