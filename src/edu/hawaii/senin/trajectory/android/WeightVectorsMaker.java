package edu.hawaii.senin.trajectory.android;

import java.util.HashMap;
import java.util.Map.Entry;

public class WeightVectorsMaker {

  private static HashMap<Integer, ReleaseRecord> releases;

  private static int[] RELEASES_OF_INTEREST = { 38, 36, 34 };

  public static void main(String[] args) {

    releases = ReleaseFactory.getReleases();

    for (int rId : RELEASES_OF_INTEREST) {
      ReleaseRecord rr = releases.get(rId);
      System.out.println(rr.getName());
    }

  }
}
