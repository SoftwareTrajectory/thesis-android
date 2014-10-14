package edu.hawaii.senin.trajectory.android.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import edu.hawaii.jmotif.timeseries.TSUtils;

/**
 * Little workflow helpers.
 * 
 * @author psenin
 * 
 */
public class WorkflowUtils {

  public static double[] processPeaks(double[] sumSeries) {
    double sd = TSUtils.stDev(sumSeries);
    for (int i = 0; i < sumSeries.length; i++) {
      if (sumSeries[i] > 2. * sd) {
        sumSeries[i] = 2. * sd;
      }
    }
    return sumSeries;
  }

  public static double[] sumSeries(double[] sumSeries, double[] series) {
    for (int i = 0; i < sumSeries.length; i++) {
      sumSeries[i] = sumSeries[i] + series[i];
    }
    return sumSeries;
  }

  public static ArrayList<String[]> readCSV(String fname) throws IOException {
    ArrayList<String[]> res = new ArrayList<String[]>();
    String line = null;
    BufferedReader br = new BufferedReader(new FileReader(new File(fname)));
    while ((line = br.readLine()) != null) {
      res.add(line.split("\\s*,\\s*"));
    }
    br.close();
    return res;
  }

  public static double sum(double[] changeLinesSeries) {
    double sum = 0;
    for (double i : changeLinesSeries)
      sum += i;
    return sum;
  }

}
