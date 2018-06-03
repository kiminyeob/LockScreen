/*
사용자가 입력한 데이터 ESM
사용자가 PUPUP받았을 때 데이터 POP UP
 */

package kr.ac.kaist.lockscreen;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static DBHelper dbHelper = null;
    public static final String DATABASE_NAME = "data.db";
    public static final String TABLE_NAME = "ESM";
    public static final String TABLE_NAME2 = "POPUP2";
    public static final int DB_VERSION = 1;

    //private SQLiteDatabase db;

    private Context context;

    /*
    public static DBHelper getInstance(Context context){ // 싱글턴 패턴으로 구현하였다.
        if(dbHelper == null){
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }
    */

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ESM ( ");
        sb.append("_ID INTEGER PRIMARY KEY AUTOINCREMENT,");
        sb.append("TIME TEXT,");
        sb.append("IS_FOCUSING TEXT,");
        sb.append("TAG TEXT,");
        sb.append("EXPLAIN TEXT,");
        sb.append("LOCK_TIME TEXT)");
        db.execSQL(sb.toString());

        StringBuffer sb2 = new StringBuffer();
        sb2.append("CREATE TABLE POPUP2 ( ");
        sb2.append("_ID INTEGER PRIMARY KEY AUTOINCREMENT,");
        sb2.append("TIME TEXT,");
        sb2.append("ACCEPTED TEXT,");
        sb2.append("LOCK_TIME TEXT)");

        db.execSQL(sb2.toString());
        Log.i("db","DB2가 생성되었습니다");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        onCreate(db);
    }

    public boolean insertData(String time, String isFocus, String tag, String explain, String duration){ // Insert 하는 부분
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TIME", time);
        contentValues.put("IS_FOCUSING", isFocus);
        contentValues.put("TAG", tag);
        contentValues.put("EXPLAIN", explain);
        contentValues.put("LOCK_TIME", duration);

        long result = db.insert(TABLE_NAME, null, contentValues); //ESM
        Log.i("저장(ESM)",time+","+isFocus+","+tag+","+explain+","+duration);
        if(result == -1)
            return false;
        else
            return true;
    }

    public boolean insertData(String time, String accepted, String duration){ // Insert 하는 부분
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TIME", time);
        contentValues.put("ACCEPTED", accepted);
        contentValues.put("LOCK_TIME", duration);

        long result = db.insert(TABLE_NAME2, null, contentValues); //POPUP
        Log.i("저장(POPUP)",time+","+accepted+","+duration);

        if(result == -1)
            return false;
        else
            return true;
    }

    public List<String> selectAll(String table_name){
        SQLiteDatabase db = getWritableDatabase();
        List<String> dataResultList = new ArrayList<String>();
        String sql = "select * from "+table_name+" ORDER BY "+"_ID"+" DESC;";
        Cursor results = db.rawQuery(sql, null);

        if(table_name.equals("POPUP2")) {
            if(results.moveToFirst()){
                do{
                    StringBuffer data = new StringBuffer();
                    data.append(results.getString(1)+",");
                    data.append(results.getString(2)+",");
                    data.append(results.getString(3)+"\n");
                    dataResultList.add(data.toString());
                    //Log.i("result!!",data.toString());
                }while(results.moveToNext());
            }
        }
        else{ //ESM일 때
            if(results.moveToFirst()){
                do{
                    StringBuffer data = new StringBuffer();
                    data.append(results.getString(1)+",");
                    data.append(results.getString(2)+",");
                    data.append(results.getString(3)+",");
                    data.append(results.getString(4)+",");
                    data.append(results.getString(5)+"\n");
                    dataResultList.add(data.toString());
                    //Log.i("result!!",data.toString());
                }while(results.moveToNext());
            }
        }
        return dataResultList;
    }

    public void testDB(){
        SQLiteDatabase db = getReadableDatabase();
    }

    public void clearDB(){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
        db.execSQL("delete from " + TABLE_NAME2);
    }
}
