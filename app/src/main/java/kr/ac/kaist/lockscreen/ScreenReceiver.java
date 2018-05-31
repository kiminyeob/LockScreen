package kr.ac.kaist.lockscreen;

//화면이 켜졌을 때 ACTION_SCREEN_OFF intent 를 받는다.

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
    protected SharedPreferences.Editor editor =null;
    protected SharedPreferences pref_other=null;
    protected SharedPreferences.Editor editor_other =null;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref= context.getSharedPreferences("FocusMode",Context.MODE_PRIVATE);
        int focus = pref.getInt("FocusMode",-1);

        //pref_other = context.getSharedPreferences("OtherApp", Activity.MODE_PRIVATE); //다른 앱(홈화면 포함) 실행 중인가?
        //editor_other = pref_other.edit();


        if (intent.getAction().equals(intent.ACTION_SCREEN_ON) && focus == 1){
            Log.i("information","스마트폰 화면이 켜짐(타이머 종료)");
            Intent i = new Intent(context,LockScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            SharedPreferences pref_flag = context.getSharedPreferences("Flag",Context.MODE_PRIVATE);
            //SharedPreferences pref_other = context.getSharedPreferences("OtherApp",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor_flag = pref_flag.edit();
            int flag = pref_flag.getInt("Flag",-1);
            //int otherApp = pref_other.getInt("OtherApp",-1);

            Log.i("information","스마트폰 화면이 꺼짐" + String.valueOf(flag));

            if (flag != 1 || focus == 0) { // 만약에 잠금 화면에서 화면이 꺼진 것이라면 reset하지 않는다. 그리고 timer가 trigger되지 않았으면.
                final Intent intentService = new Intent(context, CountService.class);
                editor_flag.putInt("Flag",0);
                editor_flag.commit();
                context.stopService(intentService);
                context.startService(intentService);
            }

            /*
            if(otherApp == 1 && focus == 0){
                final Intent intentService = new Intent(context, CountService.class);
                editor_flag.putInt("Flag",0);
                editor_flag.commit();
                context.stopService(intentService);
                context.startService(intentService);
            }
            editor_other.putInt("OtherApp",0);
            editor_other.commit();
            */
        }

        if (intent.getAction().equals("kr.ac.kaist.lockscreen.shake")) {
            Log.i("Shake","움직임을 잘 받았다.");
            final Intent intentService = new Intent(context, CountService.class);
            context.stopService(intentService);
            context.startService(intentService);
            /*
            Intent i = new Intent(context,LockScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            context.startActivity(i);
            */
            Intent intent_home = new Intent(intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
            intent_home.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
            intent_home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 태스크를 생성하여 그 태스크안에서 액티비티 추가
            context.startActivity(intent_home);
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, CountService.class);
            context.startService(i);
        }
    }
}
