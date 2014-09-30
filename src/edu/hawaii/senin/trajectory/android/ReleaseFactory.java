package edu.hawaii.senin.trajectory.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ReleaseFactory {

  private static HashMap<Integer, ReleaseRecord> releases;

  static {
    
    releases = new HashMap<Integer, ReleaseRecord>();
    try {
      BufferedReader bw = new BufferedReader(new FileReader(new File("data/android_releases.csv")));
      String str = null;
      int counter = 1;
      while (null != (str = bw.readLine())) {
        String[] strSplit = str.split(",");
        ReleaseRecord rr = new ReleaseRecord(counter, strSplit[0].trim(), strSplit[1].trim()
            .replace("\"", ""));
        releases.put(rr.getId(), rr);
        counter++;
      }
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static HashMap<Integer, ReleaseRecord> getReleases() {
    return releases;
  }
}
