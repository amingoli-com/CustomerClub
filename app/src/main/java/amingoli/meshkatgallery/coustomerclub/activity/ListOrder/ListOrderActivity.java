package amingoli.meshkatgallery.coustomerclub.activity.ListOrder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import amingoli.meshkatgallery.coustomerclub.R;
import amingoli.meshkatgallery.coustomerclub.util.database.Database;
import amingoli.meshkatgallery.coustomerclub.util.database.Query;

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

    private List<ListOrderModel> list_main = null;
    private AdapterListOrder adapter;
    private void itemList(){
        list_main = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        addItemToList();
        adapter = new AdapterListOrder(list_main, new AdapterListOrder.listener() {
            @Override
            public void result(int pos) {
//                removeItem(pos);
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
            list_main.add(new ListOrderModel(String.valueOf(no),total_price,date,desc));
        }
    }


    private void DATABASE(){
        writeDatabase = new Database(this).getWritableDatabase();
        readDatabase = new Database(this).getReadableDatabase();
    }
}