package amingoli.meshkatgallery.coustomerclub.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.zxing.WriterException;

import org.json.JSONObject;

import java.util.Date;

import amingoli.meshkatgallery.coustomerclub.R;
import amingoli.meshkatgallery.coustomerclub.util.MyApplication;
import amingoli.meshkatgallery.coustomerclub.util.TicketView;
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
        searchBarcode(barcode);

        if (barcodeWasSaved()){
            Toast.makeText(this, "OK "+barcode, Toast.LENGTH_SHORT).show();
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

    private void searchBarcode(String barcode) {
        // making volley's json request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL , null,
                new Response.Listener< JSONObject >() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "Ticket response: " + response.toString());

                        // check for success status
                        if (!response.has("error")) {
                            // received movie response
                            renderMovie(response);
                        } else {
                            // no movie found
                            showNoTicket();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                showNoTicket();
            }
        });

        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showNoTicket() {
        txtError.setVisibility(View.VISIBLE);
        ticketView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Rendering movie details on the ticket
     */
    private void renderMovie(JSONObject response) {
        try {

            // converting json to movie object
            Movie movie = new Gson().fromJson(response.toString(), Movie.class);

            if (movie != null) {
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


                txtName.setText(movie.getName());
                txtDirector.setText(movie.getDirector());
                txtDuration.setText(movie.getDuration());
                txtGenre.setText(movie.getGenre());
                txtRating.setText("" + movie.getRating());
                txtPrice.setText(movie.getPrice());
//                Glide.with(this).load(movie.getPoster()).into(imgPoster);

                if (movie.isReleased()) {
                    btnBuy.setText(getString(R.string.btn_buy_now));
                    btnBuy.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                } else {
                    btnBuy.setText(getString(R.string.btn_coming_soon));
                    btnBuy.setTextColor(ContextCompat.getColor(this, R.color.btn_disabled));
                }
                ticketView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);


                btnBuy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

            } else {
                // movie not found
                showNoTicket();
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON Exception: " + e.getMessage());
            showNoTicket();
            Toast.makeText(getApplicationContext(), "Error occurred. Check your LogCat for full report", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // exception
            showNoTicket();
            Toast.makeText(getApplicationContext(), "Error occurred. Check your LogCat for full report", Toast.LENGTH_SHORT).show();
        }
    }

    private class Movie {
        String name;
        String director;
        String poster;
        String duration;
        String genre;
        String price;
        float rating;

        @SerializedName("released")
        boolean isReleased;

        public String getName() {
            return name;
        }

        public String getDirector() {
            return director;
        }

        public String getPoster() {
            return poster;
        }

        public String getDuration() {
            return duration;
        }

        public String getGenre() {
            return genre;
        }

        public String getPrice() {
            return price;
        }

        public float getRating() {
            return rating;
        }

        public boolean isReleased() {
            return isReleased;
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