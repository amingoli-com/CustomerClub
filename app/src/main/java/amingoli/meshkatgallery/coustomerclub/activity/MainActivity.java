package amingoli.meshkatgallery.coustomerclub.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import amingoli.meshkatgallery.coustomerclub.BuildConfig;
import amingoli.meshkatgallery.coustomerclub.R;
import amingoli.meshkatgallery.coustomerclub.util.FaNum;
import amingoli.meshkatgallery.coustomerclub.util.database.Database;
import amingoli.meshkatgallery.coustomerclub.util.database.Query;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase writeDatabase, readDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // making toolbar transparent
        transparentToolbar();
        setContentView(R.layout.activity_main);
        DATABASE();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        writeDatabase.close();
        readDatabase.close();
    }

    public void click(View view) {
        switch (view.getTag().toString()){
            case "tel":
                addQrCode();
                break;
            case "scan":
                if (permission()){
                    startScanActivity();
                }
                break;
            default:
                aboutMe();
                break;
        }
    }

    /**
     * Alert Dialog
     * */
    private void addQrCode(){
        View view = View.inflate(this, R.layout.content_dialog_search_by_tel, null);
        final EditText edt_tel = view.findViewById(R.id.edt_tel);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("شماره تلفن را وارد کنید")
                .setView(view)
                .setCancelable(true)
                .setPositiveButton("جستجو", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String tel = FaNum.convertToEN(edt_tel.getText().toString());
                        if (tel.length()>7 && tel.length()<=15){
                            if (barcodeWasSaved(tel)){
                                showQrCode(tel);
                            }else {
                                dialogDoYouWantAddQrCodeByThisTel(tel);
                            }
                        }else {
                            Toast.makeText(MainActivity.this, "قالب شماره تلفن صحیح نیست!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        builder.show();
    }

    private boolean barcodeWasSaved(String tel){
        return Query.cursor(readDatabase,Query.select_qrCodeByTel(tel)).getCount()>0;
    }

    private void showQrCode(String tel){
        Cursor cursor = Query.cursor(readDatabase,Query.select_qrCodeByTel(tel));
        cursor.moveToFirst();
        goToTicketResult(cursor.getString(cursor.getColumnIndex("qrcode")));
    }

    private void dialogDoYouWantAddQrCodeByThisTel(final String tel){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ثبت مشتری جدید")
                .setMessage("آیا میخواهید مشتری جدید با این شماره تلفن ثبت گردد؟")
                .setCancelable(true)
                .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        goToTicketResult(tel);
                    }
                })
                .setNegativeButton("خیر", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void aboutMe(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("ساخته شده توسط امین گلی"+"\n"+"نسخه "+ FaNum.convert(BuildConfig.VERSION_NAME))
                .setCancelable(true)
                .setPositiveButton("ارتباط با من", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://AminGoli.com")));
                    }
                })
                .setNegativeButton("بستن", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void goToTicketResult(String QR_CODE){
        Intent goToTicket = new Intent(MainActivity.this,TicketResultActivity.class);
        goToTicket.putExtra("code",QR_CODE);
        startActivity(goToTicket);
    }

    /**
     * DataBase
     * */
    private void DATABASE(){
        writeDatabase = new Database(this).getWritableDatabase();
        readDatabase = new Database(this).getReadableDatabase();
    }

    /**
     * Other
     * */
    private void transparentToolbar() {
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

    /**
     * check Permission for SMS
     * */
    private boolean dialogIsRun = false;
    private static final int CAMERA_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private boolean permission(){
        final String[] a = {Manifest.permission.CAMERA};

        SharedPreferences permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //Show Information about why you need the permission
                dialogIsRun = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.desc_need_permission);
                builder.setPositiveButton(R.string.ok_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogIsRun = false;
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, a, CAMERA_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton(R.string.exit_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogIsRun = false;
                        finish();
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.CAMERA,false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                dialogIsRun = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.desc_need_permission);
                builder.setPositiveButton(R.string.ok_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogIsRun = false;
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton(R.string.exit_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogIsRun = false;
                        finish();
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this, a, CAMERA_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.CAMERA,true);
            editor.apply();
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CONSTANT: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanActivity();
                    // call your method
                } else {
                    permission();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void startScanActivity(){
        startActivity(new Intent(MainActivity.this, ScanActivity.class));
    }
}
