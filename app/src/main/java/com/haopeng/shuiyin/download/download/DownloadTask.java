package com.haopeng.shuiyin.download.download;


import java.io.File;

/**
 * @author lizhen
 * @date 2015-8-13 下午2:37:24
 */
public class DownloadTask {
	/** 唯一标识 */
	public long id;
	/** 网络地址 */
	public String url;
	/** 本地路径 */
	public String path;
	/** 下载进度 */
	public float progress;
	
	/** 最后下载位置 */
	public long endPostion;
	
	/** 下载状态 */
	public int status;

	/** 异常 */
	public Exception exception;
	
	public static final int STATUS_DOWNLOADING = 0x0101;
	public static final int STATUS_DOWNLOAD_WAITING = STATUS_DOWNLOADING + 1;
	public static final int STATUS_DOWNLOAD_PAUSED = STATUS_DOWNLOAD_WAITING + 1;
	public static final int STATUS_DOWNLOAD_FAILED = STATUS_DOWNLOAD_PAUSED + 1;
	public static final int STATUS_DOWNLOADED = STATUS_DOWNLOAD_FAILED + 1;
	
	public DownloadTask(long id,String path,String url){
		this.id = id;
		this.url = url;
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		this.path = path + File.separator + id+".mp4";
		this.status = STATUS_DOWNLOAD_WAITING;
	}
	
	
}
