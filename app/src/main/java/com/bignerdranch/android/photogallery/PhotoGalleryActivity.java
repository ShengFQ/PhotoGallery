package com.bignerdranch.android.photogallery;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PhotoGalleryActivity extends SingleFragmentActivity{
	private static String TAG="PhotoGalleryActivity";
	@Override
	public Fragment createFragment() {
		return new PhotoGalleryFragment();
	}
/*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_gallery);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo_gallery, menu);
		return true;
	}
*/
	/**
	 * 响应Intent打开的Activity实例方法
     * 通过覆盖此方法,可以接收到新的intent
     *
	 * */
	public void onNewIntent(Intent intent){
		PhotoGalleryFragment fragment=(PhotoGalleryFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			String query=intent.getStringExtra(SearchManager.QUERY);
			Log.i(TAG,"receive a new search query:"+query);
            //获取默认的sp对象
			PreferenceManager.getDefaultSharedPreferences(this)
			.edit()
			.putString(FlickrFetchr.PREF_SEARCH_QUERY, query)
			.commit();
		}
		fragment.updateItems();
	}
}
