package edu.hawaii.senin.trajectory.android.workflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.saxvsm.UCRUtils;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.text.cluster.RandomStartStrategy;
import edu.hawaii.jmotif.text.cluster.TextKMeans;
import edu.hawaii.jmotif.timeseries.TSException;

public class Step04ClusterRelease {

  // data
  //
  private static final String TRAINING_DATA = "results/135_el_train.csv";

  // prefix for all of the output
  private static final String PREFIX = "/home/psenin/dendroscope/";

  // SAX parameters to use
  //
  public static final int WINDOW_SIZE = 24;
  public static final int PAA_SIZE = 5;
  public static final int ALPHABET_SIZE = 7;
  public static final SAXCollectionStrategy STRATEGY = SAXCollectionStrategy.CLASSIC;

  private static final String OUT0_FILENAME = "results/135_vector0.csv";
  private static final String OUT1_FILENAME = "results/135_vector1.csv";

  // logger business
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(Step01PrintReleaseSeries.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * @param args
   * @throws IOException
   * @throws TSException
   * @throws IndexOutOfBoundsException
   */
  public static void main(String[] args) throws IOException, IndexOutOfBoundsException, TSException {

    int[] params = new int[4];
    params[0] = WINDOW_SIZE;
    params[1] = PAA_SIZE;
    params[2] = ALPHABET_SIZE;
    params[3] = STRATEGY.index();

    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(TRAINING_DATA);
    consoleLogger.info("trainData classes: " + trainData.size() + ", series length: "
        + trainData.entrySet().iterator().next().getValue().get(0).length);
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      consoleLogger.info(" training class: " + e.getKey() + " series: " + e.getValue().size());
    }

    // make a map of resulting bags
    List<WordBag> bags = new ArrayList<WordBag>();

    // process series one by one building word bags
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {

      String classLabel = e.getKey();
      WordBag classBag = new WordBag(classLabel);

      for (double[] series : e.getValue()) {
        WordBag tmp = TextUtils.seriesToWordBag("tmp", series, params);
        classBag.mergeWith(tmp);
      }

      bags.add(classBag);
      System.out.println(classBag.getLabel() + "\n" + classBag.toColumn());
    }

    // create the TFIDF data structure
    HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);
    tfidf = TextUtils.normalizeToUnitVectors(tfidf);
    System.out.println(TextUtils.tfidfToTable(tfidf));

    // launch HC to see the dendogram
    // Cluster clusters = HC.Hc(tfidf, LinkageCriterion.COMPLETE);
    // System.out.println((new CosineDistanceMatrix(tfidf)).toString());
    // BufferedWriter bw = new BufferedWriter(new FileWriter(PREFIX + "test2.newick"));
    // bw.write("(" + clusters.toNewick() + ")");
    // bw.close();

    // launch KMeans with random centers
    @SuppressWarnings("unused")
    HashMap<String, List<String>> clustersK = TextKMeans.cluster(tfidf, 2,
        new RandomStartStrategy());

    HashMap<String, Double> vector1 = TextKMeans.computeCentroid(tfidf, clustersK.get("0"));
    saveWeightVector(vector1, OUT0_FILENAME);

    HashMap<String, Double> vector2 = TextKMeans.computeCentroid(tfidf, clustersK.get("1"));
    saveWeightVector(vector2, OUT1_FILENAME);

    // write down tf*idf vectors for each class
    // writePreClusterTable(tfidf, PREFIX + "cylinder-bell-funnel.csv");
  }

  private static void saveWeightVector(HashMap<String, Double> vector, String fname)
      throws IOException {
    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fname)));
    for (Entry<String, Double> e : vector.entrySet()) {
      bw.write(e.getKey() + " " + e.getValue() + "\n");
    }
    bw.close();
  }

}
