package rdhuht.github.playtimelimiter.utils;

import org.bukkit.entity.Player;

public class MyUtils {

    public static String getPlayerDirection(Player playerSelf) {
        String dir = "";
        float y = playerSelf.getLocation().getYaw();
        if (y < 0) {
            y += 360;
        }
        y %= 360;
        int i = (int) ((y + 8) / 22.5);
        if (i == 4) {
            dir = "W";
        } else if (i == 5) {
            dir = "WNW";
        } else if (i == 6) {
            dir = "NW";
        } else if (i == 7) {
            dir = "NNW";
        } else if (i == 8) {
            dir = "N";
        } else if (i == 9) {
            dir = "NNE";
        } else if (i == 10) {
            dir = "NE";
        } else if (i == 11) {
            dir = "ENE";
        } else if (i == 12) {
            dir = "E";
        } else if (i == 13) {
            dir = "ESE";
        } else if (i == 14) {
            dir = "SE";
        } else if (i == 15) {
            dir = "SSE";
        } else if (i == 0) {
            dir = "S";
        } else if (i == 1) {
            dir = "SSW";
        } else if (i == 2) {
            dir = "SW";
        } else if (i == 3) {
            dir = "WSW";
        } else {
            dir = "S";
        }
        return dir;
    }
}
