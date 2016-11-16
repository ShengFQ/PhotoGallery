package io.gank.api;

public class CategoryJson<T extends ResourceItem> {
	public String[] category;
	public String error;
	public T[] results;
}
