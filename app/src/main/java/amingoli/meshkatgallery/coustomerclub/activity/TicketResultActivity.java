package amingoli.meshkatgallery.coustomerclub.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.zxing.WriterException;

import java.text.DecimalFormat;
import java.util.Calendar;

import amingoli.meshkatgallery.coustomerclub.R;
import amingoli.meshkatgallery.coustomerclub.activity.ListOrder.ListOrderActivity;
import amingoli.meshkatgallery.coustomerclub.util.FaNum;
import amingoli.meshkatgallery.coustomerclub.util.NumberTextWatcher;
import amingoli.meshkatgallery.coustomerclub.util.TicketView;
import amingoli.meshkatgallery.coustomerclub.util.Tools;
import amingoli.meshkatgallery.coustomerclub.util.database.Database;
import amingoli.meshkatgallery.coustomerclub.util.database.Query;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import ir.hamsaa.persiandatepicker.Listener;
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;

public class TicketResultActivity extends AppCompatActivity {
    private static final String TAG = "amingoli78-"+TicketResultActivity.class.getSimpleName();

    private SQLiteDatabase writeDatabase, readDatabase;
    private static final String URL = "https://api.androidhive.info/barcodes/search.php?code=dunkirk";
    private View box;
    private TextView crated_at,txtName, txtDesc,qr_code, txtTel, txtLastDateOrder, txtTotalOrder, txtPrice, txtError,edit,order_list;
    private ImageView imgPoster;
    private Button txtAddOrder;
    private ProgressBar progressBar;
    private TicketView ticketView;
    private String barcode = null;

    private boolean onPause = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (onPause){
            renderAllOrder();
            onPause = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TRANSPARENTTOOLBAR(); // remove toolbar
        setContentView(R.layout.activity_ticket_result); // set layout
        DATABASE();
        FINDID();

        barcode = getIntent().getStringExtra("code");
        // close the activity in case of empty barcode
        if (TextUtils.isEmpty(barcode)) {
            Toast.makeText(getApplicationContext(), "Barcode is empty!", Toast.LENGTH_LONG).show();
            finish();
        }

        // search the barcode


        if (barcodeWasSaved()){
            searchBarcode();
        }else {
            addQrCode(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (writeDatabase !=null) writeDatabase.close();
        if (readDatabase !=null) readDatabase.close();
    }


    /**
    * Alert Dialog
    * */
    private void addQrCode(final boolean isNew){
        View view = View.inflate(this, R.layout.content_dialog_add_qrcode, null);
        final EditText name = view.findViewById(R.id.name);
        final EditText tel = view.findViewById(R.id.tel);
        final EditText desc = view.findViewById(R.id.desc);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اطلاعات بارکد را وارد کنید");
        builder.setMessage(" شناسه بارکد: "+barcode)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("ذخیره", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isNew){
                            Query.insert_qrCode(writeDatabase,barcode,String.valueOf(getDay().getTimeInMillis()),getTextEditText(name),getTextEditText(tel),getTextEditText(desc));
                        }else {
                            Query.update_qrCode(writeDatabase,barcode,getTextEditText(name),getTextEditText(tel),getTextEditText(desc));
                        }
                        searchBarcode();
                    }
                });
        if (!isNew){
            name.setText(txtName.getText());
            tel.setText(txtTel.getText());
            desc.setText(txtDesc.getText());
            builder.setNegativeButton("لغو", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        builder.show();
    }
    private Calendar getDay() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        return cal;
    }

