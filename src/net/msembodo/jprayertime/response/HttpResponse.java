package net.msembodo.jprayertime.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Connect to web services with the supplied URL.
 *
 * @author Martyono Sembodo (martyono.sembodo@gmail.com)
 */
public class HttpResponse {
    public String response;

    public HttpResponse(String urlStr) {
        try {
            URL url = new URL(urlStr);
            httpGet(url);
        }
        catch (MalformedURLException e) {
            System.out.println("JPrayerTime: Malformed URL");
        }
    }

    private void httpGet(URL url) {
        StringBuilder sb = new StringBuilder();

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() != 200)
                throw new IOException(conn.getResponseMessage());

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null)
                sb.append(line);

            rd.close();
            conn.disconnect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        response = sb.toString();
    }
}
