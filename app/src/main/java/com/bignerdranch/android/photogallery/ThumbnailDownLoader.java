package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 为ImageView提供异步下载图片的后台线程类
 * Created by SHENGFQ on 2016/2/16.
 * 为什么要设置泛型参数Token为ImageView？？
 */
public class ThumbnailDownLoader<Token> extends HandlerThread {
    private static final String TAG="ThumbnailDownLoader";
    private static final int MESSAGE_DOWNLOAD=0;
    private Handler mHandler;
    Map<Token,String> requestMap= Collections.synchronizedMap(new HashMap<Token,String>());
    /**
     * 主线程中的Handler对象
     * */
    Handler mRespoonseHandler;
    Listener<Token> mListener;
    
    /**
     * 用于通信的监听器接口
     * 当下载完要执行的事情：将图片加载到UI线程的ImageView
     * */
    public interface Listener<Token>{
    	/**
    	 * 后台线程的输出,将下载的图片指定给前台的ImageView
    	 * @param token 存放图片的容器
    	 * @param thumbnail 图片格式
    	 * */
        void onThumbnailDownloaded(Token token,Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener){
        mListener=listener;
    }

    public ThumbnailDownLoader(){
        super(TAG);
    }

    /**
     * 主线程传递的Handler
     * */
    public ThumbnailDownLoader(Handler responseHandler){
        super(TAG);
        mRespoonseHandler=responseHandler;
    }
/**
 * 该方法的调用发生在Looper第一次检查消息队列之前
 * */

 @SuppressLint("handlerLeak")
 @Override
 protected void onLooperPrepared(){
    mHandler=new Handler(){
    //looper取得消息队列中的特定消息,回调方法根据消息what属性进行处理
      public void handleMessage(Message msg){
        if(msg.what==MESSAGE_DOWNLOAD){
        	@SuppressWarnings("unchecked")
            Token token=(Token)msg.obj;//Handler.obtainMessage(msg,obj);通过Handler发送Message传递了message.obj,这里处理消息时,可以获取obj,属于约定内容。
            Log.i(TAG,"Got a request for url:"+requestMap.get(token));
            handleRequest(token);
        }
      }
    };
 }
 /**
  * 发送message请求下载图片,将URL和Token传递到同步hashMap中
  * 在调用getView()的时候请求下载
  * @param token 前台交互的UI控件
  * @param url 前台指定的下载地址
  * */
    public void queueThumbnail(Token token,String url){
        Log.i(TAG,"Got to URL:"+url);
        requestMap.put(token,url);//调用getview()的时候调用
        Message message=mHandler.obtainMessage(MESSAGE_DOWNLOAD,token);//创建信息并传入消息字段,自动完成目标handler的设置
        message.sendToTarget();//将消息压入消息队列
    }

    /**
     * 远程下载图片，将后台下载的图片加载到前台UI的ImageView中
     * @param token 泛型参数，这里指前台的ImageView
     * */
    private void handleRequest(final Token token){
        try{
            final String url=requestMap.get(token);
            if(url==null)
            return;
            byte[] bitmapBytes=new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap created");
            //此处定义了主线程在后台线程交互操作的UI处理
            mRespoonseHandler.post(new Runnable(){
                public void run(){
                    if(requestMap.get(token)!=url) return;
                    requestMap.remove(token);//
                    mListener.onThumbnailDownloaded(token,bitmap);
                }
            });
            
        }catch(IOException ioe){
            Log.e(TAG,"Error downloading image",ioe);
        }
    }

    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
    
}
