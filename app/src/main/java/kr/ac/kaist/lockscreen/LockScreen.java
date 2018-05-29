package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LockScreen extends AppCompatActivity {
    protected SharedPreferences pref = null;
    protected SharedPreferences.Editor editor = null;
    protected SharedPreferences pref_count = null;
    protected SharedPreferences.Editor editor_count = null;
    protected Thread myThread=null;
    static Handler handler;
    TextView timer;
    boolean isService = false; // 서비스 중인 확인용
    boolean isStop = false;
    CountService myService;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CountService.LocalBinder mb = (CountService.LocalBinder) service;
            myService = mb.getService();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        timer = (TextView) findViewById(R.id.timer);

        Window win = getWindow();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Button homeButton = (Button) findViewById(R.id.home);
        Button esmButton = (Button) findViewById(R.id.ESM);

        //서비스
        final Intent intentService = new Intent(this, CountService.class);

        //Toast.makeText(getApplicationContext(), "최초생성", Toast.LENGTH_LONG).show();

        pref = getSharedPreferences("FocusMode", Activity.MODE_PRIVATE);
        editor = pref.edit();

        pref_count = getSharedPreferences("Count", Activity.MODE_PRIVATE);
        editor_count = pref_count.edit();

        homeButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              //Toast.makeText(getApplicationContext(), "홈화면으로 이동", Toast.LENGTH_LONG).show();

                                              if (!isService) {
                                                  stopService(intentService);
                                              } else {
                                                  unbindService(conn);
                                              }
                                              //bindService(intentService, conn, BIND_AUTO_CREATE);
                                              startService(intentService);

                                              //Log.i("서비스 실행 중?1: ",String.valueOf(isServiceRunningCheck()));

                                              //홈화면으로 이동
                                              Intent intent = new Intent(Intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
                                              intent.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
                                              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 태스크를 생성하여 그 태스크안에서 액티비티 추가
                                              startActivity(intent);
                                          }
                                      }
        );

        esmButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             SharedPreferences pref_count= getSharedPreferences("Count", Context.MODE_PRIVATE);
                                             int count = pref_count.getInt("Count",-1);
                                             //stopService(intentService);
                                             //startService(intentService);

                                             //ESM 받는 것 구현하기
                                             Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_LONG).show();
                                             Intent intent = new Intent(Intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
                                             intent.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
                                             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 태스크를 생성하여 그 태스크안에서 액티비티 추가
                                             startActivity(intent);
                                         }
                                     }
        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //FLAG_SHOW_WHEN_LOCKED 는 안드로이드 기본 잠금화면 보다 위에 이 activity 띄워라라고 시키는 것
        //FLAG_DISMISS_KEYGUARD 는 안드로이드 기본 잠금화면을 없애라라고 시키는 것

        /*
        MyThread thread = new MyThread();
        thread.setDaemon(true);
        thread.start();
        */

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                updateThread();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        isStop = true;
        Log.i("resume", "굿굿");

        /*
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        while (isServiceRunningCheck()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.i("쓰레드 동작","굿굿");
                        }
                    }
                });
            }
        });
        thread.start();
        */

    }

    //뒤로가기 키 막기
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        handler.sendMessage(handler.obtainMessage());
                        Thread.sleep(1000);
                    }catch (Throwable t){;
                    }
                }
            }
        });
        myThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isStop = false;
        myThread.interrupt();
    }

    public boolean isServiceRunningCheck(){
        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
            if("kr.ac.kaist.lockscreen.CountService".equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    private void updateThread()
    {
        SharedPreferences pref_count= getSharedPreferences("Count", Context.MODE_PRIVATE);
        int count = pref_count.getInt("Count",-1);
        int hour = 0;
        int min = 0;
        int sec = 0;

        if (count < 60){
            sec = count;
            timer.setText(String.valueOf(sec)+"초");
        } else if (count < 3600){
            min = count / 60;
            sec = count % 60;
            timer.setText(String.valueOf(min)+"분 "+String.valueOf(sec)+"초");
        } else {
            hour = count / 3600;
            min = (count % 3600) / 60;
            sec = count % 60;
            timer.setText(String.valueOf(hour)+"시간"+String.valueOf(min)+"분 "+String.valueOf(sec)+"초");
        }
    }


    /*
    static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 0){
                try{
                    if(isService) {
                        timer.setText("Time:" + String.valueOf(myService.getCount()));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };
    */
}


