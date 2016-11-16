package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by sheng on 16/9/5.
 * 结果检查服务
 */
public class PollService extends IntentService {
    private static final String TAG="PollService";
    public static final String PREF_IS_ALARM_ON="isAlarmOn";//是否启动了定时器的持久化保持
    private  static final int POLL_INTERVAL=1000*15;
    public static final String ACTION_SHOW_NOTIFICATION="com.bignerdranch.android.photogallery.show_notification";//发送广播的action

    public PollService(){
        super(TAG);
    }

    /**检测后台网络的可用性
     * 是否允许使用后台数据,是否打开了网络连接
     * 需要ACCESS_NETWORK_STATE权限
     * @param intent
     */

    protected void onHandleIntent(Intent intent){
        Log.i(TAG,"Received an intent");
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkavaliable=connectivityManager.getBackgroundDataSetting() &&
        connectivityManager.getActiveNetworkInfo()!=null;
        if(!isNetworkavaliable) return;
        //从默认SharedPreferences中获取当前查询结果以及上一次结果ID
        //使用FlickrFeatchr类抓取最新结果集
        //如果有结果返回,抓取结果的第一条
        //检查确认是否不同于上一次结果ID
        //将第一条结果保存回SharedPreferences

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String query=sharedPreferences.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId=sharedPreferences.getString(FlickrFetchr.PREF_LAST_RESULT_ID,null);

        ArrayList<GalleryItem> items=new ArrayList<GalleryItem>();
        if(query!=null){
           // items=new FlickrFetchr().search(query);
        }else{
           // items=new FlickrFetchr().fetchItems();
        }
       // if(items.size()==0){
          //  return;
       // }
       /* String resultId=items.get(0).getmId();
        if(!resultId.equals(lastResultId)){
            Log.i(TAG,"got a new result:"+resultId);
        }else{
            Log.i(TAG,"got a old result:"+resultId);

        }*/
        if(1==1) {
            //创建通知信息notification对象
            Resources r = getResources();
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .setContentTitle(r.getString(R.string.new_pictures_title))
                    .setContentText(r.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(false)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
            //发送广播
            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION));
        }
      //  sharedPreferences.edit().putString(FlickrFetchr.PREF_LAST_RESULT_ID,resultId).commit();

    }

    /**定时器,AlarmManager(定时器)与PendingIntent(启动器)
     *
     *
     * @param context
     * @param isOn
     */
    public static void setServiceAlarm(Context context,boolean isOn){
        Intent i=new Intent(context,PollService.class);
        //创建一个PendingIntent 一个用来发送intent的Context,一个区分PendingIntent来源的请求代码,待发送的Intent对象,如何创建PendingIntent的标志符号
        PendingIntent pendingIntent=PendingIntent.getService(context,0,i,0);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            //设置定时器 描述定时器时间基准的常量,定时器运行的开始时间,定时器循环的时间间隔,一个到时候要发送的PendingIntent
            alarmManager.setRepeating(AlarmManager.RTC,System.currentTimeMillis(),POLL_INTERVAL,pendingIntent);
        }else{
            //取消定时器
            alarmManager.cancel(pendingIntent);
            //同步取消PendingIntent
            pendingIntent.cancel();
        }

        //持久化保存定时器的起停状态
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PollService.PREF_IS_ALARM_ON,isOn)
                .commit();
    }

    public  static boolean isServiceAlarmOn(Context context){
        Intent i=new Intent(context,PollService.class);
        PendingIntent pi=PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }


}
