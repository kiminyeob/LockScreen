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

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref= context.getSharedPreferences("FocusMode",Context.MODE_PRIVATE);
        int focus = pref.getInt("FocusMode",-1);


        if (intent.getAction().equals(intent.ACTION_SCREEN_ON) && focus == 1){
            Log.i("information","스마트폰 화면이 켜짐(타이머 종료)");
            Intent i = new Intent(context,LockScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            SharedPreferences pref_flag = context.getSharedPreferences("Flag",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor_flag = pref_flag.edit();
            int flag = pref_flag.getInt("Flag",-1);

            Log.i("information","스마트폰 화면이 꺼짐" + String.valueOf(flag));

            if (flag != 1) { // 만약에 잠금 화면에서 화면이 꺼진 것이라면 reset하지 않는다.
                final Intent intentService = new Intent(context, CountService.class);
                editor_flag.putInt("Flag",0);
                editor_flag.commit();
                context.stopService(intentService);
                context.startService(intentService);
            }





            /*
            Intent i = new Intent(context,LockScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // activity에서 startActivity를 하는게 아니기 때문에 넣어야 한다(안넣으면 에러남)
            context.startActivity(i);
            */
        }

        if (intent.getAction().equals("kr.ac.kaist.lockscreen.TIMER_FINISHED") && focus == 1) {
            Log.i("information","타이머 종료");

            /*
            Intent i = new Intent(context,LockScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            context.startActivity(i);
            */
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, CountService.class);
            context.startService(i);
        }
    }
}