    private void addOrder(final String date){
        View view = View.inflate(this, R.layout.content_dialog_add_order, null);
        final EditText orderPrice = view.findViewById(R.id.order_price);
        final EditText order_desc = view.findViewById(R.id.order_desc);
        final TextView date_picker = view.findViewById(R.id.date_picker);
        date_picker.setText(Tools.getFormattedDateSimple(Long.valueOf(date)));
        orderPrice.addTextChangedListener(new NumberTextWatcher(orderPrice));
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اطلاعات خرید را وارد کنید");
        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton("ثبت", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (getTextEditText(orderPrice).length()>=1 && !getTextEditText(orderPrice).startsWith("0")){
                            Query.insert_order(writeDatabase,date,getTextEditText(orderPrice),barcode,getTextEditText(order_desc));
                            searchBarcode();
                        }else {
                            Toast.makeText(TicketResultActivity.this, "مبلغی وارد کنید", Toast.LENGTH_SHORT).show();
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
    private void datePicker(){
        Typeface iranyekan_regular = ResourcesCompat.getFont(this, R.font.iranyekan_regular);
        PersianDatePickerDialog picker = new PersianDatePickerDialog(this)
                .setPositiveButtonString("باشه")
                .setNegativeButton("لغو")
                .setTodayButton("امروز")
                .setTodayButtonVisible(true)
                .setMinYear(1300)
                .setMaxYear(PersianDatePickerDialog.THIS_YEAR)
                .setInitDate(new PersianCalendar())
                .setActionTextColor(Color.GRAY)
                .setTypeFace(iranyekan_regular)
                .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                .setShowInBottomSheet(true)
                .setListener(new Listener() {
                    @Override
                    public void onDateSelected(PersianCalendar persianCalendar) {
                        addOrder(String.valueOf(persianCalendar.getTimeInMillis()));
                        Log.d(TAG, "onDateSelected: "+persianCalendar.getGregorianChange());//Fri Oct 15 03:25:44 GMT+04:30 1582
                        Log.d(TAG, "onDateSelected: "+persianCalendar.getTimeInMillis());//1583253636577
                        Log.d(TAG, "onDateSelected: "+persianCalendar.getTime());//Tue Mar 03 20:10:36 GMT+03:30 2020
                        Log.d(TAG, "onDateSelected: "+persianCalendar.getDelimiter());//  /
                        Log.d(TAG, "onDateSelected: "+persianCalendar.getPersianLongDate());// سه‌شنبه  13  اسفند  1398
                        Log.d(TAG, "onDateSelected: "+persianCalendar.getPersianLongDateAndTime()); //سه‌شنبه  13  اسفند  1398 ساعت 20:10:36
                        Log.d(TAG, "onDateSelected: "+persianCalendar.getPersianMonthName()); //اسفند
                        Log.d(TAG, "onDateSelected: "+persianCalendar.isPersianLeapYear());//false
                    }
                    @Override
                    public void onDismissed() {

                    }
                });

        picker.show();
    }




    /**
     * DateBase
     */
    private void searchBarcode() {
        Cursor cursor = Query.cursor(readDatabase,Query.select_qrCode(barcode));
        if (cursor.getCount()>=1){
            cursor.moveToFirst();
            String qrcode = cursor.getString( cursor.getColumnIndex("qrcode") );
            String crated_at = cursor.getString( cursor.getColumnIndex("crated_at") );
            String name = cursor.getString( cursor.getColumnIndex("name") );
            String tel = cursor.getString( cursor.getColumnIndex("tel") );
            String desc = cursor.getString(cursor.getColumnIndex("desc") );
            renderTick(qrcode,name,tel, String.valueOf(crated_at),desc);
        }else {
            showNoTicket();
        }
    }

    /**
     * Rendering movie details on the ticket
     */
    @SuppressLint("SetTextI18n")
    private void renderTick(final String barcode, String name, final String tel, String date, String desc) {
        renderImageCode(barcode);
        qr_code.setText(barcode);
        crated_at.setText(getString(R.string.crated_at)+" "+Tools.getFormattedDateSimple(Long.valueOf(date)));
        txtName.setText(name);
        txtTel.setText(FaNum.convert(tel));
        txtDesc.setText(FaNum.convert(desc));
        renderAllOrder();
        txtAddOrder.setText(getString(R.string.btn_buy_now));
        txtAddOrder.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        txtTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+tel));
                startActivity(intent);
            }
        });
        txtAddOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQrCode(false);
            }
        });
        order_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToListOrder = new Intent(TicketResultActivity.this, ListOrderActivity.class);
                goToListOrder.putExtra("QR_CODE",barcode);
                startActivity(goToListOrder);
            }
        });

        ticketView.setVisibility(View.VISIBLE);
        box.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void renderImageCode(String barcode){
        try {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager != null ? manager.getDefaultDisplay() : null;
            Point point = new Point();
            if (display != null) display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = Math.min(width, height);
            smallerDimension = smallerDimension*2;

            QRGEncoder qrgEncoder = new QRGEncoder(
                    barcode, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            imgPoster.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }
    }

    private void renderTotalPrice(){
        int total_price = 0;
        Cursor cursor = Query.cursor(readDatabase,Query.select_order(barcode));
        if (cursor.getCount()>=1){
            while (cursor.moveToNext()){
                total_price = total_price + cursor.getInt( cursor.getColumnIndex("total_price") );
            }
        }
        txtPrice.setText(Tools.getForamtPrice(total_price));
    }

    private void renderTotalOrder(){
        int count = Query.cursor(readDatabase,Query.select_order(barcode)).getCount();
        txtTotalOrder.setText(FaNum.convert(String.valueOf(count)));
    }

    private void renderLastOrder(){
        Cursor cursor = Query.cursor(readDatabase,Query.select_order(barcode));
        if (cursor.getCount()>=1){
            cursor.moveToLast();
            txtLastDateOrder.setText(Tools.getFormattedDateSimple(
                    Long.valueOf(cursor.getString( cursor.getColumnIndex("date") ))));
        }
    }

    private void renderAllOrder(){
        renderLastOrder();
        renderTotalPrice();
        renderTotalOrder();
    }


    /**
     * Util
     * */

    private boolean barcodeWasSaved(){
        Log.d(TAG, "barcodeWasSaved: "+Query.cursor(readDatabase,Query.select_qrCode(barcode)).getCount());
        return Query.cursor(readDatabase,Query.select_qrCode(barcode)).getCount()>0;
    }

    private String getTextEditText(EditText editText){
        return String.valueOf(FaNum.convertToEN(editText.getText().toString().trim()));
    }

    private void showNoTicket() {
        txtError.setVisibility(View.VISIBLE);
        ticketView.setVisibility(View.GONE);
        box.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Tools
     * */
    //  making toolbar transparent
    private void TRANSPARENTTOOLBAR() {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            SETWIMDOWFLAG(this, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            SETWIMDOWFLAG(this, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
    private void SETWIMDOWFLAG(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        } else {
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        win.setAttributes(winParams);
    }

    private void DATABASE(){
        writeDatabase = new Database(this).getWritableDatabase();
        readDatabase = new Database(this).getReadableDatabase();
    }

    private void FINDID(){
        crated_at = findViewById(R.id.crated_at);
        txtName = findViewById(R.id.name);
        txtTel = findViewById(R.id.director);
        txtDesc = findViewById(R.id.duration);
        txtPrice = findViewById(R.id.price);
        txtTotalOrder = findViewById(R.id.rating);
        imgPoster = findViewById(R.id.poster);
        qr_code = findViewById(R.id.qr_code);
        txtLastDateOrder = findViewById(R.id.genre);
        txtAddOrder = findViewById(R.id.btn_buy);
        imgPoster = findViewById(R.id.poster);
        txtError = findViewById(R.id.txt_error);
        ticketView = findViewById(R.id.layout_ticket);
        progressBar = findViewById(R.id.progressBar);
        box = findViewById(R.id.box);
        edit = findViewById(R.id.edit);
        order_list = findViewById(R.id.order_list);

    }
}