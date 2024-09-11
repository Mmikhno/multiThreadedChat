import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class Utils {
    private static final String DATE_TIME_TEMPLATE = "dd.MM.yyyy HH:mm:ss";
    private static Calendar calendar = new GregorianCalendar();

    public static String printTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_TEMPLATE);
        return dateFormat.format(calendar.getTime());
    }

    public static void log(String msg, File logFile) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));
        writer.println(msg);
        writer.flush();
    }

    public static int readPort(File file) {
        try {
            Object object = new JSONParser().parse(new FileReader(file));
            JSONObject j = (JSONObject) object;
            long port = (long) j.get("port");
            return (int) port;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String readHost(File file) {
        try {
            Object object = new JSONParser().parse(new FileReader(file));
            JSONObject j = (JSONObject) object;
            return (String) j.get("host");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
