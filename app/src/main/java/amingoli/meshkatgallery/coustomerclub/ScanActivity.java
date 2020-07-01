package amingoli.meshkatgallery.coustomerclub;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.vision.barcode.Barcode;
import java.util.List;
import info.androidhive.barcode.BarcodeReader;

public class ScanActivity extends AppCompatActivity {

    BarcodeReader barcodeReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // get the barcode reader instance
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);

        if (barcodeReader != null) {
            Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
            barcodeReader.setListener(new BarcodeReader.BarcodeReaderListener() {
                @Override
                public void onScanned(Barcode barcode) {
                    Toast.makeText(ScanActivity.this, ""+barcode, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onScannedMultiple(List<Barcode> barcodes) {
                    Toast.makeText(ScanActivity.this, "onScannedMultiple", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
                    Toast.makeText(ScanActivity.this, "onBitmapScanned", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onScanError(String errorMessage) {
                    Toast.makeText(ScanActivity.this, "onScanError", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCameraPermissionDenied() {
                    Toast.makeText(ScanActivity.this, "onCameraPermissionDenied", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}