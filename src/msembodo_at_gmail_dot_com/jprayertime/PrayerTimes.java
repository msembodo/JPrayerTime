package msembodo_at_gmail_dot_com.jprayertime;

import msembodo_at_gmail_dot_com.jprayertime.location.LocalDateTime;
import org.json.JSONException;

import java.io.IOException;

/**
 * Calculate prayer times based on location input and also provide location properties
 * such as formatted address, date-time, and time zone name.
 *
 * @author Martyono Sembodo (martyono.sembodo@gmail.com)
 */
public class PrayerTimes {
    public String formattedAddress;
    public String formattedDateTime;
    public String timeZoneName;

    public int[] fajrHrMin = new int[2];
    public int[] sunriseHrMin = new int[2];
    public int[] zuhrHrMin = new int[2];
    public int[] asrHrMin = new int[2];
    public int[] maghribHrMin = new int[2];
    public int[] ishaHrMin = new int[2];

    public PrayerTimes(String address) throws IOException, JSONException {
        double fajrTwilight = -19.5;
        double ishaTwilight = -17.5;

        LocalDateTime ldt = new LocalDateTime(address);
        this.getPrayerTimes(ldt, fajrTwilight, ishaTwilight);
        formattedAddress = ldt.formattedAddress;
        formattedDateTime = ldt.formattedDateTime;
        timeZoneName = ldt.timeZoneName;
    }

    /**
     * Original algorithm is in C++ by Mahmoud Adly Ezzat.
     * http://3adly.blogspot.co.id/2010/07/prayer-times-calculations-pure-c-code.html
     */
    private void getPrayerTimes(LocalDateTime localDateTime, double fajrTwilight, double ishaTwilight) {
        double fajr, sunrise, zuhr, asr, maghrib, isha;

        double D = (367 * localDateTime.year) - ((localDateTime.year + (int) ((localDateTime.month + 9) / 12)) * 7 / 4)
                + (((int)(275 * localDateTime.month / 9)) + localDateTime.day - 730531.5);

        double L = 280.461 + 0.9856474 * D;
        L = moreLess360(L);

        double M = 357.528 + (0.9856003) * D;
        M = moreLess360(M);

        double lambda = L + 1.915 * Math.sin (degToRad(M)) + 0.02 * Math.sin (degToRad(2 * M));
        lambda = moreLess360(lambda);

        double obliquity = 23.439 - 0.0000004 * D;
        double alpha = radToDeg(Math.atan((Math.cos(degToRad(obliquity)) * Math.tan(degToRad(lambda)))));
        alpha = moreLess360(alpha);

        alpha = alpha - (360 * (int)(alpha / 360));
        alpha = alpha + 90 * (Math.floor(lambda / 90) - Math.floor(alpha / 90));

        double ST = 100.46 + 0.985647352 * D;
        double dec = radToDeg(Math.asin(Math.sin(degToRad(obliquity)) * Math.sin(degToRad(lambda))));
        double durinalArc = radToDeg(Math.acos((Math.sin(degToRad(-0.8333)) -
                Math.sin(degToRad(dec))*Math.sin(degToRad(localDateTime.latitude))) / (Math.cos(degToRad(dec)) *
                Math.cos(degToRad(localDateTime.latitude)))));

        double noon = alpha - ST;
        noon = moreLess360(noon);

        double UTNoon = noon - localDateTime.longitude;

        // Calculating Prayer Times Arcs & Times.

        // 2) Zuhr Time [local noon]
        zuhr = UTNoon / 15 + localDateTime.localOffset;

        // Asr Shafii
        double asrAlt = radToDeg(Math.atan(1 + Math.tan(degToRad(localDateTime.latitude - dec))));
        double asrArc = radToDeg(Math.acos((Math.sin(degToRad(90 - asrAlt)) - Math.sin(degToRad(dec)) *
                Math.sin(degToRad(localDateTime.latitude))) / (Math.cos(degToRad(dec)) *
                Math.cos(degToRad(localDateTime.latitude)))));
        asrArc = asrArc / 15;

        // 3) Asr Time
        asr = zuhr + asrArc;

        // 1) Shorouq Time
        sunrise = zuhr - (durinalArc / 15);

        // 4) Maghrib Time
        maghrib = zuhr + (durinalArc / 15);

        double ishaArc = radToDeg(Math.acos((Math.sin(degToRad(ishaTwilight)) - Math.sin(degToRad(dec)) *
                Math.sin(degToRad(localDateTime.latitude))) / (Math.cos(degToRad(dec)) *
                Math.cos(degToRad(localDateTime.latitude)))));

        // 5) Isha Time
        isha = zuhr + (ishaArc / 15);

        double fajrArc = radToDeg(Math.acos((Math.sin(degToRad(fajrTwilight)) - Math.sin(degToRad(dec)) *
                Math.sin(degToRad(localDateTime.latitude))) / (Math.cos(degToRad(dec)) *
                Math.cos(degToRad(localDateTime.latitude)))));

        // 6) Fajr Time
        fajr = zuhr - (fajrArc / 15);

        // Convert prayer times from double to hour-minute.
        fajrHrMin[0] = (int)Math.floor(moreLess24(fajr));
        fajrHrMin[1] = (int)Math.floor(moreLess24(fajr - fajrHrMin[0]) * 60);

        sunriseHrMin[0] = (int)Math.floor(moreLess24(sunrise));
        sunriseHrMin[1] = (int)Math.floor(moreLess24(sunrise - sunriseHrMin[0]) * 60);

        zuhrHrMin[0] = (int)Math.floor(moreLess24(zuhr));
        zuhrHrMin[1] = (int)Math.floor(moreLess24(zuhr - zuhrHrMin[0]) * 60);

        asrHrMin[0] = (int)Math.floor(moreLess24(asr));
        asrHrMin[1] = (int)Math.floor(moreLess24(asr - asrHrMin[0]) * 60);

        maghribHrMin[0] = (int)Math.floor(moreLess24(maghrib));
        maghribHrMin[1] = (int)Math.floor(moreLess24(maghrib - maghribHrMin[0]) * 60);

        ishaHrMin[0] = (int)Math.floor(moreLess24(isha));
        ishaHrMin[1] = (int)Math.floor(moreLess24(isha - ishaHrMin[0]) * 60);
    }

    // Convert degree to radian
    private double degToRad(double degree) {
        return (Math.PI / 180) * degree;
    }

    // Convert radian to degree
    private double radToDeg(double radian) {
        return (180 / Math.PI) * radian;
    }

    // Make sure a value is between 0 and 360
    private double moreLess360(double value) {
        while (value > 360 || value < 0) {
            if (value > 360)
                value -= 360;
            else if (value < 0)
                value += 360;
        }

        return value;
    }

    // Make sure a value is between 0 and 24
    private double moreLess24(double value) {
        while (value > 24 || value < 0) {
            if (value > 24)
                value -= 24;
            else if (value < 0)
                value += 24;
        }

        return value;
    }
}
