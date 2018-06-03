package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    protected SharedPreferences pref_duration = null;
    protected SharedPreferences.Editor editor_duration = null;
    protected SharedPreferences pref_flag = null;
    protected SharedPreferences.Editor editor_flag = null;
    protected SharedPreferences pref_other=null;
    protected SharedPreferences.Editor editor_other =null;
    protected SharedPreferences pref_shake =null;
    protected SharedPreferences.Editor editor_shake =null;
    protected ListView listView;
    protected String[] results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DBHelper dbHelper = new DBHelper(MainActivity.this, "data.db",null,1);
        dbHelper.testDB();

        listView = (ListView)findViewById(R.id.listView);
        Button button = (Button)findViewById(R.id.resetButton2);
        Button button_esm = (Button)findViewById(R.id.esmResult);
        Button button_pop = (Button)findViewById(R.id.popupResult);
        Button button_dbClear = (Button)findViewById(R.id.dbClear);
        Button button_confirm = (Button)findViewById(R.id.confirm);
        Button button_submission = (Button)findViewById(R.id.submission);
        final TextView textView = (TextView)findViewById(R.id.input_sec);
        final TextView resultView = (TextView)findViewById(R.id.result);
        final TextView sec= (TextView)findViewById(R.id.textView2);

        listView.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        button_esm.setVisibility(View.GONE);
        button_pop.setVisibility(View.GONE);
        button_dbClear.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        resultView.setVisibility(View.GONE);
        button_confirm.setVisibility(View.GONE);
        sec.setVisibility(View.GONE);

        pref_duration = getSharedPreferences("Duration", Activity.MODE_PRIVATE);
        editor_duration = pref_duration.edit();

        pref_flag = getSharedPreferences("Flag", Activity.MODE_PRIVATE);
        editor_flag = pref_flag.edit();

        pref_other = getSharedPreferences("OtherApp", Activity.MODE_PRIVATE); //다른 앱(홈화면 포함) 실행 중인가?
        editor_other = pref_other.edit();

        pref_shake = getSharedPreferences("Shake", Activity.MODE_PRIVATE);
        editor_shake = pref_shake.edit();


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

        button_esm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<String> results_temp = dbHelper.selectAll("ESM");
                    int count = results_temp.size();
                    results = new String[count];
                    results = results_temp.toArray(results);
                    //Date date = new Date(Long.parseLong(results[0].split("\n")[0].split(":")[1]));
                    //results[0] = date.toString();

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, results);
                    listView.setAdapter(adapter);
                    resultView.setText(String.valueOf(count)+"개의 결과가 발견되었습니다.");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        button_pop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<String> results_temp = dbHelper.selectAll("POPUP2");
                    int count = results_temp.size();
                    results = new String[count];
                    results = results_temp.toArray(results);
                    //Date date = new Date(Long.parseLong(results[0].split("\n")[0].split(":")[1]));
                    //results[0] = date.toString();

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, results);
                    listView.setAdapter(adapter);
                    resultView.setText(String.valueOf(count)+"개의 결과가 발견되었습니다.");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        button_dbClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dbHelper.clearDB();
                    resultView.setText("DB의 데이터가 모두 제거되었습니다.");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });



        editor_other.putInt("OtherApp",0);
        editor_other.commit();

        editor_shake.putInt("Shake",1);
        editor_shake.commit();

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
        //Log.i("Main Activity:",String.valueOf(pref_flag.getInt("Flag",-1)));
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

    //뒤로가기 키 막기
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final Intent intent = new Intent(Intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
        intent.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 태스크를 생성하여 그 태스크안에서 액티비티 추가
        startActivity(intent);
        return true;
    }
}
