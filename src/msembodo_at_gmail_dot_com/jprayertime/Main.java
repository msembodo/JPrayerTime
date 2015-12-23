package msembodo_at_gmail_dot_com.jprayertime;

/**
 * Main class to run the application.
 *
 * @author Martyono Sembodo (martyono.sembodo@gmail.com)
 */
public class Main {
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
            System.out.printf("%10s%s%02d%s%02d\n", "Fajr", " - ", pt.fajrHrMin[0], ":", pt.fajrHrMin[1]);
            System.out.printf("%10s%s%02d%s%02d\n", "Sunrise", " - ", pt.sunriseHrMin[0], ":", pt.sunriseHrMin[1]);
            System.out.printf("%10s%s%02d%s%02d\n", "Zuhr", " - ", pt.zuhrHrMin[0], ":", pt.zuhrHrMin[1]);
            System.out.printf("%10s%s%02d%s%02d\n", "Asr", " - ", pt.asrHrMin[0], ":", pt.asrHrMin[1]);
            System.out.printf("%10s%s%02d%s%02d\n", "Maghrib", " - ", pt.maghribHrMin[0], ":", pt.maghribHrMin[1]);
            System.out.printf("%10s%s%02d%s%02d\n", "Isha", " - ", pt.ishaHrMin[0], ":", pt.ishaHrMin[1]);
            System.out.println();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("USAGE  : JPrayerTime <location>");
            System.out.println("EXAMPLE: JPrayerTime \"London, UK\"");
        }
    }
}
