package edu.hawaii.senin.trajectory.android.workflow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.timeseries.TSException;

public class Step02WriteUCRFormat {

  private static final String IN_DATA_FNAME = "results/release_28_added_lines.csv";

  private static final String OUT_FNAME = "results/test.csv";

  private static final int[] RELEASES_OF_INTEREST = { 1, 3, 5 };

  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(Step02WriteUCRFormat.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws TSException, IOException {

    // "pre" or "post"
    // "release numeric ID"
    // "email, dates"
    // metrics
    HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, double[]>>>> data = loadBehaviorData(IN_DATA_FNAME);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(OUT_FNAME)));

    {
      HashMap<Integer, HashMap<String, HashMap<String, double[]>>> pre = data.get("pre");
      for (Entry<Integer, HashMap<String, HashMap<String, double[]>>> e : pre.entrySet()) {
        Integer release_id = e.getKey();
        if (contains(RELEASES_OF_INTEREST, release_id)) {
          for (HashMap<String, double[]> e1 : e.getValue().values()) {
            for (double[] dd : e1.values()) {
              bw.write("pre-" + release_id + " "
                  + Arrays.toString(dd).replace("[", "").replace("]", "").replace(",", "") + "\n");
            }
          }
        }
      }
    }

    {
      HashMap<Integer, HashMap<String, HashMap<String, double[]>>> post = data.get("post");
      for (Entry<Integer, HashMap<String, HashMap<String, double[]>>> e : post.entrySet()) {
        Integer release_id = e.getKey();
        if (contains(RELEASES_OF_INTEREST, release_id)) {
          for (HashMap<String, double[]> e1 : e.getValue().values()) {
            for (double[] dd : e1.values()) {
              bw.write("post-" + release_id + " "
                  + Arrays.toString(dd).replace("[", "").replace("]", "").replace(",", "") + "\n");
            }
          }
        }
      }
    }

    bw.close();

  }

  private static boolean contains(int[] releasesOfInterest, Integer release_id) {
    for (int i : releasesOfInterest) {
      if (release_id.equals(Integer.valueOf(i))) {
        return true;
      }
    }
    return false;
  }

  private static HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, double[]>>>> loadBehaviorData(
      String preDataFname) {

    HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, double[]>>>> res = new HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, double[]>>>>();

    try {
      BufferedReader bw = new BufferedReader(new FileReader(new File(preDataFname)));
      bw.readLine();
      String str = null;
      while (null != (str = bw.readLine()) && str.trim().length() > 0) {

        String[] strSplit = str.trim().split(", ");

        // post_Android 1.0, 1-post, bzolnier@gmail.com, 2008-09-29T00:00:00.000+02:00,
        // 2008-10-27T00:00:00.000+01:00, 0.0
        String release_key = strSplit[1].substring(strSplit[1].indexOf('-') + 1);
        Integer release_id = Integer.valueOf(strSplit[1].substring(0, strSplit[1].indexOf('-')));
        String email = strSplit[2].trim();
        DateTime start = fmt.withZoneUTC().parseDateTime(strSplit[3].trim());
        DateTime end = fmt.withZoneUTC().parseDateTime(strSplit[4].trim());

        String[] arraySplit = strSplit[5].trim().split("\\s+");
        double[] data = new double[arraySplit.length];
        for (int i = 0; i < arraySplit.length; i++) {
          data[i] = Double.valueOf(arraySplit[i].trim()).doubleValue();
        }

        if (!(res.containsKey(release_key))) {
          res.put(release_key, new HashMap<Integer, HashMap<String, HashMap<String, double[]>>>());
        }
        HashMap<Integer, HashMap<String, HashMap<String, double[]>>> pointer = res.get(release_key);

        if (!(pointer.containsKey(release_id))) {
          pointer.put(release_id, new HashMap<String, HashMap<String, double[]>>());
        }
        HashMap<String, HashMap<String, double[]>> pointer2 = pointer.get(release_id);

        if (!(pointer.containsKey(email))) {
          pointer2.put(email, new HashMap<String, double[]>());
        }
        HashMap<String, double[]> pointer3 = pointer2.get(email);

        pointer3.put(start.toString() + "_" + end.toString(), data);

      }
      bw.close();
      consoleLogger.info("loaded " + res.size() + " release records.");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return res;
  }
}
