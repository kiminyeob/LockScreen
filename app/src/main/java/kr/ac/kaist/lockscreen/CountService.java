package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CountService extends Service {
    protected CountDownTimer timer;
    protected long duration = 20*1000;
    protected long interval = 1000;
    protected SharedPreferences pref=null;
    protected SharedPreferences.Editor editor =null;
    private BroadcastReceiver mReceiver;

    public CountService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
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

        //Screen receiver로부터 Screen On/OFF event를 받을 수 있음
        if( intent == null)
        {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mReceiver = new ScreenReceiver();
            registerReceiver(mReceiver, filter);
        }

        //카운트 다운 시작
        timer = new CountDownTimer(duration,interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i("Remaining time",Long.toString(millisUntilFinished));
                editor.putInt("FocusMode",0);
                editor.commit();
                //Toast.makeText(getApplicationContext(), Long.toString(millisUntilFinished), Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFinish() {
                //Toast.makeText(getApplicationContext(), "끝!", Toast.LENGTH_LONG).show();
                editor.putInt("FocusMode",1);
                editor.commit();

                try {
                    Thread.sleep(2*interval);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                Log.i("information","끝!");

                /*
                Intent intent = new Intent("kr.ac.kaist.lockscreen.TIMER_FINISHED");
                intent.putExtra("DATA","COUNT_IS_FINISHED");
                sendBroadcast(intent);
                */
            }
        };
        timer.start();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
