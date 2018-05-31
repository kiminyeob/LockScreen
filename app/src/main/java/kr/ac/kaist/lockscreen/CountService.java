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
    private BroadcastReceiver mReceiver;
    protected SharedPreferences pref_startService=null;
    protected SharedPreferences.Editor editor_startService =null;
    protected SharedPreferences pref_other=null;
    protected SharedPreferences.Editor editor_other =null;

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

        Log.i("test", "서비스 시작");

        pref = getSharedPreferences("FocusMode", Activity.MODE_PRIVATE);
        editor = pref.edit();

        pref_count = getSharedPreferences("Count", Activity.MODE_PRIVATE);
        editor_count = pref_count.edit();

        pref_duration = getSharedPreferences("Duration", Activity.MODE_PRIVATE);
        trigger_duration_in_second = pref_duration.getInt("Duration",-1);

        pref_startService = getSharedPreferences("StartService", Activity.MODE_PRIVATE);
        editor_startService = pref_startService.edit();

        pref_other = getSharedPreferences("OtherApp", Activity.MODE_PRIVATE); //다른 앱(홈화면 포함) 실행 중인가?
        editor_other = pref_other.edit();

        editor_startService.putInt("StartService",(int)(System.currentTimeMillis()/1000));
        editor_startService.commit();

        Log.i("current: ", String.valueOf((int)(System.currentTimeMillis()/1000)));
        Log.i("startService: ", String.valueOf(pref_startService.getInt("StartService",-1)));

        //카운터 시작
        isStop = false;
        Thread counter = new Thread(new Counter());
        counter.start();
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
            int serviceStart_time = pref_startService.getInt("StartService",-1);

            Log.i("startService2: ", String.valueOf(serviceStart_time));

            editor_count.putInt("Count",0);
            editor_count.commit();
            while(!isStop){

                int currentTime = (int)(System.currentTimeMillis()/1000);

                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                Log.i("test", "currentTime: "+String.valueOf(currentTime)+
                        " serviceStart: "+String.valueOf(serviceStart_time)+
                        " duration:" + String.valueOf(trigger_duration_in_second) + " Difference: "+String.valueOf(currentTime-serviceStart_time));

                if ((currentTime - serviceStart_time) > trigger_duration_in_second) {
                    if (pref.getInt("FocusMode", -1) != 1) {
                        editor.putInt("FocusMode", 1);
                        editor.commit();
                        editor_count.putInt("Count", (int) System.currentTimeMillis() / 1000);
                        editor_count.commit();
                        Log.i("information", "포커스 모드가 시작되었습니다.");
                    }
                }
                else {
                    editor.putInt("FocusMode", 0);
                    editor.commit();
                    Log.i("information", "포커스 모드가 아닙니다.");
                }
            }
            Log.i("test", "while 끝남");
        }
    }
}


