package edu.hawaii.senin.trajectory.android.builder;

import org.joda.time.Days;
import org.joda.time.LocalDateTime;

public class DateIntervalRecord implements Comparable<DateIntervalRecord> {

  private LocalDateTime start;
  private LocalDateTime end;

  private int size;

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

  public DateIntervalRecord(LocalDateTime start, LocalDateTime end) {

    this.start = new LocalDateTime(start);
    this.end = new LocalDateTime(end);

    this.size = Days.daysBetween(start, end).getDays() + 1;

    this.commitsSeries = new double[this.size];

    this.changeTargetSeries = new double[this.size];
    this.addTargetSeries = new double[this.size];
    this.editTargetSeries = new double[this.size];
    this.deleteTargetSeries = new double[this.size];
    this.renamedTargetSeries = new double[this.size];
    this.copiedTargetSeries = new double[this.size];

    this.changeLinesSeries = new double[this.size];
    this.addLinesSeries = new double[this.size];
    this.editLinesSeries = new double[this.size];
    this.deleteLinesSeries = new double[this.size];

  }

  @Override
  public int compareTo(DateIntervalRecord arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

}
