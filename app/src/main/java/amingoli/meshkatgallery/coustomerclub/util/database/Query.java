package amingoli.meshkatgallery.coustomerclub.util.database;

import static amingoli.meshkatgallery.coustomerclub.util.database.Database.table_orderList;
import static amingoli.meshkatgallery.coustomerclub.util.database.Database.table_qrCodeList;

public class Query {
    public static String inset_qrCodeList(String qrCode,String cratedAt,String name,String tel,String desc){
        return "INSERT INTO " + table_qrCodeList
                + " (qrcode,crated_at,name,tel,desc) "
                + " Values ("+qrCode+","+cratedAt+","+name+","+tel+","+desc+") ";
    }

    public static String inset_orderList(String date,String totalPrice,String qrCode){
        return "INSERT INTO " + table_orderList
                + " (date,total_price,qrcode) "
                + " Values ("+date+","+totalPrice+","+qrCode+") ";
    }
}
