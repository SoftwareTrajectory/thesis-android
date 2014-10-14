package edu.hawaii.senin.trajectory.android.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;

/**
 * This takes the full format of trajectories and creates a UCR-formatted file suitable for DIRECT
 * optimization.
 * 
 * @author psenin
 * 
 */
public class Step06BehaviorPrinter {

  private static final String IN_DATA_FNAME = "results/release_28_removed_lines.csv";

  private static final int[] RELEASES_OF_INTEREST = { 1, 3, 4, 8, 10, 11, 12 };

  private static final String[] PRE_PATTERNS = { "ebbbebbbbbbb" };

  private static final String[] POST_PATTERNS = { "edbbbbbbbbbb" };

  // SAX parameters to use
  //
  private static final int WINDOW_SIZE = 12;
  private static final int PAA_SIZE = 12;
  private static final int ALPHABET_SIZE = 5;
  private static final SAXCollectionStrategy STRATEGY = SAXCollectionStrategy.CLASSIC;

  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  private static final DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecondMillis();

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(Step06BehaviorPrinter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws TSException, IOException {

    int[][] params = new int[1][4];
    params[0][0] = WINDOW_SIZE;
    params[0][1] = PAA_SIZE;
    params[0][2] = ALPHABET_SIZE;
    params[0][3] = STRATEGY.index();

    // "pre" or "post"
    // "release numeric ID"
    // "email, dates"
    // metrics
    HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, double[]>>>> data = loadBehaviorData(IN_DATA_FNAME);

    { // PRE-hunter
      HashMap<Integer, HashMap<String, HashMap<String, double[]>>> pre = data.get("pre");
      for (Entry<Integer, HashMap<String, HashMap<String, double[]>>> e : pre.entrySet()) {

        Integer release_id = e.getKey();
        if (contains(RELEASES_OF_INTEREST, release_id)) {

          for (HashMap<String, double[]> e1 : e.getValue().values()) {
            for (Entry<String, double[]> e3 : e1.entrySet()) {
              WordBag tmp = TextUtils.seriesToWordBag("tmp", e3.getValue(), params[0]);
              if (tmp.contains(PRE_PATTERNS[0])) {
                System.out.println("pre, " + e3.getKey() + ", "
                    + Arrays.toString(e3.getValue()).replace(",", ""));
              }
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
            for (Entry<String, double[]> e3 : e1.entrySet()) {
              WordBag tmp = TextUtils.seriesToWordBag("tmp", e3.getValue(), params[0]);
              if (tmp.contains(POST_PATTERNS[0])) {
                System.out.println("post, " + e3.getKey() + ", "
                    + Arrays.toString(e3.getValue()).replace(",", ""));
              }
            }
          }
        }

      }
    }

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
