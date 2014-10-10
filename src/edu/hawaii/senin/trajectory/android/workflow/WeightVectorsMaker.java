package edu.hawaii.senin.trajectory.android.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;

public class WeightVectorsMaker {

  private static final String PRE_DATA_FNAME = "results/release_28_added_lines.csv";

  private static final int[] RELEASES_OF_INTEREST = { 1, 3, 5 };

  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(WeightVectorsMaker.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws TSException {

    // "pre" or "post"
    // "release numeric ID"
    // "email, dates"
    // metrics
    HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, double[]>>>> data = loadBehaviorData(PRE_DATA_FNAME);

    ArrayList<WordBag> bags = new ArrayList<WordBag>();

    for (int rId : RELEASES_OF_INTEREST) {

      HashMap<String, HashMap<String, double[]>> pre = data.get("pre").get(rId);
      WordBag wb = new WordBag("pre-" + rId);

      Collection<HashMap<String, double[]>> preArrays = pre.values();
      for (HashMap<String, double[]> e : preArrays) {
        for (double[] dd : e.values()) {
          String word = toSAX(dd, 7, 4, 3);
          wb.addWord(word);
        }
      }
      consoleLogger.info("  pre-release arrays: " + preArrays.size());

      bags.add(wb);
      //
      // HashMap<String, int[]> postArrays = dataPostRelease.get(rr.getId()).get("post");
      // consoleLogger.info("  post-release arrays: " + postArrays.size());
      // WordBag wb2 = new WordBag("post-" + rr.getId());
      // for (Entry<String, int[]> e : postArrays.entrySet()) {
      // String word = toSAX(toDoubles(e.getValue()), 7, 4, 3);
      // wb2.addWord(word);
      // }
      // consoleLogger.info("    wordbag: " + wb.toString().replace("\n", ", "));
      // bags.add(wb2);
      // }
      //
      // HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);
      //
      // System.out.println(TextUtils.tfidfToTable(tfidf));DateTime

    }
  }

  private static String toSAX(double[] ts, int slidingWindowSize, int paaSize, int alphabetSize)
      throws TSException {

    NormalAlphabet normalA = new NormalAlphabet();

    if (TSUtils.stDev(ts) > 0.5) {
      ts = TSUtils.zNormalize(ts);
    }
    double[] paa = TSUtils.optimizedPaa(ts, paaSize);
    char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(alphabetSize));

    return String.valueOf(currentString);
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
