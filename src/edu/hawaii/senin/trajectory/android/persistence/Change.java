package edu.hawaii.senin.trajectory.android.persistence;

import org.joda.time.LocalDateTime;

public class Change {

  private Integer id;
  private Integer project_id;
  private String commit_hash;
  private String tree_hash;
  private LocalDateTime utc_time;

  private Integer author_id;
  private LocalDateTime author_date;

  private Integer committer_id;
  private LocalDateTime committer_date;

  private String subject;

  private Integer added_files;
  private Integer edited_files;
  private Integer removed_files;
  private Integer renamed_files;
  private Integer copied_files;
  private Integer added_lines;
  private Integer edited_lines;
  private Integer removed_lines;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getProject_id() {
    return project_id;
  }

  public void setProject_id(Integer project_id) {
    this.project_id = project_id;
  }

  public String getCommit_hash() {
    return commit_hash;
  }

  public void setCommit_hash(String commit_hash) {
    this.commit_hash = commit_hash;
  }

  public String getTree_hash() {
    return tree_hash;
  }

  public void setTree_hash(String tree_hash) {
    this.tree_hash = tree_hash;
  }

  public LocalDateTime getUtc_time() {
    return utc_time;
  }

  public void setUtc_time(LocalDateTime utc_time) {
    this.utc_time = utc_time;
  }

  public Integer getAuthor_id() {
    return author_id;
  }

  public void setAuthor_id(Integer author_id) {
    this.author_id = author_id;
  }

  public LocalDateTime getAuthor_date() {
    return author_date;
  }

  public void setAuthor_date(LocalDateTime author_date) {
    this.author_date = author_date;
  }

  public Integer getCommitter_id() {
    return committer_id;
  }

  public void setCommitter_id(Integer committer_id) {
    this.committer_id = committer_id;
  }

  public LocalDateTime getCommitter_date() {
    return committer_date;
  }

  public void setCommitter_date(LocalDateTime committer_date) {
    this.committer_date = committer_date;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public Integer getAdded_files() {
    return added_files;
  }

  public void setAdded_files(Integer added_files) {
    this.added_files = added_files;
  }

  public Integer getEdited_files() {
    return edited_files;
  }

  public void setEdited_files(Integer edited_files) {
    this.edited_files = edited_files;
  }

  public Integer getRemoved_files() {
    return removed_files;
  }

  public void setRemoved_files(Integer removed_files) {
    this.removed_files = removed_files;
  }

  public Integer getRenamed_files() {
    return renamed_files;
  }

  public void setRenamed_files(Integer renamed_files) {
    this.renamed_files = renamed_files;
  }

  public Integer getCopied_files() {
    return copied_files;
  }

  public void setCopied_files(Integer copied_files) {
    this.copied_files = copied_files;
  }

  public Integer getAdded_lines() {
    return added_lines;
  }

  public void setAdded_lines(Integer added_lines) {
    this.added_lines = added_lines;
  }

  public Integer getEdited_lines() {
    return edited_lines;
  }

  public void setEdited_lines(Integer edited_lines) {
    this.edited_lines = edited_lines;
  }

  public Integer getRemoved_lines() {
    return removed_lines;
  }

  public void setRemoved_lines(Integer removed_lines) {
    this.removed_lines = removed_lines;
  }

}