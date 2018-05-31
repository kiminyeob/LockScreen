package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CountService extends Service {
    protected SharedPreferences pref=null;
    protected SharedPreferences.Editor editor =null;
    protected SharedPreferences pref_count =null;
    protected SharedPreferences.Editor editor_count =null;
    protected SharedPreferences pref_duration = null;
    protected SharedPreferences.Editor editor_duration = null;
    private BroadcastReceiver mReceiver;
    protected boolean isStop=  false;
    protected int trigger_duration_in_second;
    public int count = 0;
    //public int Aftercount = 0;

    private final IBinder mBinder = new LocalBinder();

    class LocalBinder extends Binder {
        CountService getService(){
            return CountService.this;
        }
    }

    public CountService() {
    }


    @Override
    public void onCreate(){
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction("android.intent.action.SCREEN_OFF");
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        pref = getSharedPreferences("FocusMode", Activity.MODE_PRIVATE);
        editor = pref.edit();

        pref_count = getSharedPreferences("Count", Activity.MODE_PRIVATE);
        editor_count = pref_count.edit();

        pref_duration = getSharedPreferences("Duration", Activity.MODE_PRIVATE);
        trigger_duration_in_second = pref_duration.getInt("Duration",-1);

        //카운터 시작
        isStop = false;
        Thread counter = new Thread(new Counter());
        counter.start();

        Log.i("test", "서비스 시작");

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        //노티바 고정 띄우기
        Notification notiEx = new NotificationCompat.Builder(CountService.this)
                .setContentTitle("락스크린")
                .setContentText("실험에 참여해 주셔서 감사합니다")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(9999, notiEx);

        //Screen receiver로부터 Screen On/OFF event를 받을 수 있음
        if( intent == null)
        {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mReceiver = new ScreenReceiver();
            registerReceiver(mReceiver, filter);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i("test", "서비스 종료");
        isStop = true;
        unregisterReceiver(mReceiver);
    }

    private class Counter implements Runnable {
        //private Handler handler = new Handler();

        @Override
        public void run() {
            editor_count.putInt("Count",0);
            editor_count.commit();
            while(!isStop){
                Log.i("test", String.valueOf(count++));
                if(count > trigger_duration_in_second){
                    try{
                        Thread.sleep(1000);
                        if(pref.getInt("FocusMode",-1) != 1) {
                            editor.putInt("FocusMode", 1);
                            editor.commit();
                            editor_count.putInt("Count",(int)System.currentTimeMillis()/1000);
                            editor_count.commit();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        Thread.sleep(1000);
                        editor.putInt("FocusMode", 0);
                        editor.commit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i("test", "Thread finished counter is at " + String.valueOf(count));
        }
    }

    int getCount(){
        return count;
    }
}


