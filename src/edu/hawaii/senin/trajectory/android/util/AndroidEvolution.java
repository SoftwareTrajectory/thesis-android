package edu.hawaii.senin.trajectory.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This yields a set of releases.
 * 
 * @author psenin
 * 
 */
public class AndroidEvolution {

  private static final String RELEASES_FNAME = "data/android_releases_major.csv";
  private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYY-MM-dd");
  private static LinkedHashMap<DateTime, String> releases;

  static {
    try {
      releases = new LinkedHashMap<DateTime, String>();
      BufferedReader br = new BufferedReader(new FileReader(new File(RELEASES_FNAME)));
      String str = null;
      while (null != (str = br.readLine())) {
        String[] split = str.split("\\,\\s+");
        String name = split[2].replace("\"", "");
        DateTime date = fmt.parseDateTime(split[1]);
        releases.put(date, name);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Map<DateTime, String> getReleasesAsMap() throws ParseException {
    return releases;
  }

  public static void main(String[] args) throws ParseException {
    for (Entry<DateTime, String> e : releases.entrySet()) {
      System.out.println(e.getKey() + ", \"" + e.getValue() + "\"");
    }
  }
}
