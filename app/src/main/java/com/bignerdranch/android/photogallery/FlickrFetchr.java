/*
 * Name:FlickrFetchr.java
 * Desc:处理网络连接专用类
 * CreateDate:2015-05-07
 * Version:01
 * 
 * */
package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * 向图片分享网站请求数据源XML
 * */
public class FlickrFetchr {
public static final String TAG="FlickrFetchr";

public static final String PREF_SEARCH_QUERY="searchQuery";//持久化查询字符串存储的键
public static final String PREF_LAST_RESULT_ID="lastResultId";//保存最后一次的搜索结果,chapter29
private static final String  ENDPOINT="http://api.flickr.com/services/rest/";
private static final String API_KEY="2df474a7506edc160ed63f3acb5e7d85";
private static final String METHOD_GET_RECENT="flickr.photos.getRecent";
private static final String METHOD_GET_SEARCH="flickr.photos.search";
private static final String PARAM_EXTRAS="extras";
private static final String PARAM_TEXT="text";
private static final String EXTRA_SMALL_URL="url_s";
private static final String XML_PHOTO="photo";
	/**
	 * 从URL字符串远程读取数据到字节数组中
	 * */
	public byte[] getUrlBytes(String urlSpec) throws IOException{
		URL url=new URL(urlSpec);
		HttpURLConnection connection=(HttpURLConnection)url.openConnection();
		try{
			InputStream in=connection.getInputStream();
			ByteArrayOutputStream out=new ByteArrayOutputStream();			
			if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
				return null;
			}
			int bytesRead=0;
			byte[] buffer=new byte[1024];
			while ((bytesRead=in.read(buffer))>0) {
				out.write(buffer,0,bytesRead);
				
			}
			out.close();
			return out.toByteArray();
		}finally{
			connection.disconnect();
		}
	}
	
	/**
	 * URL could be disabled usually,so we must declaring it IOException
	 * */
	public String getUrl(String urlSpec) throws IOException{
		return new String(getUrlBytes(urlSpec));
	}
	/**
	 * 根据一个URL
	 * */
	public ArrayList<GalleryItem> downLoadGalleryItem(String url){
		ArrayList<GalleryItem> items=new ArrayList<GalleryItem>();		
		try{
			String xmlString=getUrl(url);
			Log.i(TAG, "" + xmlString);
			XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
			XmlPullParser parser=factory.newPullParser();
			parser.setInput(new StringReader(xmlString));
			parseItem(items,parser);
		}catch(IOException ex){
		Log.e(TAG,"failed to fetch items",ex);
		}catch(XmlPullParserException xppe){
			Log.e(TAG,"Failed to parse items",xppe);
		}
		return items;	
	
	}

	/**
	 * 构建访问URL,获取返回的XML数据
	 * */
	 public ArrayList<GalleryItem> fetchItems(){	 	
	 	//构建一个远程链接服务器URL	
		String url=	Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter("method",METHOD_GET_RECENT)
	 	.appendQueryParameter("api_key",API_KEY)
	 	.appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL)
	 	.build().toString();
		return downLoadGalleryItem(url);
	 }
	 
	 public ArrayList<GalleryItem> search(String query){
		 String url=Uri.parse(ENDPOINT).buildUpon().appendQueryParameter("method", METHOD_GET_SEARCH)
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
				.appendQueryParameter(PARAM_TEXT,query).build().toString();
		 return downLoadGalleryItem(url);
		 
		 
	 }

	 /**
	  * 从XmlPullParser的XML数据流中解析出需要的图片对象保存到ArrayList集合中
	  * @param items 传递保存的对象集合引用
	  * @param parser xml数据流
	  * */
	 public void parseItem(ArrayList<GalleryItem> items,XmlPullParser parser) throws XmlPullParserException,IOException{
	 	int eventType=parser.next();
	 	while(eventType!=XmlPullParser.END_DOCUMENT){
	 		if(eventType==XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){
	 			String id=parser.getAttributeValue(null,"id");
	 			String caption=parser.getAttributeValue(null,"title");
	 			String smallUrl=parser.getAttributeValue(null,EXTRA_SMALL_URL);
	 			String owner=parser.getAttributeValue(null,"owner");
	 			GalleryItem item=new GalleryItem();
	 			item.setmId(id);
	 			item.setmUrl(smallUrl);
	 			item.setmCaption(caption);
	 			item.setOwner(owner);
	 			items.add(item);
	 			
			}
			eventType=parser.next();
		}
	 }
}
