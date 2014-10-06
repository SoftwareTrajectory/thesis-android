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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;
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
import edu.hawaii.senin.trajectory.android.util.MapEntry;

/**
 * The first step of the SAT workflow, prints software trajectories.
 * 
 * @author psenin
 * 
 */
public class Step01PrintReleaseSeries {

  // this is the project name
  //
  private static final String PROJECT_OF_INTEREST = "OMAP";

  // this is the window size that shall be considered before and after the release date
  //
  private static final int DAYS = 28;

  // this is the set of metrics we'll be retrieving data for
  //
  private static final String[] METRICS_OF_INTEREST = { "added_files", "edited_files",
      "removed_files", "added_lines", "edited_lines", "removed_lines" };

  // this is the output prefix
  //
  private static final String OUTPUT_PREFIX = "results/release_" + String.valueOf(DAYS);

  private static TimeZone tz;

  // logger business
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(Step01PrintReleaseSeries.class);
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

    // this is a set of release we are working on
    //
    Map<DateTime, String> releases = AndroidEvolution.getReleasesAsMap();
    ArrayList<DateTime> myDates = new ArrayList<DateTime>();
    myDates.addAll(releases.keySet());
    Collections.sort(myDates);
    consoleLogger.info("Dates considered: " + myDates);

    // Acquire the DB connection
    OMAPDB db = OMAPDBManager.getProductionInstance();

    // get the project
    ChangeProject project = db.getProject(PROJECT_OF_INTEREST);
    if (null == project) {
      System.exit(-10);
    }
    consoleLogger.info("project found: " + project.getName());

    // make the result data arrays
    //
    ArrayList<Entry<String, double[]>> preRelease = new ArrayList<Entry<String, double[]>>();
    ArrayList<Entry<String, double[]>> postRelease = new ArrayList<Entry<String, double[]>>();

    // looping over selected metrics
    //
    for (String metric : METRICS_OF_INTEREST) {

      // looping over selected dates
      //
      int counter = 1;
      for (DateTime releaseDate : myDates) {

        String s = releases.get(releaseDate);

        // dates business
        //
        DateTime releaseMonday = releaseDate.withDayOfWeek(DateTimeConstants.MONDAY);

        DateTime postReleaseStartMonday = releaseMonday.plusDays(7);
        DateTime preReleaseStartMonday = releaseMonday.minusDays(DAYS);

        // POST SERIES
        {
          // get the list of users
          //
          LocalDateTime start = postReleaseStartMonday.toLocalDateTime();
          LocalDateTime end = postReleaseStartMonday.plusDays(DAYS).toLocalDateTime();
          List<ChangePeople> postUsers = db.getAndroidAuthorsForInterval(project.getId(), null,
              start, end, metric);
          consoleLogger
              .info("POST dates: " + start + ", " + end + ", authors: " + postUsers.size());

          // extract series
          //
          for (ChangePeople cp : postUsers) {
            double[] series = db.getAndroidAuthorMetricAsSeries(project.getId(), cp.getId(),
                metric, start, end);
            if (TSUtils.mean(series) == 0d) {
              continue;
            }
            System.out.println("# " + counter + ", " + s + "-post, " + cp.getEmail() + ", "
                + postReleaseStartMonday + ", " + postReleaseStartMonday.plusDays(DAYS) + ", "
                + Arrays.toString(series));
            postRelease.add(new MapEntry<String, double[]>(s + ", " + counter + "-post, "
                + cp.getEmail() + ", " + postReleaseStartMonday + ", "
                + postReleaseStartMonday.plusDays(DAYS), series));
          }

        }

        // PRE SERIES
        {
          // get the list of users
          //
          LocalDateTime start = preReleaseStartMonday.toLocalDateTime();
          LocalDateTime end = preReleaseStartMonday.plusDays(DAYS).toLocalDateTime();
          List<ChangePeople> preUsers = db.getAndroidAuthorsForInterval(project.getId(), null,
              start, end, metric);
          consoleLogger.info("PRE dates: " + start + ", " + end + ", authors: " + preUsers.size());

          // extract series
          //
          for (ChangePeople cp : preUsers) {
            double[] series = db.getAndroidAuthorMetricAsSeries(project.getId(), cp.getId(),
                metric, start, end);
            if (TSUtils.mean(series) == 0d) {
              continue;
            }
            System.out.println("# " + counter + ", " + s + "-pre, " + cp.getEmail() + ", "
                + postReleaseStartMonday + ", " + postReleaseStartMonday.plusDays(DAYS) + ", "
                + Arrays.toString(series));
            postRelease.add(new MapEntry<String, double[]>(s + ", " + counter + "-pre, "
                + cp.getEmail() + ", " + postReleaseStartMonday + ", "
                + postReleaseStartMonday.plusDays(DAYS), series));
          }
        }

        counter++;

      }

      saveSet(preRelease, postRelease, OUTPUT_PREFIX + "_" + metric);
    }

  }

  private static void saveSet(ArrayList<Entry<String, double[]>> preRelease,
      ArrayList<Entry<String, double[]>> postRelease, String outputPrefix) throws IOException {

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPrefix + ".csv")));

    for (Entry<String, double[]> s : preRelease) {
      if (TSUtils.mean(s.getValue()) == 0) {
        continue;
      }
      bw.write("pre_" + s.getKey() + ", "
          + Arrays.toString(s.getValue()).replace("[", "").replace("]", "").replace(",", "") + "\n");
    }

    for (Entry<String, double[]> s : postRelease) {
      if (TSUtils.mean(s.getValue()) == 0) {
        continue;
      }
      bw.write("post_" + s.getKey() + ", "
          + Arrays.toString(s.getValue()).replace("[", "").replace("]", "").replace(",", "") + "\n");
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
