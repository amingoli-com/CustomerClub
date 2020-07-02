package amingoli.meshkatgallery.coustomerclub.util;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class Tools {
    public static String getFormattedDateSimple(Long dateTime) {
        PersianDateFormat pdformater = new PersianDateFormat("l j F Y");
        return FaNum.convert(pdformater.format(new PersianDate(dateTime)));
    }
}
