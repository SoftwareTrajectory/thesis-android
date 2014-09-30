package edu.hawaii.senin.trajectory.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ReleaseFactory {

  private static HashMap<Integer, ReleaseRecord> releases;

  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {

    consoleLogger = (Logger) LoggerFactory.getLogger(ReleaseFactory.class);
    consoleLogger.setLevel(LOGGING_LEVEL);

    consoleLogger.info("reading release table");

    releases = new HashMap<Integer, ReleaseRecord>();
    try {
      BufferedReader bw = new BufferedReader(new FileReader(new File("data/android_releases.csv")));
      String str = null;
      int counter = 1;
      while (null != (str = bw.readLine()) && str.replaceAll("\\s+", "").length() > 0) {
        String[] strSplit = str.split(",");
        ReleaseRecord rr = new ReleaseRecord(counter, strSplit[0].trim(), strSplit[1].trim()
            .replace("\"", ""));
        releases.put(rr.getId(), rr);
        counter++;
      }
      bw.close();
      consoleLogger.info("loaded " + releases.size() + " release records.");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static HashMap<Integer, ReleaseRecord> getReleases() {
    return releases;
  }
}
