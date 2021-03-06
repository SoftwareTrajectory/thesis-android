package edu.hawaii.senin.trajectory.android.builder;

import java.math.BigDecimal;
import java.util.HashMap;

public class SingleDayRecord {

  private static final int SIZE = 24;

  private double[] commitsSeries;

  private double[] changeTargetSeries;
  private double[] addTargetSeries;
  private double[] editTargetSeries;
  private double[] deleteTargetSeries;
  private double[] renamedTargetSeries;
  private double[] copiedTargetSeries;

  private double[] changeLinesSeries;
  private double[] addLinesSeries;
  private double[] editLinesSeries;
  private double[] deleteLinesSeries;

  public SingleDayRecord() {
    commitsSeries = new double[SIZE];

    changeTargetSeries = new double[SIZE];
    addTargetSeries = new double[SIZE];
    editTargetSeries = new double[SIZE];
    deleteTargetSeries = new double[SIZE];
    renamedTargetSeries = new double[SIZE];
    copiedTargetSeries = new double[SIZE];

    changeLinesSeries = new double[SIZE];
    addLinesSeries = new double[SIZE];
    editLinesSeries = new double[SIZE];
    deleteLinesSeries = new double[SIZE];

  }

  public void put(HashMap<String, Object> res) {
    Integer hour = Integer.valueOf((String) res.get("hour"));

    commitsSeries[hour] = ((Long) res.get("changes")).doubleValue();

    addTargetSeries[hour] = ((BigDecimal) res.get("added_files")).doubleValue();
    editTargetSeries[hour] = ((BigDecimal) res.get("edited_files")).doubleValue();
    deleteTargetSeries[hour] = ((BigDecimal) res.get("removed_files")).doubleValue();
    renamedTargetSeries[hour] = ((BigDecimal) res.get("renamed_files")).doubleValue();
    copiedTargetSeries[hour] = ((BigDecimal) res.get("copied_files")).doubleValue();

    changeTargetSeries[hour] = addTargetSeries[hour] + editTargetSeries[hour]
        + deleteTargetSeries[hour] + renamedTargetSeries[hour] + copiedTargetSeries[hour];

    addLinesSeries[hour] = ((BigDecimal) res.get("added_lines")).doubleValue();
    editLinesSeries[hour] = ((BigDecimal) res.get("removed_lines")).doubleValue();
    deleteLinesSeries[hour] = ((BigDecimal) res.get("edited_files")).doubleValue();

    changeLinesSeries[hour] = addLinesSeries[hour] + editLinesSeries[hour]
        + deleteLinesSeries[hour];
  }

  public double[] getCommitsSeries() {
    return commitsSeries;
  }

  public double[] getChangeTargetSeries() {
    return changeTargetSeries;
  }

  public double[] getAddTargetSeries() {
    return addTargetSeries;
  }

  public double[] getEditTargetSeries() {
    return editTargetSeries;
  }

  public double[] getDeleteTargetSeries() {
    return deleteTargetSeries;
  }

  public double[] getRenamedTargetSeries() {
    return renamedTargetSeries;
  }

  public double[] getCopiedTargetSeries() {
    return copiedTargetSeries;
  }

  public double[] getChangeLinesSeries() {
    return changeLinesSeries;
  }

  public double[] getAddLinesSeries() {
    return addLinesSeries;
  }

  public double[] getEditLinesSeries() {
    return editLinesSeries;
  }

  public double[] getDeleteLinesSeries() {
    return deleteLinesSeries;
  }

}
