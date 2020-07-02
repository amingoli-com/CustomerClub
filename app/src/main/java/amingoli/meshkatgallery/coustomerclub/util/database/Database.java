package amingoli.meshkatgallery.coustomerclub.util.database;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class Database extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "data_mgcc.db";

    /*Table Get SMS*/
    public static String table_qrCodeList = "qrcode_list";
    public static String table_orderList = "order_list";

    public static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }
    public static String select_del(String id,String del_id,String del_liked){
        return "SELECT * FROM "+ table_qrCodeList +
                " WHERE "+del_id + " = '"+id+"' "+
                " AND "+del_liked + " = 'true' limit 1";
    }




}