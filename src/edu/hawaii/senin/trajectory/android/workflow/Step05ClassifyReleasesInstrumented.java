package edu.hawaii.senin.trajectory.android.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
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

public class Step05ClassifyReleasesInstrumented {

  private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

  private static final DecimalFormat df = (DecimalFormat) NumberFormat
      .getNumberInstance(Locale.FRANCE);

  // SAX parameters to use
  //
  private static final int WINDOW_SIZE = 21;
  private static final int PAA_SIZE = 10;
  private static final int ALPHABET_SIZE = 12;
  private static final SAXCollectionStrategy STRATEGY = SAXCollectionStrategy.EXACT;

  private static final String IN_DATA_FNAME = "results/release_28_added_lines.csv";

  private static final String PRE_CLASS = "results/pre_words.csv";
  private static final String POST_CLASS = "results/post_words.csv";

  // logger business
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(Step01PrintReleaseSeries.class);
    consoleLogger.setLevel(LOGGING_LEVEL);

    df.setMinimumFractionDigits(8);
  }

  /**
   * @param args
   * @throws IOException
   * @throws TSException
   * @throws IndexOutOfBoundsException
   * @throws ParseException
   */
  public static void main(String[] args) throws IOException, IndexOutOfBoundsException,
      TSException, ParseException {

    int[][] params = new int[1][4];
    params[0][0] = WINDOW_SIZE;
    params[0][1] = PAA_SIZE;
    params[0][2] = ALPHABET_SIZE;
    params[0][3] = STRATEGY.index();

    HashMap<String, Double> preVector = readVector(PRE_CLASS);
    HashMap<String, Double> postVector = readVector(POST_CLASS);

    int goodCounter = 0;

    HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, double[]>>>> data = loadBehaviorData(IN_DATA_FNAME);

    { // PRE DATA CLASSIFICATION
      HashMap<Integer, HashMap<String, HashMap<String, double[]>>> preData = data.get("pre");

      for (Entry<Integer, HashMap<String, HashMap<String, double[]>>> e : preData.entrySet()) {

        WordBag preBag = new WordBag("test");
        Integer release_id = e.getKey();

        {
          for (HashMap<String, double[]> e1 : e.getValue().values()) {
            for (double[] dd : e1.values()) {
              WordBag tmp = TextUtils.seriesToWordBag("tmp", dd, params[0]);
              preBag.mergeWith(tmp);
            }
          }
        }

        HashMap<String, Double> insight = new HashMap<String, Double>();
        double preCosine = TextUtils.cosineSimilarityInstrumented(preBag, preVector, insight);
        if (2 == release_id) {
          for (Entry<String, Double> entry : insight.entrySet()) {
            System.out.println(entry.getKey() + " " + df.format(entry.getValue()));
          }
        }

        insight = new HashMap<String, Double>();
        double postCosine = TextUtils.cosineSimilarityInstrumented(preBag, postVector, insight);

        String res = "misclassified";
        if (preCosine > postCosine) {
          res = "ok";
          goodCounter++;
        }

        System.out.println("pre-" + release_id + " " + df.format(preCosine) + " "
            + df.format(postCosine) + " " + res);

      }
    }

    {
      HashMap<Integer, HashMap<String, HashMap<String, double[]>>> postData = data.get("post");

      for (Entry<Integer, HashMap<String, HashMap<String, double[]>>> e : postData.entrySet()) {

        WordBag preBag = new WordBag("post");
        Integer release_id = e.getKey();

        {
          for (HashMap<String, double[]> e1 : e.getValue().values()) {
            for (double[] dd : e1.values()) {
              WordBag tmp = TextUtils.seriesToWordBag("tmp", dd, params[0]);
              preBag.mergeWith(tmp);
            }
          }
        }

        HashMap<String, Double> insight = new HashMap<String, Double>();
        double preCosine = TextUtils.cosineSimilarityInstrumented(preBag, preVector, insight);

        insight = new HashMap<String, Double>();
        double postCosine = TextUtils.cosineSimilarityInstrumented(preBag, postVector, insight);
        if (2 == release_id) {
          for (Entry<String, Double> entry : insight.entrySet()) {
            System.out.println(entry.getKey() + " " + df.format(entry.getValue()));
          }
        }

        String res = "misclassified";
        if (postCosine > preCosine) {
          res = "ok";
          goodCounter++;
        }

        System.out.println("post-" + release_id + " " + df.format(preCosine) + " "
            + df.format(postCosine) + " " + res);

      }
    }

    System.out.println("=====\nAccuracy: " + goodCounter / 24.0);

  }

  private static HashMap<String, Double> readVector(String fname) throws NumberFormatException,
      IOException {
    HashMap<String, Double> res = new HashMap<String, Double>();
    String line = null;
    BufferedReader br = new BufferedReader(new FileReader(new File(fname)));
    while ((line = br.readLine()) != null) {
      String[] r = line.split("\\s+");
      res.put(r[0], Double.valueOf(r[1]));
    }
    return res;
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
