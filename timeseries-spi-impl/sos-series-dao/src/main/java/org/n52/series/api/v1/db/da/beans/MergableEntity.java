
package org.n52.series.api.v1.db.da.beans;

import java.util.Set;

public interface MergableEntity {

    String getMergeRole();

    void setMergeRole(String mergeRole);

    Set<Long> getMergablePkids();

}
