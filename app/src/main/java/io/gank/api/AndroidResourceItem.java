package io.gank.api;

import org.json.JSONObject;

public class AndroidResourceItem  extends ResourceItem{
	public String _id;
	public String _ns;
	public String createdAt;
	public String desc;
	public String source;
	public String publishedAt;
	public String type;
	public String url;
	public String used;
	public String who;
	public User user;
	@Override
	public AndroidResourceItem parseItem(JSONObject object) {
		// TODO Auto-generated method stub
		return new AndroidResourceItem();
	}
}
