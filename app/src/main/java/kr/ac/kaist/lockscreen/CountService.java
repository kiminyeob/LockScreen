package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CountService extends Service {
    protected SharedPreferences pref=null;
    protected SharedPreferences.Editor editor =null;
    private BroadcastReceiver mReceiver;
    protected boolean isStop=  false;
    protected int trigger_duration_in_second = 100;

    public CountService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        //카운터 시작
        isStop = false;
        Thread counter = new Thread(new Counter());
        counter.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        pref = getSharedPreferences("FocusMode", Activity.MODE_PRIVATE);
        editor = pref.edit();

        //노티바 고정 띄우기
        Notification notiEx = new NotificationCompat.Builder(CountService.this)
                .setContentTitle("락스크린")
                .setContentText("실험에 참여해 주셔서 감사합니다")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(9999, notiEx);

        //Screen receiver 로부터 Screen On/OFF Event 를 받을 수 있음
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
        isStop = true;
        unregisterReceiver(mReceiver);
    }

    private class Counter implements Runnable {
        private int count = 0;
        //private Handler handler = new Handler();

        @Override
        public void run() {
            while(!isStop){
                Log.i("test", String.valueOf(count++));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(count == trigger_duration_in_second){
                    break;
                }
            }
            Log.i("test", "Thread finished counter is at " + String.valueOf(count));
        }
    }
}


