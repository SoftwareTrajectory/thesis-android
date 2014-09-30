package edu.hawaii.senin.trajectory.android.db;

import java.io.IOException;

/**
 * Manages DB instances preventing multiple objects instantiation.
 * 
 * @author psenin
 * 
 */
public class OMAPDBManager {

  private static OMAPDB productionInstance;

  /**
   * Get production DB instance.
   * 
   * @return production DB instance.
   * @throws IOException if error occurs.
   */
  public static OMAPDB getProductionInstance() throws IOException {
    if (null == productionInstance) {
      productionInstance = new OMAPDB(OMAPDB.PRODUCTION_INSTANCE);
    }
    return productionInstance;
  }

}
