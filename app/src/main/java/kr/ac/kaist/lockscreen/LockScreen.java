package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LockScreen extends AppCompatActivity {
    protected SharedPreferences pref = null;
    protected SharedPreferences.Editor editor = null;
    protected SharedPreferences pref_count = null;
    protected SharedPreferences.Editor editor_count = null;
    protected SharedPreferences pref_flag = null;
    protected SharedPreferences.Editor editor_flag = null;
    protected SharedPreferences pref_startService=null;
    protected SharedPreferences.Editor editor_startService =null;
    protected SharedPreferences pref_other=null;
    protected SharedPreferences.Editor editor_other =null;
    protected SharedPreferences pref_typing=null;
    protected SharedPreferences.Editor editor_typing =null;
    protected SharedPreferences pref_shaked=null;
    protected SharedPreferences.Editor editor_shaked =null;

    protected Thread myThread=null;
    static Handler handler;
    TextView timer;
    boolean isService = false; // 서비스 중인 확인용
    boolean isStop = false;
    CountService myService;
    AlertDialog dialog;
    boolean ratio_flag = false;
    protected int difference_time;
    String isFocusing = "-1";
    protected DBHelper dbHelper;

    /*
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
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        dbHelper = new DBHelper(LockScreen.this, "data.db",null,1);
        dbHelper.testDB();

        timer = (TextView) findViewById(R.id.timer);
        isFocusing = "-1";

        Window win = getWindow();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Button homeButton = (Button) findViewById(R.id.home);
        Button esmButton = (Button) findViewById(R.id.ESM);

        //서비스
        final Intent intentService = new Intent(this, CountService.class);

        pref = getSharedPreferences("FocusMode", Activity.MODE_PRIVATE);
        editor = pref.edit();

        pref_count = getSharedPreferences("Count", Activity.MODE_PRIVATE);
        editor_count = pref_count.edit();

        pref_flag = getSharedPreferences("Flag", Activity.MODE_PRIVATE);
        editor_flag = pref_flag.edit();

        pref_startService = getSharedPreferences("StartService", Activity.MODE_PRIVATE);
        editor_startService = pref_startService.edit();

        pref_other = getSharedPreferences("OtherApp", Activity.MODE_PRIVATE); //다른 앱(홈화면 포함) 실행 중인가?
        editor_other = pref_other.edit();

        pref_shaked = getSharedPreferences("Shaked", Activity.MODE_PRIVATE);
        editor_shaked = pref_shaked.edit();
        editor_shaked.putInt("Shaked", 0);
        editor_shaked.commit();

        pref_typing = getSharedPreferences("Typing", Activity.MODE_PRIVATE);
        editor_typing = pref_typing.edit();

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                float percentage = random.nextFloat();
                Log.i("확률",String.valueOf(percentage));

                editor_shaked.putInt("Shaked", 0);
                editor_shaked.commit();

                //홈화면으로 가는 intent
                final Intent intent = new Intent(Intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
                intent.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 태스크를 생성하여 그 태스크안에서 액티비티 추가

                if (percentage < 1){

                    editor_typing.putInt("Typing", 1);
                    editor_typing.commit();

                    //사용자 입력 UI정의
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.custom_dialog,null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LockScreen.this);
                    final EditText answer = (EditText) dialogView.findViewById(R.id.answer);
                    final TextView question = (TextView) dialogView.findViewById(R.id.question);
                    final RadioGroup rg = (RadioGroup) dialogView.findViewById(R.id.radioGroup);

                    rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            if(checkedId == R.id.ratio_yes){
                                question.setText("스마트폰을 어떤 용도로 사용할 예정입니까?");
                                isFocusing = "y";
                            }
                            if(checkedId == R.id.ratio_no){
                                question.setText("지금은 어떤 상황인가요?");
                                isFocusing = "n";
                            }
                        }
                    });

                    builder.setView(dialogView);
                    builder.setTitle("상황을 입력해주세요:)");

                    //직접 잠금 해제 --> 확인 버튼
                    builder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id){
                        boolean success;
                        boolean success2;
                        // tag: 1은 해제를 하는 순간...
                        success = dbHelper.insertData(String.valueOf(System.currentTimeMillis()), isFocusing, "1",answer.getText().toString(), String.valueOf(difference_time));
                        success2 = dbHelper.insertData(String.valueOf(System.currentTimeMillis()),"y", String.valueOf(difference_time));
                        isFocusing = "-1";

                        editor_flag.putInt("Flag",0);
                        editor_flag.commit();

                        if(success && success2) {
                            Toast.makeText(getApplicationContext(), "저장되었습니다. 감사합니다:)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "저장 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }

                        /*
                        editor_typing.putInt("Typing", 0);
                        editor_typing.commit();
                        */

                        stopService(intentService);
                        startService(intentService);
                        startActivity(intent);
                        editor.putInt("FocusMode", 0);
                        editor.commit();
                        dialog.cancel();
                        }
                    });

                    //직접 잠금 해제 --> 나중에
                    builder.setNegativeButton("나중에",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id){
                            dbHelper.insertData(String.valueOf(System.currentTimeMillis()),"n", String.valueOf(difference_time));

                            /*
                            editor_typing.putInt("Typing", 0);
                            editor_typing.commit();
                            */
                            editor_flag.putInt("Flag",0);
                            editor_flag.commit();


                            stopService(intentService);
                            startService(intentService);
                            startActivity(intent);
                            editor.putInt("FocusMode", 0);
                            editor.commit();
                            dialog.cancel();

                        }
                    });
                    dialog=builder.create();
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
                else{
                    stopService(intentService);
                    startService(intentService);
                    startActivity(intent);
                    editor.putInt("FocusMode", 0);
                    editor.commit();
                }
                }
        });

        esmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor_typing.putInt("Typing", 1);
                editor_typing.commit();
                SharedPreferences pref_count= getSharedPreferences("Count", Context.MODE_PRIVATE);
                int count = pref_count.getInt("Count",-1);

                editor_shaked.putInt("Shaked", 0);
                editor_shaked.commit();

                //사용자 입력 UI정의
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_dialog,null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(LockScreen.this);
                final EditText answer = (EditText) dialogView.findViewById(R.id.answer);
                final RadioGroup rg = (RadioGroup) dialogView.findViewById(R.id.radioGroup);
                final TextView question = (TextView) dialogView.findViewById(R.id.question);
                rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if(checkedId == R.id.ratio_yes){
                            question.setText("지금은 어떤 상황인가요? (집중관련해서)");
                            isFocusing = "y";
                        }
                        if(checkedId == R.id.ratio_no){
                            question.setText("지금은 어떤 상황인가요?");
                            isFocusing = "n";

                        }
                    }
                });
                builder.setView(dialogView);
                builder.setTitle("상황을 입력해주세요:)");

                builder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        boolean success;
                        // tag: 2는 락스크린 중...
                        success = dbHelper.insertData(String.valueOf(System.currentTimeMillis()), isFocusing, "2",answer.getText().toString(), String.valueOf(difference_time));
                        //dbHelper.insertData(String.valueOf(System.currentTimeMillis()),"y", String.valueOf(difference_time));

                        /*
                        if (rg.getCheckedRadioButtonId() == R.id.ratio_yes) {
                            Log.i("button", "YES");
                            } else if (rg.getCheckedRadioButtonId() == R.id.ratio_no) {
                            Log.i("button", "NO");
                        }
                        */

                        if(success){
                            Toast.makeText(getApplicationContext(), "저장되었습니다. 감사합니다:)", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "저장 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                        isFocusing = "-1";

                        /*
                        editor_typing.putInt("Typing", 0);
                        editor_typing.commit();
                        */
                           ratio_flag = false;
                           dialog.cancel();
                        }
                });
                builder.setNegativeButton("나중에",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){

                        /*
                        editor_typing.putInt("Typing", 0);
                        editor_typing.commit();
                        */
                        dialog.cancel();
                    }
                });
                dialog=builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                }}
        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //FLAG_SHOW_WHEN_LOCKED 는 안드로이드 기본 잠금화면 보다 위에 이 activity 띄워라라고 시키는 것
        //FLAG_DISMISS_KEYGUARD 는 안드로이드 기본 잠금화면을 없애라라고 시키는 것

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

        /*
        editor_typing.putInt("Typing", 1);
        editor_typing.commit();
        */

        editor_shaked.putInt("Shaked", 0);
        editor_shaked.commit();
        //Log.i("resume", "굿굿");

        editor_flag.putInt("Flag",1);
        editor_flag.commit();
    }

    @Override
    public void onStop(){
        super.onStop();
        //Log.i("onStop", "onStop");
        int shaked = pref_shaked.getInt("Shaked",-1);

        //Log.i("shaked?",String.valueOf(shaked));
        if (shaked == 1){
            try{
                dbHelper.insertData(String.valueOf(System.currentTimeMillis()),"shaking", String.valueOf(difference_time));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        editor_shaked.putInt("Shaked", 0);
        editor_shaked.commit();

        /*
        editor_typing.putInt("Typing", 0);
        editor_typing.commit();
        */

        isStop = false;
        myThread.interrupt();
    }

    //뒤로가기 키 막기
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();
        //Log.i("onStart", "onStart");
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
        editor_other.putInt("OtherApp",0);
        editor_other.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        int previous_time = pref_count.getInt("Count",-1); //Focus Mode가 1이 된 순간
        int current_time = (int)System.currentTimeMillis()/1000;

        if (pref_shaked.getInt("Shaked",-1) == 0) {

            difference_time = current_time - previous_time;

            int hour = 0;
            int min = 0;
            int sec = 0;

            if (difference_time > 0) {
                if (difference_time < 60) {
                    sec = difference_time;
                    timer.setText(String.valueOf(sec) + "초");
                } else if (difference_time < 3600) {
                    min = difference_time / 60;
                    sec = difference_time % 60;
                    timer.setText(String.valueOf(min) + "분 " + String.valueOf(sec) + "초");
                } else {
                    hour = difference_time / 3600;
                    min = (difference_time % 3600) / 60;
                    sec = difference_time % 60;
                    timer.setText(String.valueOf(hour) + "시간" + String.valueOf(min) + "분 " + String.valueOf(sec) + "초");
                }
            } else {
                timer.setText("잠금 모드 해제!");
            }
        }
    }
}