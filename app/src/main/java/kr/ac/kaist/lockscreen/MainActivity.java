package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    protected SharedPreferences pref_duration = null;
    protected SharedPreferences.Editor editor_duration = null;
    protected SharedPreferences pref_flag = null;
    protected SharedPreferences.Editor editor_flag = null;
    protected SharedPreferences pref_other=null;
    protected SharedPreferences.Editor editor_other =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.resetButton2);
        Button button_confirm = (Button)findViewById(R.id.confirm);
        final TextView textView = (TextView)findViewById(R.id.input_sec);

        pref_duration = getSharedPreferences("Duration", Activity.MODE_PRIVATE);
        editor_duration = pref_duration.edit();

        pref_flag = getSharedPreferences("Flag", Activity.MODE_PRIVATE);
        editor_flag = pref_flag.edit();

        pref_other = getSharedPreferences("OtherApp", Activity.MODE_PRIVATE); //다른 앱(홈화면 포함) 실행 중인가?
        editor_other = pref_other.edit();

        int set_duration = pref_duration.getInt("Duration",-1);
        textView.setText(String.valueOf(set_duration));

        final Intent intentService = new Intent(this, CountService.class);

        //락스크린 서비스 실행(카운트도 같이 함)
        final Intent intent = new Intent(this, CountService.class);
        startService(intent);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
                startService(intent);
            }
        });

        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int duration = Integer.parseInt(textView.getText().toString());
                    editor_duration.putInt("Duration", duration);
                    editor_duration.commit();
                    Log.i("결과", String.valueOf(duration));

                    stopService(intentService);
                    startService(intentService);
                } catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "올바른 값을 입력해 주세요(1이상 자연수)", Toast.LENGTH_LONG).show();
                }
            }
        });

        editor_other.putInt("OtherApp",0);
        editor_other.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor_flag.putInt("Flag",0);
        editor_flag.commit();
        Log.i("Main Activity:",String.valueOf(pref_flag.getInt("Flag",-1)));
    }

    @Override
    protected void onStop(){
        super.onStop();
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
}
