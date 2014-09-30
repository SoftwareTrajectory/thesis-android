package edu.hawaii.senin.trajectory.android.persistence;

import org.joda.time.LocalDate;

public class SaxSeries {
  private LocalDate day;
  private String tag;
  private String params;
  private Integer author_id;
  private Integer project_id;
  private String commits;
  private String targets;
  private String lines;
  private String targets_added;
  private String targets_edited;
  private String targets_deleted;
  private String targets_renamed;
  private String targets_copied;
  private String lines_added;
  private String lines_edited;
  private String lines_deleted;

  public LocalDate getDay() {
    return day;
  }

  public void setDay(LocalDate day) {
    this.day = day;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  public Integer getAuthor_id() {
    return author_id;
  }

  public void setAuthor_id(Integer author_id) {
    this.author_id = author_id;
  }

  public Integer getProject_id() {
    return project_id;
  }

  public void setProject_id(Integer project_id) {
    this.project_id = project_id;
  }

  public String getCommits() {
    return commits;
  }

  public void setCommits(String commits) {
    this.commits = commits;
  }

  public String getTargets() {
    return targets;
  }

  public void setTargets(String targets) {
    this.targets = targets;
  }

  public String getLines() {
    return lines;
  }

  public void setLines(String lines) {
    this.lines = lines;
  }

  public String getTargets_added() {
    return targets_added;
  }

  public void setTargets_added(String targets_added) {
    this.targets_added = targets_added;
  }

  public String getTargets_edited() {
    return targets_edited;
  }

  public void setTargets_edited(String targets_edited) {
    this.targets_edited = targets_edited;
  }

  public String getTargets_deleted() {
    return targets_deleted;
  }

  public void setTargets_deleted(String targets_deleted) {
    this.targets_deleted = targets_deleted;
  }

  public String getTargets_renamed() {
    return targets_renamed;
  }

  public void setTargets_renamed(String targets_renamed) {
    this.targets_renamed = targets_renamed;
  }

  public String getTargets_copied() {
    return targets_copied;
  }

  public void setTargets_copied(String targets_copied) {
    this.targets_copied = targets_copied;
  }

  public String getLines_added() {
    return lines_added;
  }

  public void setLines_added(String lines_added) {
    this.lines_added = lines_added;
  }

  public String getLines_edited() {
    return lines_edited;
  }

  public void setLines_edited(String lines_edited) {
    this.lines_edited = lines_edited;
  }

  public String getLines_deleted() {
    return lines_deleted;
  }

  public void setLines_deleted(String lines_deleted) {
    this.lines_deleted = lines_deleted;
  }

}
