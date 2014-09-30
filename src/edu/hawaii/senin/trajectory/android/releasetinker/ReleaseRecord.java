package edu.hawaii.senin.trajectory.android.releasetinker;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ReleaseRecord {

  private DateTime timestamp;
  private String name;
  private int id;

  DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-DD");

  public ReleaseRecord(int id, String timestamp, String name) {
    this.id = id;
    this.timestamp = fmt.withZoneUTC().parseDateTime(timestamp);
    this.name = name;
  }

  public DateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(DateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

}
