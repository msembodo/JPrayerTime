package msembodo_at_gmail_dot_com.jprayertime.location;

import msembodo_at_gmail_dot_com.jprayertime.response.HttpResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provide details of date & time based on location input.
 *
 * @author Martyono Sembodo (martyono.sembodo@gmail.com)
 */
public class LocalDateTime {
    private final String location;

    public String timeZoneName;
    public double latitude;
    public double longitude;
    public String formattedAddress;
    public int year;
    public int month;
    public int day;
    public int localOffset;
    public String formattedDateTime;

    public LocalDateTime(String address) {
        this.location = address;
        this.getLocalDateTime();
    }

    private void getLocalDateTime() {
        DateTime localDateTime;
        double rawOffset;
        double dstOffset;

        DateTime utcDate = new DateTime(DateTimeZone.UTC);

        double origin = (new DateTime(1970, 1, 1, 0, 0, 0).getMillis()) / 1000;
        double dateNow = utcDate.getMillis() / 1000;
        double timeStamp = dateNow - origin;

        Location loc = new Location(location);

        String urlTimeZone = "https://maps.googleapis.com/maps/api/timezone/json?location=";
        urlTimeZone += Double.toString(loc.latitude) + "," + Double.toString(loc.longitude) + "&timestamp=";
        urlTimeZone += Double.toString(timeStamp);
        urlTimeZone += "&sensor=false";

        HttpResponse hr = new HttpResponse(urlTimeZone);

        try {
            JSONObject tzResponse = new JSONObject(hr.response);

            dstOffset = tzResponse.getDouble("dstOffset");
            rawOffset = tzResponse.getDouble("rawOffset");
            timeZoneName = tzResponse.getString("timeZoneName");
            localDateTime = utcDate.plusSeconds((int) rawOffset + (int) dstOffset);

            formattedAddress = loc.formattedAddress;
            latitude = loc.latitude;
            longitude = loc.longitude;
            year = localDateTime.getYear();
            month = localDateTime.getMonthOfYear();
            day = localDateTime.getDayOfMonth();
            localOffset = (int) rawOffset / 3600;

            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
            formattedDateTime = dtf.print(localDateTime);
        }
        catch (JSONException e) {
            System.out.print("JPrayerTime: unknown timezone.");
            System.exit(1);
        }
    }
}
