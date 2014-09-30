package edu.hawaii.senin.trajectory.android.builder;

import java.math.BigDecimal;
import java.util.HashMap;

public class SingleWeekRecord {

  private static final int SIZE = 7;

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

  public SingleWeekRecord() {
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
    Integer dow = ((Long) res.get("dow")).intValue();

    commitsSeries[dow] = ((Long) res.get("changes")).doubleValue();

    addTargetSeries[dow] = ((BigDecimal) res.get("added_files")).doubleValue();
    editTargetSeries[dow] = ((BigDecimal) res.get("edited_files")).doubleValue();
    deleteTargetSeries[dow] = ((BigDecimal) res.get("removed_files")).doubleValue();
    renamedTargetSeries[dow] = ((BigDecimal) res.get("renamed_files")).doubleValue();
    copiedTargetSeries[dow] = ((BigDecimal) res.get("copied_files")).doubleValue();

    changeTargetSeries[dow] = addTargetSeries[dow] + editTargetSeries[dow]
        + deleteTargetSeries[dow] + renamedTargetSeries[dow] + copiedTargetSeries[dow];

    addLinesSeries[dow] = ((BigDecimal) res.get("added_lines")).doubleValue();
    editLinesSeries[dow] = ((BigDecimal) res.get("removed_lines")).doubleValue();
    deleteLinesSeries[dow] = ((BigDecimal) res.get("edited_files")).doubleValue();

    changeLinesSeries[dow] = addLinesSeries[dow] + editLinesSeries[dow]
        + deleteLinesSeries[dow];
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
