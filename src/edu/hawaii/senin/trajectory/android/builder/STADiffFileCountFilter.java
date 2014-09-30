package edu.hawaii.senin.trajectory.android.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.gitective.core.filter.commit.CommitDiffFilter;
import org.gitective.core.filter.commit.CommitFilter;
import org.gitective.core.filter.commit.DiffFileCountFilter;

public class STADiffFileCountFilter extends CommitDiffFilter {

  private long added;
  private Map<AbbreviatedObjectId, DiffEntry> addedTargets = new HashMap<AbbreviatedObjectId, DiffEntry>();

  private long modified;
  private Map<AbbreviatedObjectId, DiffEntry> modifiedTargets = new HashMap<AbbreviatedObjectId, DiffEntry>();

  private long deleted;
  private Map<AbbreviatedObjectId, DiffEntry> deletedTargets = new HashMap<AbbreviatedObjectId, DiffEntry>();

  private long renamed;
  private Map<AbbreviatedObjectId, DiffEntry> renamedTargets = new HashMap<AbbreviatedObjectId, DiffEntry>();

  private long copied;
  private Map<AbbreviatedObjectId, DiffEntry> copiedTargets = new HashMap<AbbreviatedObjectId, DiffEntry>();

  public STADiffFileCountFilter() {
    super(true);
  }

  /**
   * @return added
   */
  public long getAdded() {
    return added;
  }

  /**
   * @return edited
   */
  public long getModified() {
    return modified;
  }

  /**
   * @return deleted
   */
  public long getDeleted() {
    return deleted;
  }

  /**
   * @return deleted
   */
  public long getRenamed() {
    return renamed;
  }

  /**
   * @return deleted
   */
  public long getCopied() {
    return copied;
  }

  @Override
  public boolean include(RevCommit commit, Collection<DiffEntry> diffs) {
    for (DiffEntry diff : diffs)
      switch (diff.getChangeType()) {
      case ADD: {
        added++;
        addedTargets.put(diff.getNewId(), diff);
      }
        break;
      case MODIFY: {
        modified++;
        modifiedTargets.put(diff.getNewId(), diff);
      }
        break;
      case DELETE: {
        deleted++;
        deletedTargets.put(diff.getOldId(), diff);
      }
        break;
      case RENAME: {
        renamed++;
        renamedTargets.put(diff.getOldId(), diff);
      }
        break;
      case COPY: {
        copied++;
        copiedTargets.put(diff.getOldId(), diff);
      }
        break;
      }
    return true;
  }

  @Override
  public CommitFilter reset() {
    added = 0;
    modified = 0;
    deleted = 0;
    renamed = 0;
    copied = 0;
    return super.reset();
  }

  @Override
  public RevFilter clone() {
    return new DiffFileCountFilter();
  }

  public Map<AbbreviatedObjectId, DiffEntry> getAddedTargets() {
    return this.addedTargets;
  }

  public Map<AbbreviatedObjectId, DiffEntry> getDeletedTargets() {
    return this.deletedTargets;
  }

  public Map<AbbreviatedObjectId, DiffEntry> getModifiedTargets() {
    return this.modifiedTargets;
  }

  public Map<AbbreviatedObjectId, DiffEntry> getRenamedTargets() {
    return this.renamedTargets;
  }

  public Map<AbbreviatedObjectId, DiffEntry> getCopiedTargets() {
    return this.copiedTargets;
  }
}
