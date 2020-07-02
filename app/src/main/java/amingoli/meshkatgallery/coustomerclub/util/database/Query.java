package amingoli.meshkatgallery.coustomerclub.util.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static amingoli.meshkatgallery.coustomerclub.util.database.Database.table_orderList;
import static amingoli.meshkatgallery.coustomerclub.util.database.Database.table_qrCodeList;

public class Query {
    private static String TAG = "amingoli78-Query";
    public static void insert_qrCode(SQLiteDatabase database,String qrCode, String cratedAt, String name, String tel, String desc){
        String query = "INSERT INTO " + table_qrCodeList
                + " (qrcode,crated_at,name,tel,desc) "
                + " Values ('"+qrCode+"','"+cratedAt+"','"+name+"','"+tel+"','"+desc+"') ";
        Log.d(TAG, "insert_qrCode: "+query);
        write(database,query);
    }

    public static void insert_order(SQLiteDatabase database,String date,String totalPrice,String qrCode){
        String query = "INSERT INTO " + table_orderList
                + " (date,total_price,qrcode) "
                + " Values ('"+date+"','"+totalPrice+"','"+qrCode+"') ";
        write(database,query);
    }

    public static String select_qrCode(String qrCode){
        return "select * from "+table_qrCodeList+" where qrcode = '"+qrCode+"' limit 1";
    }
    public static String select_order(String qrCode){
        return "select * from "+table_orderList+" where qrcode = '"+qrCode+"'";
    }

    public static void write (SQLiteDatabase database,String Query){
        database.execSQL(Query);
    }
    public static Cursor cursor(SQLiteDatabase database,String query){
        return database.rawQuery(query,null);
    }
}
