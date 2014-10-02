package edu.hawaii.senin.trajectory.android.workflow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.senin.trajectory.android.db.OMAPDB;
import edu.hawaii.senin.trajectory.android.db.OMAPDBManager;
import edu.hawaii.senin.trajectory.android.persistence.ChangePeople;
import edu.hawaii.senin.trajectory.android.persistence.ChangeProject;
import edu.hawaii.senin.trajectory.android.util.AndroidEvolution;

public class PrintAndroidReleaseSeries {

  private static final int DAYS = 30;
  private static final String METRIC_STR = "added_lines";
  private static final String OUTPUT_PREFIX = "data/release_" + METRIC_STR + "_"
      + String.valueOf(DAYS);

  private static TimeZone tz;

  // logger business
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(PrintAndroidReleaseSeries.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * @param args
   * @throws IOException
   * @throws TSException
   * @throws ParseException
   */
  public static void main(String[] args) throws IOException, TSException, ParseException {

    // make GMT - where data was acquired the default timezone
    //
    tz = TimeZone.getTimeZone("UTC");
    TimeZone.setDefault(tz);

    ArrayList<Entry<DateTime, String>> releases = AndroidEvolution.getReleasesAsArray();

    // get the DB connection
    //
    OMAPDB db = OMAPDBManager.getProductionInstance();
    ChangeProject project = db.getAndroidProject("kernel_omap");

    // make data arrays
    //
    ArrayList<double[]> preRelease = new ArrayList<double[]>();
    ArrayList<double[]> postRelease = new ArrayList<double[]>();

    // looping over selected dates
    //
    for (int counter = 0; counter < releases.size() - 1; counter = counter + 1) {

      Entry<DateTime, String> s = releases.get(counter);

      // dates business
      //
      DateTime postReleaseStartMonday = s.getKey().plusDays(7)
          .withDayOfWeek(DateTimeConstants.MONDAY);
      DateTime preReleaseStartMonday = postReleaseStartMonday.minusDays(DAYS).minusDays(7)
          .withDayOfWeek(DateTimeConstants.MONDAY);

      for (int i = 0; i < 4; i++) {

        // makeup dates
        //
        DateTime postStart = postReleaseStartMonday.plusDays(7 * i);
        DateTime preStart = preReleaseStartMonday.plusDays(7 * i);

        // get the list of users
        //
        List<ChangePeople> postUsers = db.getAndroidAuthorsForInterval(project.getId(),
            "%google.com", postStart.toLocalDateTime(), postStart.plusDays(7).toLocalDateTime());
        List<ChangePeople> preUsers = db.getAndroidAuthorsForInterval(project.getId(),
            "%google.com", preStart.toLocalDateTime(), preStart.plusDays(7).toLocalDateTime());

        // extract series
        //
        for (ChangePeople cp : postUsers) {
          double[] series = db.getAndroidAuthorMetricAsSeries(project.getId(), METRIC_STR,
              cp.getId(), postStart.toLocalDateTime(), postStart.plusDays(7).toLocalDateTime());
          if (TSUtils.mean(series) == 0d) {
            continue;
          }
          System.out.println("# " + s.getKey() + ", " + s.getValue() + "-post, " + cp.getEmail()
              + ", " + postStart + ", " + postStart.plusDays(7) + ", " + Arrays.toString(series));
          postRelease.add(series);
        }

        for (ChangePeople cp : preUsers) {
          double[] series = db.getAndroidAuthorMetricAsSeries(project.getId(), METRIC_STR,
              cp.getId(), preStart.toLocalDateTime(), preStart.plusDays(7).toLocalDateTime());
          if (TSUtils.mean(series) == 0d) {
            continue;
          }
          System.out.println("# " + s.getKey() + ", " + s.getValue() + "-pre, " + cp.getEmail()
              + ", " + postStart + ", " + postStart.plusDays(7) + ", " + Arrays.toString(series));
          preRelease.add(series);
        }

      }

      // pre-release
      //
      // double[] preSummary = db.getAndroidMetricAsSeries(project.getId(), METRIC_STR,
      // "%google.com",
      // preReleaseStart, preReleaseStart.plusDays(DAYS));
      // preRelease.add(preSummary);

      // post-release
      //
      // double[] postSummary = db.getAndroidMetricAsSeries(project.getId(), METRIC_STR,
      // "%google.com", postReleaseStart, postReleaseStart.plusDays(DAYS));
      // postRelease.add(postSummary);

    }

    saveSet(preRelease, postRelease, OUTPUT_PREFIX);

  }

  private static void saveSet(ArrayList<double[]> preRelease, ArrayList<double[]> postRelease,
      String outputPrefix) throws IOException {

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPrefix + ".csv")));

    for (double[] s : preRelease) {
      if (TSUtils.mean(s) == 0) {
        continue;
      }
      bw.write("pre " + Arrays.toString(s).replace("[", "").replace("]", "").replace(",", "")
          + "\n");
    }

    for (double[] s : postRelease) {
      if (TSUtils.mean(s) == 0) {
        continue;
      }
      bw.write("post " + Arrays.toString(s).replace("[", "").replace("]", "").replace(",", "")
          + "\n");
    }

    bw.close();
  }

  private static double[] processPeaks(double[] sumSeries) {
    double mean = TSUtils.mean(sumSeries);
    double sd = TSUtils.stDev(sumSeries);
    for (int i = 0; i < sumSeries.length; i++) {
      if (sumSeries[i] > 2. * sd) {
        sumSeries[i] = 2. * sd;
      }
    }
    return sumSeries;
  }

  private static double[] sumSeries(double[] sumSeries, double[] series) {
    for (int i = 0; i < sumSeries.length; i++) {
      sumSeries[i] = sumSeries[i] + series[i];
    }
    return sumSeries;
  }

  private static ArrayList<String[]> readCSV(String fname) throws IOException {
    ArrayList<String[]> res = new ArrayList<String[]>();
    String line = null;
    BufferedReader br = new BufferedReader(new FileReader(new File(fname)));
    while ((line = br.readLine()) != null) {
      String[] r = line.split("\\s*,\\s*");
      res.add(line.split("\\s*,\\s*"));
    }
    return res;
  }

  private static double sum(double[] changeLinesSeries) {
    double sum = 0;
    for (double i : changeLinesSeries)
      sum += i;
    return sum;
  }
}
