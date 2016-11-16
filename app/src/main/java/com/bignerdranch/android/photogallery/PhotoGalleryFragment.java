package com.bignerdranch.android.photogallery;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";
    GridView mGridView;
    ArrayList<GalleryItem> mItems;
    ThumbnailDownLoader<ImageView> mThumbnailThread;//


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);//TODO 设置为保留fragment的作用？
        setHasOptionsMenu(true);//TODO 设置为有选项菜单
        //下载图片墙列表数据
        updateItems();
        //启动服务
        //方式1
       // Intent i=new Intent(getActivity(),PollService.class);
      //  getActivity().startService(i);
     //方式2
       // PollService.setServiceAlarm(getActivity(),true);
        //初始化后台线程下载图片
        mThumbnailThread = new ThumbnailDownLoader<ImageView>(new Handler());//与主线程Looper绑定的Handler
        mThumbnailThread.setListener(new ThumbnailDownLoader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                	//给ImageView设置图片Bitmap
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");
    }
    
    /**
     * 支持搜索更新
     * */
    public void updateItems(){
    	new FetchItemsTask().execute();
    	
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> gridView, View view, int pos, long id) {
                GalleryItem item = mItems.get(pos);
                String url = item.getPhotoUrl();
                Uri photoUrl = Uri.parse(url);
                //Intent intent=new Intent(Intent.ACTION_VIEW,photoUrl);
                Intent intent = new Intent(getActivity(), PhotoPageActivity.class);
                intent.setData(photoUrl);//Intent.setData(Uri); TODO why?
                startActivity(intent);
            }
        });
        setupAdapter();
        //chapter 31 点击图片打开图片url
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GalleryItem item= mItems.get(position);
                Uri photoPageUri=Uri.parse(item.getPhotoUrl());
               //隐式intent方式用浏览器打开
               // Intent i=new Intent(Intent.ACTION_VIEW,photoPageUri);
                //显示用webview加载
                Intent i=new Intent(getActivity(),PhotoPageActivity.class);
                i.setData(photoPageUri);
                startActivity(i);
            }
        });
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.quit();

        Log.i(TAG, "Background thread destroyed");
    }
    
    /**
     * 选项菜单UI 
     * */
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.fragment_photo_gallery, menu);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            MenuItem searchItem=menu.findItem(R.id.menu_item_search);
            SearchView searchView=(SearchView)searchItem.getActionView();
            SearchManager searchManager=(SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
            ComponentName name=getActivity().getComponentName();
            SearchableInfo searchableInfo=searchManager.getSearchableInfo(name);
            searchView.setSearchableInfo(searchableInfo);

        }
    }
    
    /**
     * TODO OptionItem selected
     * api lower 3.0 could be work well,but higher 3.0
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();//register show search
                //getActivity().getIntent().putExtra("buy", "yes");

                //getActivity().startSearch("android", true, null, false);
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit().putString(FlickrFetchr.PREF_SEARCH_QUERY, null).commit();
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), !shouldStartAlarm);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
                    getActivity().invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * api lower 11 could be work well,but higher 11
     * */
    @Override
    public void onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);

        MenuItem toggleItem=menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }
    }
    /**
     * 当竖屏转横屏时，UI会销毁，但是下载器还在挂起，如果点击可能会导致异常
     * 清除队列外地所有请求
     * */
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }
    
    void setupAdapter() {
        if (getActivity() == null || mGridView == null) return;
        if (mItems != null) {
            //  mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),android.R.layout.simple_gallery_item,mItems));
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }

    class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {
        //should be override 3 methods
        //AsyncTask<Params,Progress,Result>
        /*protected Void doInBackground(Void... params){
            try{
				String result=new FlickrFetchr().getUrl("http://www.baidu.com");
				Log.i(TAG, "Fetched contents of URL:"+result);
			}catch(IOException ioe){
				Log.e(TAG,"failed to fetch to Featch UIRL:",ioe);
			}
			return null;
		}*/
    	/**
    	 * 给gridview指定数据源
    	 * */
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }

        /**
         * 后台下载XML数据集
         * **/
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
        //	String query="android";
        	Activity activity=getActivity();
        	if(activity==null)
        		return new ArrayList<GalleryItem>();
        	String query=PreferenceManager.getDefaultSharedPreferences(activity)
        			.getString(FlickrFetchr.PREF_SEARCH_QUERY,null);
        	if(query!=null){
        		//如果有查询字符串,则进行网站查找
        		return new FlickrFetchr().search(query);
        	}
        	//没有查询字符串,则直接获取列表
            return new FlickrFetchr().fetchItems();

        }
    }

    /**
     * 
     * 定制化可以显示图片的ArrayAdapter**/
    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
    /*	public int getCount(){
    		return 0;
    	}

    	public int getItemId(){
    		return 0;
    	}

    	public int getItemViewType(){
    		return 0;
    	}

    	public int getViewTypeCount(){
    		return 0;
    	}

    	*/
    	/**
    	 * 使用构造函数的方式指定数据源
    	 * */
        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
        	//ArrayAdapter使用默认构造函数指定数据源的方式如下,这也是通用的方式,亮点
            super(getActivity(), 0, items);
        }

        /**
         * 每次加载ImageView都会调用的方法
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
            }
            ImageView imageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brian_up_close);
            //
            GalleryItem item = getItem(position);//ArrayAdapter的API
            //后台线程异步下载图片,Token在这里指定了imageview的实例
            mThumbnailThread.queueThumbnail(imageView, item.getmUrl());
            return convertView;
        }
    }

}
