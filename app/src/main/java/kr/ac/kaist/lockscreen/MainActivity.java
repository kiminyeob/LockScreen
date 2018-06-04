package kr.ac.kaist.lockscreen;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    protected DBHelper dbHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(MainActivity.this, "data.db",null,1);
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
        final TextView explain = (TextView)findViewById(R.id.explain);


        listView.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        button_esm.setVisibility(View.GONE);
        button_pop.setVisibility(View.GONE);
        button_dbClear.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        resultView.setVisibility(View.GONE);
        button_confirm.setVisibility(View.GONE);
        sec.setVisibility(View.GONE);

        explain.setText("저희 실험에 참여해 주셔서 정말 감사합니다:)\n\n\n" +
                "앱 기능 설명 및 참여 방법\n"+
                "- 일정 시간 동안 동안 폰 사용이 없고, 움직임이 감지되지 않을 경우 자동으로 락스크린이 생성됩니다.\n"+
                "- 락스크린을 해제할 때 설문 창이 출력됩니다. 주어진 설문 질문에 답해 주시면 됩니다.\n"+
                "- 락스크린을 해제하지 않은 상태에서도 설문에 참여할 수 있습니다.\n\n"+
                "유의 사항\n"+
                "- 설문 입력은 가능하다면 구체적으로 입력해주시면 감사드리겠습니다.\n"+
                "- 귀찮으시더라도 설문에 많이 참여해 주세요ㅠㅠ.\n"+
                "- 홈 버튼을 눌러도 락스크린을 해제할 수 있으나, 가능하면 잠금 해제 버튼을 이용해 주시면 감사하겠습니다:)\n\n"+
                "문제가 발생할 경우 아래의 연락처로 연락 바랍니다.\n"+
                "- 김인엽 kiyeob4416@gmail.com, 010-2506-0708\n"+
                "- 차나래: nr.cha@kaist.ac.kr , 010-4497-3971\n\n"
        );

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

        button_submission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Long time = (long)1528049059*(long)1000;
                    Date df = new java.util.Date(time);
                    String vv = new SimpleDateFormat("MM dd, yyyy hh:mma").format(df);

                    if (System.currentTimeMillis() < time)
                        Toast.makeText(getApplicationContext(), "아직 제출 기간이 아닙니다.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "제출이 가능합니다!!", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                writeFile();
                //readFile();
                sendEmail();
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

    public void writeFile(){
        dbHelper = new DBHelper(MainActivity.this, "data.db",null,1);
        List<String> results_esm = dbHelper.selectAll("ESM");
        int count_esm = results_esm.size();
        List<String> results_pop = dbHelper.selectAll("POPUP2");
        int count_pop = results_esm.size();

        //String content = "잘되나요...!";
        String filename = "Experiment_result.txt";

        try{
            FileOutputStream os = openFileOutput(filename,MODE_PRIVATE);
            for (int i=0; i<count_esm; i++){
                os.write(results_esm.get(i).toString().getBytes());
            }

            os.write("\n".getBytes());

            for (int i=0; i<count_pop; i++){
                os.write(results_pop.get(i).toString().getBytes());
            }

            os.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void readFile(){
        FileInputStream fis = null;
        try{
            fis = getApplicationContext().openFileInput("Experiment_result.txt");
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try{
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        Log.i("어떤결과?",sb.toString());

    }

    public void sendEmail(){
        try{
            String[] address = {"kiyeob4416@gmail.com"};
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,"락스크린 실험 결과 입니다 [이름:         ]");
            shareIntent.putExtra(Intent.EXTRA_TEXT,"결과 입니다.");
            shareIntent.putExtra(Intent.EXTRA_EMAIL, address);
            ArrayList<Uri> uris = new ArrayList<Uri>();
            String shareName = new String(getFilesDir().getAbsolutePath()+"/Experiment_result.txt");
            File shareFile = new File(shareName);
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(),"kr.ac.kaist.lockscreen.fileprovider",shareFile);
            uris.add(contentUri);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String msgStr = "Share...?";
            startActivity(Intent.createChooser(shareIntent,msgStr));
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }

                /*
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        String[] address = {"kiyeob4416@gmail.com"}; //주소를 넣어두면 미리 주소가 들어가 있다.
        intent.putExtra(Intent.EXTRA_EMAIL, address);
        intent.putExtra(Intent.EXTRA_SUBJECT, "실험");
        intent.putExtra(Intent.EXTRA_TEXT, "보낼 내용");
        //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/mnt/sdcard/test.jpg")); //파일 첨부
        startActivity(intent);
        */

    }
}
