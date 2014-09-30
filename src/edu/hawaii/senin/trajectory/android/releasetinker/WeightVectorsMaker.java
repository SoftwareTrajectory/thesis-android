package edu.hawaii.senin.trajectory.android.releasetinker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;

public class WeightVectorsMaker {

  private static final String PRE_DATA_FNAME = "results/pre_portraits.txt";
  private static final String POST_DATA_FNAME = "results/post_portraits.txt";

  private static HashMap<Integer, ReleaseRecord> releases;

  private static final int[] RELEASES_OF_INTEREST = { 38, 36, 34 };

  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-DD");

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(WeightVectorsMaker.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws TSException {

    releases = ReleaseFactory.getReleases();

    HashMap<Integer, HashMap<String, HashMap<String, int[]>>> dataPreRelease = loadBehaviorData(PRE_DATA_FNAME);
    HashMap<Integer, HashMap<String, HashMap<String, int[]>>> dataPostRelease = loadBehaviorData(POST_DATA_FNAME);

    ArrayList<WordBag> bags = new ArrayList<WordBag>();

    for (int rId : RELEASES_OF_INTEREST) {

      ReleaseRecord rr = releases.get(rId);
      consoleLogger.info("Processing release #" + rr.getId() + ", " + rr.getName());

      HashMap<String, int[]> preArrays = dataPreRelease.get(rr.getId()).get("pre");
      consoleLogger.info("  pre-release arrays: " + preArrays.size());
      WordBag wb = new WordBag("pre-" + rr.getId());
      for (Entry<String, int[]> e : preArrays.entrySet()) {
        String word = toSAX(toDoubles(e.getValue()), 7, 4, 3);
        wb.addWord(word);
      }
      consoleLogger.info("    wordbag: " + wb.toString().replace("\n", ", "));
      bags.add(wb);

      HashMap<String, int[]> postArrays = dataPostRelease.get(rr.getId()).get("post");
      consoleLogger.info("  post-release arrays: " + postArrays.size());
      WordBag wb2 = new WordBag("post-" + rr.getId());
      for (Entry<String, int[]> e : postArrays.entrySet()) {
        String word = toSAX(toDoubles(e.getValue()), 7, 4, 3);
        wb2.addWord(word);
      }
      consoleLogger.info("    wordbag: " + wb.toString().replace("\n", ", "));
      bags.add(wb2);
    }

    HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);

    System.out.println(TextUtils.tfidfToTable(tfidf));

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

  private static double[] toDoubles(int[] value) {
    double[] res = new double[value.length];
    for (int i = 0; i < value.length; i++) {
      res[i] = (double) value[i];
    }
    return res;
  }

  private static HashMap<Integer, HashMap<String, HashMap<String, int[]>>> loadBehaviorData(
      String preDataFname) {

    HashMap<Integer, HashMap<String, HashMap<String, int[]>>> res = new HashMap<Integer, HashMap<String, HashMap<String, int[]>>>();

    try {
      BufferedReader bw = new BufferedReader(new FileReader(new File(preDataFname)));
      bw.readLine();
      String str = null;
      while (null != (str = bw.readLine()) && str.replaceAll("\\s+", "").length() > 0) {

        String[] strSplit = str.trim().split("\t");

        // release_id tag id email start end V1 V2 V3 V4 V5 V6 V7
        int release_id = Integer.valueOf(strSplit[0]).intValue();
        String tag = strSplit[1].trim();
        int author_id = Integer.valueOf(strSplit[2].trim()).intValue();
        String email = strSplit[3].trim();
        DateTime start = fmt.withZoneUTC().parseDateTime(strSplit[4].trim());
        DateTime end = fmt.withZoneUTC().parseDateTime(strSplit[5].trim());
        int[] data = new int[7];
        for (int i = 0; i < 7; i++) {
          data[i] = Integer.valueOf(strSplit[6 + i].trim()).intValue();
        }

        if (!(res.containsKey(release_id))) {
          res.put(release_id, new HashMap<String, HashMap<String, int[]>>());
        }
        HashMap<String, HashMap<String, int[]>> pointer = res.get(release_id);

        if (!(pointer.containsKey(tag))) {
          pointer.put(tag, new HashMap<String, int[]>());
        }
        HashMap<String, int[]> pointer2 = pointer.get(tag);

        pointer2.put(String.valueOf(author_id) + start.toString(), data);

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
