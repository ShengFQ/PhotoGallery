package com.bignerdranch.android.photogallery;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
public class PhotoPageFragment extends Fragment {
    private  static final String TAG="PhotoPageFragment";
	private String murl;
	private WebView mwebView;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);//TODO ??
		murl=getActivity().getIntent().getData().toString();
	}
	
	/**
	 * 要在fragment中显示网页
	 * 1.打开javascript,默认是关闭的
	 * 
	 * 2.重写webviewclient方法
	 * 3.指定数据源
	 * */
	
	public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstanceState){
		View view=inflater.inflate(R.layout.fragment_photo_page, parent, false);
		final TextView txtview=(TextView)view.findViewById(R.id.titleTextView);
		final ProgressBar progressBar=(ProgressBar)view.findViewById(R.id.progressBar);//TODO why final
		mwebView=(WebView)view.findViewById(R.id.webView);
		mwebView.getSettings().setJavaScriptEnabled(true);
		// ???这是设置什么
        //可以响应各种渲染事件,如检测渲染器何时开始从特定url加载图片
        //决定是否需要向服务器重新提交post请求
		mwebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
			 return false;
		}

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            public void onPageFinished(WebView view, String url) {
            }


		});
		mwebView.addJavascriptInterface(new Object() {
            public void send(String message) {
                Log.i(TAG,"get msg:"+message);
            }
        },"androidObject");
		// 这是设置什么 setting change browser zhuangshiqi
        //响应改变浏览器中装饰元素的事件接口 JavaScript警告信息,网页图标,状态条加载,当前网页标题的刷新
		mwebView.setWebChromeClient(new WebChromeClient() {
            //进度条更新的回调方法
            public void onProgressChanged(WebView webView, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }

            //标题栏更新的回调接口
            public void onReceivedTitle(WebView webView, String title) {
                txtview.setText(title);
            }
        });
		mwebView.loadUrl(murl);
		
		return view;
	}
}
