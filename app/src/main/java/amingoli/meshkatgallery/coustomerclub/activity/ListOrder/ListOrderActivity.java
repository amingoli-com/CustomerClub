package amingoli.meshkatgallery.coustomerclub.activity.ListOrder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import amingoli.meshkatgallery.coustomerclub.R;
import amingoli.meshkatgallery.coustomerclub.activity.TicketResultActivity;
import amingoli.meshkatgallery.coustomerclub.util.NumberTextWatcher;
import amingoli.meshkatgallery.coustomerclub.util.Tools;
import amingoli.meshkatgallery.coustomerclub.util.database.Database;
import amingoli.meshkatgallery.coustomerclub.util.database.Query;
import ir.hamsaa.persiandatepicker.Listener;
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;;

public class ListOrderActivity extends AppCompatActivity {

    String QR_CODE = null;
    private SQLiteDatabase writeDatabase, readDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order);
        DATABASE();

        QR_CODE = getIntent().getStringExtra("QR_CODE");

        if (TextUtils.isEmpty(QR_CODE)) {
            Toast.makeText(getApplicationContext(), "Barcode is empty!", Toast.LENGTH_LONG).show();
            finish();
        }else {
            itemList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        writeDatabase.close();
        readDatabase.close();
    }

    private List<ModelListOrder> list_main = null;
    private AdapterListOrder adapter;
    private RecyclerView recyclerView;
    private void itemList(){
        list_main = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        addItemToList();
        adapter = new AdapterListOrder(list_main, new AdapterListOrder.listener() {
            @Override
            public void result(int id, String MODEL,String date,String price,String desc) {
                switch (MODEL){
                    case "edit":
                        updateOrder(date,id,price,desc);
                        break;
                    case "view_desc":
                        break;
                    default:

                        break;
                }
            }
        }, this);
        recyclerView.setAdapter(adapter);
    }

    private void addItemToList(){
        int no = 0;
        Cursor cursor = Query.cursor(readDatabase,Query.select_order(QR_CODE));
        while (cursor.moveToNext()){
            no++;
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String total_price = cursor.getString(cursor.getColumnIndex("total_price"));
            String desc = cursor.getString(cursor.getColumnIndex("desc"));
            list_main.add(new ModelListOrder(id,String.valueOf(no),total_price,date,desc));
        }
    }


    private void DATABASE(){
        writeDatabase = new Database(this).getWritableDatabase();
        readDatabase = new Database(this).getReadableDatabase();
    }


    private void updateOrder(final String date, final int id_order,String price,String desc){
        View view = View.inflate(this, R.layout.content_dialog_add_order, null);
        final EditText orderPrice = view.findViewById(R.id.order_price);
        final EditText order_desc = view.findViewById(R.id.order_desc);
        final TextView date_picker = view.findViewById(R.id.date_picker);

        orderPrice.setText(price);
        order_desc.setText(desc);
        date_picker.setText(Tools.getFormattedDateSimple(Long.valueOf(date)));
        orderPrice.addTextChangedListener(new NumberTextWatcher(orderPrice));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اطلاعات خرید را وارد کنید");
        builder.setMessage("")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("ثبت", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (getTextEditText(orderPrice).length()>=1 && !getTextEditText(orderPrice).startsWith("0")){
                            Query.update_order(writeDatabase,QR_CODE,id_order,getTextEditText(orderPrice),getTextEditText(order_desc));
                            Toast.makeText(ListOrderActivity.this, getTextEditText(orderPrice), Toast.LENGTH_SHORT).show();
                            recyclerView.removeAllViews();
                            adapter.notifyDataSetChanged();
                            itemList();
                        }else {
                            Toast.makeText(ListOrderActivity.this, "مبلغی وارد کنید", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("لغو", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private String getTextEditText(EditText editText){
        return editText.getText().toString().trim().replace(",","");
    }
}