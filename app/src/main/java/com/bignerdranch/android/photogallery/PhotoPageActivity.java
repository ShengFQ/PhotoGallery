package com.bignerdranch.android.photogallery;


import android.app.Fragment;

public class PhotoPageActivity extends SingleFragmentActivity {

	@Override
	public Fragment createFragment() {
		
		return new PhotoPageFragment();
	}

}
