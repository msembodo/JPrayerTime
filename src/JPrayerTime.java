import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * JPrayerTime -- A command line utility to print prayer times based on location input.
 * 2015 Martyono Sembodo (martyono.sembodo@gmail.com)
 * Reference: Egyptian General Authority of Survey
 * Fajr twilight: -19.5 degree
 * Isha twilight: -17.5 degree
 */

public class JPrayerTime {
    static String formattedAddress;
    static double dstOffset;
    static double rawOffset;
    static String timeZoneName;

    public static void main(String[] args) throws IOException, JSONException {
        String location = args[0];

        // Get latitude-longitude from location.
        JSONObject loc = getLatLong(location);
        double latitude = loc.getDouble("lat");
        double longitude = loc.getDouble("lng");

        System.out.println("Prayer Time for " + formattedAddress);

        DateTime localDateTime = getLocalDateTime(latitude, longitude, new DateTime(DateTimeZone.UTC));

        // Extract localDateTime components for calcPrayerTimes input.
        int localYear = localDateTime.getYear();
        int localMonth = localDateTime.getMonthOfYear();
        int localDay = localDateTime.getDayOfMonth();

        /*
        int localHour = localDateTime.getHourOfDay();
        int localMinute = localDateTime.getMinuteOfHour();
        double localTime = localHour + (localMinute / 60.0);
        */

        int localOffset = (int)(rawOffset / 3600);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");

        System.out.println("Standard time name: " + timeZoneName);
        System.out.println("Local date & time: " + dtf.print(localDateTime));

        // Get prayer times based on given location & local time.
        PrayerTimes pt = getPrayerTimes(localYear, localMonth, localDay, longitude, latitude, localOffset, -19.5, -17.5);

        /*
        // Check to see currentTime goes to which prayer time (for colored output).
        boolean isFajr = false; boolean isZuhr = false; boolean isAsr = false;
        boolean isMaghrib = false; boolean isIsha = false;

        if (localTime < pt.fajr)
            isIsha = true;
        else if (localTime < pt.sunRise)
            isFajr = true;
        else if (localTime < pt.zuhr)
            isZuhr = false;
        else if (localTime < pt.asr)
            isZuhr = true;
        else if (localTime < pt.maghrib)
            isAsr = true;
        else if (localTime < pt.isha)
            isMaghrib = true;
        else if (localTime >= pt.isha)
            isIsha = true;
        */

        // Print prayer times.
        System.out.println();
        System.out.printf("%10s%s%02d%s%02d\n", "Fajr", " - ", pt.fajrHrMin[0], ":", pt.fajrHrMin[1]);
        System.out.printf("%10s%s%02d%s%02d\n", "Sunrise", " - ", pt.sunRiseHrMin[0], ":", pt.sunRiseHrMin[1]);
        System.out.printf("%10s%s%02d%s%02d\n", "Zuhr", " - ", pt.zuhrHrMin[0], ":", pt.zuhrHrMin[1]);
        System.out.printf("%10s%s%02d%s%02d\n", "Asr", " - ", pt.asrHrMin[0], ":", pt.asrHrMin[1]);
        System.out.printf("%10s%s%02d%s%02d\n", "Maghrib", " - ", pt.maghribHrMin[0], ":", pt.maghribHrMin[1]);
        System.out.printf("%10s%s%02d%s%02d\n", "Isha", " - ", pt.ishaHrMin[0], ":", pt.ishaHrMin[1]);
        System.out.println();
    }

