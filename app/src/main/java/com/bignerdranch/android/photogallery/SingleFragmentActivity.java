package com.bignerdranch.android.photogallery;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;

/**
 * create in 2014-11-02
 * add in chapter9
 * abstractly fragmentActivity for my project,extends by other fragmentActivity tuo guan fragment
 * 
 * */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class SingleFragmentActivity extends Activity {
	public abstract Fragment createFragment();// add in chapter9
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);   
		//get fragmentManager :manager fragment queue and transaction back stack
		FragmentManager fragmentManager=this.getFragmentManager();//TODO 该方法指定只能使用扩展包的类，影响后期使用Fragment
		//FragmentManager fragmentManager=this.getFragmentManager();
		Fragment fragment=fragmentManager.findFragmentById(R.id.fragmentContainer);
		if(fragment==null){
			fragment=createFragment();
			fragmentManager.beginTransaction().add(R.id.fragmentContainer,fragment).commit();
		}
	}
}
