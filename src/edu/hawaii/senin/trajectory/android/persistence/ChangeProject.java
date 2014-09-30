package edu.hawaii.senin.trajectory.android.persistence;

public class ChangeProject {
  private Integer id;
  private String name;
  private String local_path;
  private String retrieved;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocal_path() {
    return local_path;
  }

  public void setLocal_path(String local_path) {
    this.local_path = local_path;
  }

  public String getRetrieved() {
    return retrieved;
  }

  public void setRetrieved(String retrieved) {
    this.retrieved = retrieved;
  }

}
