package msembodo_at_gmail_dot_com.jprayertime.response;

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
    private final URL url;

    public HttpResponse(String urlStr) throws MalformedURLException {
        url = new URL(urlStr);
    }

    public String httpGet() throws IOException {
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
}
