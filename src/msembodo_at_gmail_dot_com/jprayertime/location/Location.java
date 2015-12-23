package msembodo_at_gmail_dot_com.jprayertime.location;

import msembodo_at_gmail_dot_com.jprayertime.response.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Provide location detail such as latitude & longitude using Google Maps geocoding API.
 *
 * @author Martyono Sembodo (martyono.sembodo@gmail.com)
 */
public class Location {
    private final String location;

    public String formattedAddress;
    public double latitude;
    public double longitude;

    public Location(String address) {
        this.location = address;
        this.getLatLong();
    }

    private void getLatLong() {
        try {
            String urlGeocode = "http://maps.google.com/maps/api/geocode/json?sensor=false&address=";
            urlGeocode += URLEncoder.encode(location, "UTF-8");

            HttpResponse hr = new HttpResponse(urlGeocode);

            JSONObject gcResponse = new JSONObject(hr.response);
            JSONObject res = gcResponse.getJSONArray("results").getJSONObject(0);
            formattedAddress = res.getString("formatted_address");

            JSONObject latlong = res.getJSONObject("geometry").getJSONObject("location");
            latitude = latlong.getDouble("lat");
            longitude = latlong.getDouble("lng");
        }
        catch (JSONException e) {
            System.out.println("JPrayerTime: unknown location.");
            System.exit(1);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
