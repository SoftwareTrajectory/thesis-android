package edu.hawaii.senin.trajectory.android.persistence;

public class ChangeTarget {
  private Integer change_id;
  private String target;
  private boolean added;
  private boolean edited;
  private boolean deleted;
  private boolean renamed;
  private boolean copied;
  private Integer added_lines;
  private Integer edited_lines;
  private Integer deleted_lines;

  public Integer getChange_id() {
    return change_id;
  }

  public void setChange_id(Integer change_id) {
    this.change_id = change_id;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public boolean isAdded() {
    return added;
  }

  public void setAdded(boolean added) {
    this.added = added;
  }

  public boolean isEdited() {
    return edited;
  }

  public void setEdited(boolean edited) {
    this.edited = edited;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public boolean isRenamed() {
    return renamed;
  }

  public void setRenamed(boolean renamed) {
    this.renamed = renamed;
  }

  public boolean isCopied() {
    return copied;
  }

  public void setCopied(boolean copied) {
    this.copied = copied;
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

  public Integer getDeleted_lines() {
    return deleted_lines;
  }

  public void setDeleted_lines(Integer deleted_lines) {
    this.deleted_lines = deleted_lines;
  }

  @Override
  public String toString() {
    return this.target;
  }

}
