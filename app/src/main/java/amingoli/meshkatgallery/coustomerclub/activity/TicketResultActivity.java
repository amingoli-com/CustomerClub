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

import com.google.zxing.WriterException;

import java.util.Date;

import amingoli.meshkatgallery.coustomerclub.R;
import amingoli.meshkatgallery.coustomerclub.util.TicketView;
import amingoli.meshkatgallery.coustomerclub.util.Tools;
import amingoli.meshkatgallery.coustomerclub.util.database.Database;
import amingoli.meshkatgallery.coustomerclub.util.database.Query;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class TicketResultActivity extends AppCompatActivity {
    private static final String TAG = "amingoli78-"+TicketResultActivity.class.getSimpleName();

    private SQLiteDatabase writeDatabase, readDatabase;
    private static final String URL = "https://api.androidhive.info/barcodes/search.php?code=dunkirk";
    private TextView txtName, txtDuration, txtDirector, txtGenre, txtRating, txtPrice, txtError;
    private ImageView imgPoster;
    private Button btnBuy;
    private ProgressBar progressBar;
    private TicketView ticketView;
    private String barcode = null;

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
            addQrCode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (writeDatabase !=null) writeDatabase.close();
        if (readDatabase !=null) readDatabase.close();
    }





    private void addQrCode(){
        final Date date = new Date();
        View view = View.inflate(this, R.layout.content_dialog_add_qrcode, null);
        final EditText name = view.findViewById(R.id.name);
        final EditText tel = view.findViewById(R.id.tel);
        final EditText desc = view.findViewById(R.id.desc);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اطلاعات بارکد را وارد کنید");
        builder.setMessage(barcode)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("ذخیره", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Query.insert_qrCode(writeDatabase,barcode,String.valueOf(date.getTime()),getTextEditText(name),getTextEditText(tel),getTextEditText(desc));
                    }
                }).show();
    }



    /**
     * Searches the barcode by making http call
     * Request was made using Volley network library but the library is
     * not suggested in production, consider using Retrofit
     */


    private boolean barcodeWasSaved(){
        Log.d(TAG, "barcodeWasSaved: "+Query.cursor(readDatabase,Query.select_qrCode(barcode)).getCount());
        return Query.cursor(readDatabase,Query.select_qrCode(barcode)).getCount()>0;
    }

    private Cursor inser(){
        return writeDatabase.rawQuery(Query.select_qrCode(barcode),null);
    }


    private String getTextEditText(EditText editText){
        return editText.getText().toString().trim();
    }

    private void searchBarcode() {
        @SuppressLint("Recycle")
        Cursor cursor = Query.cursor(readDatabase,Query.select_qrCode(barcode));
        if (cursor.getCount()>=1){
            cursor.moveToFirst();
            String qrcode = cursor.getString( cursor.getColumnIndex("qrcode") );
            int crated_at = cursor.getInt( cursor.getColumnIndex("crated_at") );
            String name = cursor.getString( cursor.getColumnIndex("name") );
            String tel = cursor.getString( cursor.getColumnIndex("tel") );
            String desc = cursor.getString(cursor.getColumnIndex("desc") );
            renderMovie(qrcode,name,tel, String.valueOf(crated_at),desc,"۳۴۵,۰۰۰","۲۸ مرتبه");
        }else {
            showNoTicket();
        }
    }

    private void showNoTicket() {
        txtError.setVisibility(View.VISIBLE);
        ticketView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Rendering movie details on the ticket
     */
    private void renderMovie(String barcode, String name, final String tel, String date, String desc, String totalPrice, String totalRecord) {
        setImage(barcode);
        txtName.setText(name);
        txtDirector.setText(tel);
        txtDuration.setText(desc);
        txtGenre.setText(Tools.getFormattedDateSimple(Long.valueOf(date)));
        txtRating.setText(totalRecord);
        txtPrice.setText(totalPrice);
        btnBuy.setText(getString(R.string.btn_buy_now));
        btnBuy.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        ticketView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        txtDirector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+tel));
                startActivity(intent);
            }
        });
    }

    private void setImage(String barcode){
        try {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager != null ? manager.getDefaultDisplay() : null;
            Point point = new Point();
            if (display != null) display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = Math.min(width, height);
//                    smallerDimension = smallerDimension / 3;

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


    //  making toolbar transparent
    private void TRANSPARENTTOOLBAR() {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
    private void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void DATABASE(){
        writeDatabase = new Database(this).getWritableDatabase();
        readDatabase = new Database(this).getReadableDatabase();
    }

    private void FINDID(){
        txtName = findViewById(R.id.name);
        txtDirector = findViewById(R.id.director);
        txtDuration = findViewById(R.id.duration);
        txtPrice = findViewById(R.id.price);
        txtRating = findViewById(R.id.rating);
        imgPoster = findViewById(R.id.poster);
        txtGenre = findViewById(R.id.genre);
        btnBuy = findViewById(R.id.btn_buy);
        imgPoster = findViewById(R.id.poster);
        txtError = findViewById(R.id.txt_error);
        ticketView = findViewById(R.id.layout_ticket);
        progressBar = findViewById(R.id.progressBar);

    }
}