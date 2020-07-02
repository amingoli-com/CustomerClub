package amingoli.meshkatgallery.coustomerclub.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import amingoli.meshkatgallery.coustomerclub.R;
import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class Tools {
    public static String getFormattedDateSimple(Long dateTime) {
        PersianDateFormat pdformater = new PersianDateFormat("l j F Y");
        return FaNum.convert(pdformater.format(new PersianDate(dateTime)));
    }
    public static void copyToClipboard(Context context, String data) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clipboard", data);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.msg_copied_clipboard, Toast.LENGTH_SHORT).show();
    }
}
