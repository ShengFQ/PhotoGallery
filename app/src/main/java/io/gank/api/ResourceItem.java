package io.gank.api;

import org.json.JSONObject;

public abstract class ResourceItem {
	public abstract ResourceItem parseItem(JSONObject object);
}
