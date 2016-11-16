package com.bignerdranch.android.photogallery;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by sheng on 16/9/27.
 * 手机旋转后,onCreate,onDestroy方法返回的getActivity会返回不同值.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VisibleFragment extends Fragment {
    public static final String TAG="VisibleFragment";
    private BroadcastReceiver mOnshowNotification=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(),"got a broadcast:"+intent.getAction(),Toast.LENGTH_LONG).show();;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG,getActivity()+"");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, getActivity() + "");
    }
    /**
     * 动态注册接收器
     * */
    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter=new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnshowNotification,filter);
    }

    /**
     * 动态解除接收器
     * */
    @Override
    public void onPause(){
        super.onPause();
        getActivity().unregisterReceiver(mOnshowNotification);
    }
}