    // Connect using REST with JSON output.
    public static String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (conn.getResponseCode() != 200)
            throw new IOException(conn.getResponseMessage());

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null)
            sb.append(line);

        rd.close();
        conn.disconnect();

        return sb.toString();
    }

    // Using Google Maps geocoding to get latitude & longitude.
    public static JSONObject getLatLong(String address) throws IOException, JSONException {
        String urlGeocode = "http://maps.google.com/maps/api/geocode/json?sensor=false&address=";
        urlGeocode += URLEncoder.encode(address, "UTF-8");

        JSONObject gcResponse = new JSONObject(httpGet(urlGeocode));
        JSONObject res = gcResponse.getJSONArray("results").getJSONObject(0);
        formattedAddress = res.getString("formatted_address");

        return res.getJSONObject("geometry").getJSONObject("location");
    }

    // Using Google Maps timezone API to get timezone information.
    public static DateTime getLocalDateTime(double latitude, double longitude, DateTime utcDate)
            throws IOException, JSONException {
        double origin = (new DateTime(1970, 1, 1, 0, 0, 0).getMillis()) / 1000;
        double dateNow = utcDate.getMillis() / 1000;
        double timeStamp = dateNow - origin;

        String urlTimezone = "https://maps.googleapis.com/maps/api/timezone/json?location=";
        urlTimezone += Double.toString(latitude) + "," + Double.toString(longitude) + "&timestamp=";
        urlTimezone += Double.toString(timeStamp);
        urlTimezone += "&sensor=false";

        JSONObject tzResponse = new JSONObject(httpGet(urlTimezone));
        dstOffset = tzResponse.getDouble("dstOffset");
        rawOffset = tzResponse.getDouble("rawOffset");
        timeZoneName = tzResponse.getString("timeZoneName");

        return utcDate.plusSeconds((int)rawOffset + (int)dstOffset);
    }

    /*
    Original algorithm is in C++ by Mahmoud Adly Ezzat.
	http://3adly.blogspot.co.id/2010/07/prayer-times-calculations-pure-c-code.html
     */
    public static PrayerTimes getPrayerTimes(int year, int month, int day,
                                       double longitude, double latitude, int timezone,
                                       double fajrTwilight, double ishaTwilight) {
        PrayerTimes prayerTimes = new PrayerTimes();

        double D = (367 * year) - ((year + (int) ((month + 9) / 12)) * 7 / 4) + (((int)(275 * month / 9)) + day - 730531.5);

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
        double durinalArc = radToDeg(Math.acos((Math.sin(degToRad(-0.8333)) - Math.sin(degToRad(dec))*Math.sin(degToRad(latitude))) / (Math.cos(degToRad(dec)) * Math.cos(degToRad(latitude)))));

        double noon = alpha - ST;
        noon = moreLess360(noon);

        double UTNoon = noon - longitude;

        // Calculating Prayer Times Arcs & Times.

        // 2) Zuhr Time [local noon]
        prayerTimes.zuhr = UTNoon / 15 + timezone;

        // Asr Shafii
        double asrAlt = radToDeg(Math.atan(1 + Math.tan(degToRad(latitude - dec))));
        double asrArc = radToDeg(Math.acos((Math.sin(degToRad(90 - asrAlt)) - Math.sin(degToRad(dec)) * Math.sin(degToRad(latitude))) / (Math.cos(degToRad(dec)) * Math.cos(degToRad(latitude)))));
        asrArc = asrArc / 15;

        // 3) Asr Time
        prayerTimes.asr = prayerTimes.zuhr + asrArc;

        // 1) Shorouq Time
        prayerTimes.sunRise = prayerTimes.zuhr - (durinalArc / 15);

        // 4) Maghrib Time
        prayerTimes.maghrib = prayerTimes.zuhr + (durinalArc / 15);

        double ishaArc = radToDeg(Math.acos((Math.sin(degToRad(ishaTwilight)) - Math.sin(degToRad(dec)) * Math.sin(degToRad(latitude))) / (Math.cos(degToRad(dec)) * Math.cos(degToRad(latitude)))));

        // 5) Isha Time
        prayerTimes.isha = prayerTimes.zuhr + (ishaArc / 15);

        double fajrArc = radToDeg(Math.acos((Math.sin(degToRad(fajrTwilight)) - Math.sin(degToRad(dec)) * Math.sin(degToRad(latitude))) / (Math.cos(degToRad(dec)) * Math.cos(degToRad(latitude)))));

        // 6) Fajr Time
        prayerTimes.fajr = prayerTimes.zuhr - (fajrArc / 15);

        // Convert prayer times from double to hour-minute.
        prayerTimes.fajrHrMin[0] = (int)Math.floor(moreLess24(prayerTimes.fajr));
        prayerTimes.fajrHrMin[1] = (int)Math.floor(moreLess24(prayerTimes.fajr - prayerTimes.fajrHrMin[0]) * 60);

        prayerTimes.sunRiseHrMin[0] = (int)Math.floor(moreLess24(prayerTimes.sunRise));
        prayerTimes.sunRiseHrMin[1] = (int)Math.floor(moreLess24(prayerTimes.sunRise - prayerTimes.sunRiseHrMin[0]) * 60);

        prayerTimes.zuhrHrMin[0] = (int)Math.floor(moreLess24(prayerTimes.zuhr));
        prayerTimes.zuhrHrMin[1] = (int)Math.floor(moreLess24(prayerTimes.zuhr - prayerTimes.zuhrHrMin[0]) * 60);

        prayerTimes.asrHrMin[0] = (int)Math.floor(moreLess24(prayerTimes.asr));
        prayerTimes.asrHrMin[1] = (int)Math.floor(moreLess24(prayerTimes.asr - prayerTimes.asrHrMin[0]) * 60);

        prayerTimes.maghribHrMin[0] = (int)Math.floor(moreLess24(prayerTimes.maghrib));
        prayerTimes.maghribHrMin[1] = (int)Math.floor(moreLess24(prayerTimes.maghrib - prayerTimes.maghribHrMin[0]) * 60);

        prayerTimes.ishaHrMin[0] = (int)Math.floor(moreLess24(prayerTimes.isha));
        prayerTimes.ishaHrMin[1] = (int)Math.floor(moreLess24(prayerTimes.isha - prayerTimes.ishaHrMin[0]) * 60);

        return prayerTimes;
    }

    // Convert degree to radian.
    public static double degToRad(double degree) {
        return (Math.PI / 180) * degree;
    }

    // Convert radian to degree.
    public static double radToDeg(double radian) {
        return (180 / Math.PI) * radian;
    }

    // Make sure a value is between 0 and 360.
    public static double moreLess360(double value) {
        while (value > 360 || value < 0) {
            if (value > 360)
                value -= 360;
            else if (value < 0)
                value += 360;
        }

        return value;
    }

    // Make sure a value is between 0 and 24.
    public static double moreLess24(double value) {
        while (value > 24 || value < 0) {
            if (value > 24)
                value -= 24;
            else if (value < 0)
                value += 24;
        }

        return value;
    }
}
