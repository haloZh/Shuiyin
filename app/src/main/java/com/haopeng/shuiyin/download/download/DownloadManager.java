package com.haopeng.shuiyin.download.download;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.haopeng.shuiyin.utils.ExecutorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 故事下载管理器，单例
 * @author lizhen
 * @date 2015-8-13 下午2:42:37
 */
public class DownloadManager {
	private static DownloadManager mInstance;
	/** 同步锁对象 */
	private Object obj;
	/** 下载任务列表 */
	private static List<DownloadTask> tasks;
	/** 单线程下载 */
	private static DownloadThread thread;
	/** 下载回调*/
	private static DownloadListener listener;
	private Handler handler;
	/** 故事广播Action */
	public static final String ACTION = "com.sogou.upd.x1.story.download";
	
	public static DownloadManager getInstance(){
		if(mInstance == null){
			mInstance = new DownloadManager();
			init();
		}
		return mInstance;
	}
	
	/**
	 * 故事初始化数据，读取db，初始化下载任务列表
	 */
	private static void init(){
	}
	
	/**
	 * 启动下载管理器
	 */
	public void start(){
		if(obj == null){
			obj = new Object();
		}
		if(handler == null){
			handler = new Handler();
		}
		if(thread == null || thread.stop){
			Log.e("DownloadManager", "create DownloadThread");
			thread = new DownloadThread();
		}
		if(!thread.isAlive()){
			thread.start();
		}
	}
	
	/**
	 * 停止下载管理器
	 */
	public static void stop(){
		if(thread != null){
			thread.stopThread();
			thread = null;
		}
	}
	
	/**
	 * 添加下载任务
	 */
	public synchronized void addTask(DownloadTask task,DownloadListener listener){
		if(tasks == null){
			tasks = new ArrayList<DownloadTask>();
		}
		if(task != null && task.id > 0){
			tasks.add(task);
		}
		this.listener = listener;

		start();
		synchronized (obj) {
			obj.notify();
		}
	}
	public synchronized void progress(float progress){
		listener.onProgress(progress);
	}
	public synchronized void completed(final DownloadTask task){
		task.status = DownloadTask.STATUS_DOWNLOADED;
		listener.onSuccess(task);
//		sendBroadcast();
	}
	
	public synchronized void paused(DownloadTask task){
		task.status = DownloadTask.STATUS_DOWNLOAD_PAUSED;
		listener.onPause(task);
//		sendBroadcast();
	}
	
	public synchronized void failed(final DownloadTask task){

		task.status = DownloadTask.STATUS_DOWNLOAD_FAILED;
		listener.onFailure(task);
//		sendBroadcast();
	}
	
	public synchronized List<DownloadTask> getTasks(){
		return tasks;
	}
	
	/**
	 * 获取一个等待下载状态的任务
	 */
	public synchronized DownloadTask getWaitTask(){
		DownloadTask result = null;
		if(tasks != null ){
			for(DownloadTask item : tasks){
				if(item.status == DownloadTask.STATUS_DOWNLOAD_WAITING || item.status == DownloadTask.STATUS_DOWNLOADING){
					result = item;
					result.status = DownloadTask.STATUS_DOWNLOADING;
//					sendBroadcast();
					break;
				}
			}
		}
		return result;
	}
	

	
	/**
	 * 0:不显示，1：显示下载全部，2：显示暂停所有
	 */
	public synchronized int getDownloadStatus(){
		int result = 0;
		if(tasks != null){
			for(DownloadTask task : tasks){
				if(task.status == DownloadTask.STATUS_DOWNLOADING){
					result=2;
					break;
				}
				if(task.status == DownloadTask.STATUS_DOWNLOAD_PAUSED || task.status == DownloadTask.STATUS_DOWNLOAD_FAILED){
					result=1;
				}
			}
		}
		return result;
	}
	
	/**
	 * 暂停所有
	 */
	public synchronized void pauseAll(){
		stop();
		if(tasks != null){
			for(DownloadTask task : tasks){
				if(task.status == DownloadTask.STATUS_DOWNLOADING || task.status == DownloadTask.STATUS_DOWNLOAD_WAITING){
					task.status = DownloadTask.STATUS_DOWNLOAD_PAUSED;
				}
			}
		}
	}
	

	/**
	 * 开启下载全部
	 */
	public synchronized boolean startAll(){
		boolean result = false;
		if(tasks ==null){
			return result;
		}
		for(DownloadTask task : tasks){
			if(task.status == DownloadTask.STATUS_DOWNLOAD_PAUSED || task.status == DownloadTask.STATUS_DOWNLOAD_FAILED){
				task.status = DownloadTask.STATUS_DOWNLOAD_WAITING;
			}
		}
		start();
		return result;
	}
	
	/**
	 * 一键清空
	 */
	public synchronized void deleteAll(){
		if(tasks != null){
			deleteLocalFile(tasks);
			tasks.clear();
			tasks = null;
		}
	}
	

	
	/**
	 * 下载线程是否处于工作状态，即是否正在下载
	 */
	public boolean isWork(){
		boolean result = false;
		if(thread != null){
			result = thread.isWork();
		}
		return result;
	}
	
	public static void destory(){
		stop();
		if(tasks != null){
			tasks.clear();
			tasks = null;
		}
		mInstance= null;
	}
	
	private void deleteLocalFile(List<DownloadTask> list){
		final List<String> array = new ArrayList<String>();
		for(DownloadTask task:list){
			array.add(task.path);
		}
		ExecutorUtils.excute(new Runnable() {
			
			@Override
			public void run() {
				for(String str : array){
				File file = new File(str);
				if(file.exists()){
					file.delete();
				}
				}
			}
		});
	}
}
