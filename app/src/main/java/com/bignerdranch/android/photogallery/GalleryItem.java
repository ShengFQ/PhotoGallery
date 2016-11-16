package com.bignerdranch.android.photogallery;

/**
 * Created by SHENGFQ on 2016/1/21.
 */
public class GalleryItem {
    private String mCaption;
    private String mId;//图片id
    private String mUrl;
    private String mOwner;//用户id
    public String toString(){
    return getmCaption();
    }


    public String getmCaption() {
        return mCaption;
    }

    public void setmCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }


	public String getOwner() {
		return mOwner;
	}


	public void setOwner(String owner) {
		mOwner = owner;
	}
	
	public String getPhotoUrl(){
		StringBuffer sb=new StringBuffer("http://www.flickr.com/");
		return sb.append(this.mOwner).append("/").append(this.mId).toString();
	}

}
