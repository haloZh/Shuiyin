package com.haopeng.shuiyin.download.download;

public interface DownloadListener {
	void onSuccess(DownloadTask task);
	void onFailure(DownloadTask task);
	void onPause(DownloadTask task);
	void onProgress(float progress);
}
