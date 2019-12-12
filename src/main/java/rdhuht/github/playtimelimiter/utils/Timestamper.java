package rdhuht.github.playtimelimiter.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Timestamper {
    private static final SimpleDateFormat format = new SimpleDateFormat(
            "dd/M/yyy HH:mm:ss a");

    public static String now() {
        return format.format(new Date());
    }

    public static String was(Date date) {
        return format.format(date);
    }
}
