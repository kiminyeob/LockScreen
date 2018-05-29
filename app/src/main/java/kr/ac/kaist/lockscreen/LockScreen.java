package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class LockScreen extends AppCompatActivity {
    protected SharedPreferences pref=null;
    protected SharedPreferences.Editor editor =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        Window win = getWindow();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Button homeButton = (Button)findViewById(R.id.home);
        Button esmButton = (Button)findViewById(R.id.ESM);

        //서비스
        final Intent intentService = new Intent(this, CountService.class);

        //Toast.makeText(getApplicationContext(), "최초생성", Toast.LENGTH_LONG).show();

        pref = getSharedPreferences("FocusMode", Activity.MODE_PRIVATE);
        editor = pref.edit();

        homeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Toast.makeText(getApplicationContext(), "홈화면으로 이동", Toast.LENGTH_LONG).show();

                stopService(intentService);
                startService(intentService);

                //홈화면으로 이동
                Intent intent = new Intent(Intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
                intent.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 태스크를 생성하여 그 태스크안에서 액티비티 추가
                startActivity(intent);
            }}
        );

        esmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //ESM 받는 것 구현하기
                Toast.makeText(getApplicationContext(), "ESM 받아야 함", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
                intent.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 태스크를 생성하여 그 태스크안에서 액티비티 추가
                startActivity(intent);
            }}
        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //FLAG_SHOW_WHEN_LOCKED 는 안드로이드 기본 잠금화면 보다 위에 이 activity 띄워라라고 시키는 것
        //FLAG_DISMISS_KEYGUARD 는 안드로이드 기본 잠금화면을 없애라라고 시키는 것
    }

    //뒤로가기 키 막기
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}

