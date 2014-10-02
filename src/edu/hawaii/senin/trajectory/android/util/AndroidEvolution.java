package edu.hawaii.senin.trajectory.android.util;

import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class AndroidEvolution {

  private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("MM-dd-YY");

  private static final String[] dates = { "09-23-08", "02-09-09", "04-30-09", "09-15-09",
      "10-26-09", "12-03-09", "01-12-10", "05-20-10", "12-06-10", "02-22-11", "05-10-11",
      "07-15-11", "10-19-11", "12-16-11" };
  private static final String[] names = { "Android 1.0", "Android 1.1", "Android 1.5",
      "Android 1.6", "Android 2.0", "Android 2.0.1", "Android 2.1", "Android 2.2", "Android 2.3",
      "Android 3.0", "Android 3.1", "Android 3.2", "Android 4.0", "Android 4.0.3" };

  public static Map<DateTime, String> getReleasesAsMap() throws ParseException {
    Map<DateTime, String> res = new LinkedHashMap<DateTime, String>();
    for (int i = 0; i < dates.length; i++) {
      res.put(fmt.parseDateTime(dates[i]), names[i]);
    }
    return res;
  }

  public static ArrayList<Entry<DateTime, String>> getReleasesAsArray() throws ParseException {
    ArrayList<Entry<DateTime, String>> res = new ArrayList<Entry<DateTime, String>>();
    for (int i = 0; i < dates.length; i++) {
      res.add(new AbstractMap.SimpleEntry<DateTime, String>(fmt.parseDateTime(dates[i]), names[i]));
    }
    return res;
  }

  public static void main(String[] args) throws ParseException {
    for (Entry<DateTime, String> e : getReleasesAsArray()) {
      System.out.println(e.getKey() + ", \"" + e.getValue() + "\"");
    }
  }
}
