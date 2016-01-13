package net.msembodo.jprayertime;

import org.json.JSONException;

/**
 * Main class to run the application.
 *
 * @author Martyono Sembodo (martyono.sembodo@gmail.com)
 */
public class Main implements HourMinute {
    public static void main(String[] args) {
        String location;

        try {
            if (args.length != 1)
                throw new ArrayIndexOutOfBoundsException();

            location = args[0];

            PrayerTimes pt = new PrayerTimes(location);

            System.out.println("Prayer Time for " + pt.formattedAddress);
            System.out.println("Standard time name: " + pt.timeZoneName);
            System.out.println("Local date & time: " + pt.formattedDateTime);
            System.out.println();
            System.out.printf("%10s%s%02d%s%02d\n", "Fajr", " - ", pt.fajrTime[HOUR], ":", pt.fajrTime[MINUTE]);
            System.out.printf("%10s%s%02d%s%02d\n", "Sunrise", " - ", pt.sunriseTime[HOUR], ":", pt.sunriseTime[MINUTE]);
            System.out.printf("%10s%s%02d%s%02d\n", "Zuhr", " - ", pt.zuhrTime[HOUR], ":", pt.zuhrTime[MINUTE]);
            System.out.printf("%10s%s%02d%s%02d\n", "Asr", " - ", pt.asrTime[HOUR], ":", pt.asrTime[MINUTE]);
            System.out.printf("%10s%s%02d%s%02d\n", "Maghrib", " - ", pt.maghribTime[HOUR], ":", pt.maghribTime[MINUTE]);
            System.out.printf("%10s%s%02d%s%02d\n", "Isha", " - ", pt.ishaTime[HOUR], ":", pt.ishaTime[MINUTE]);
            System.out.println();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("USAGE  : JPrayerTime <location>");
            System.out.println("EXAMPLE: JPrayerTime \"London, UK\"");
        }
        catch (JSONException e) {
            System.err.println("Location not found or does not exist.");
            System.exit(1);
        }
    }
}
