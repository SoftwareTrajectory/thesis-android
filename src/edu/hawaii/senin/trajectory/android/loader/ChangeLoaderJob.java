package edu.hawaii.senin.trajectory.android.loader;

import static java.lang.Integer.MAX_VALUE;
import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import org.apache.ibatis.session.SqlSession;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gitective.core.BlobUtils;
import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.filter.commit.AndCommitFilter;
import org.gitective.core.filter.commit.CommitLimitFilter;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.util.StackTrace;
import edu.hawaii.senin.trajectory.android.builder.STADiffFileCountFilter;
import edu.hawaii.senin.trajectory.android.db.OMAPDBManager;
import edu.hawaii.senin.trajectory.android.persistence.Change;
import edu.hawaii.senin.trajectory.android.persistence.ChangePeople;
import edu.hawaii.senin.trajectory.android.persistence.ChangeProject;
import edu.hawaii.senin.trajectory.android.persistence.ChangeTarget;

public class ChangeLoaderJob implements Callable<String> {

  private List<RevCommit> commits;
  private SqlSession session;
  private ChangeProject project;
  private FileRepository repository;

  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(ChangeLoaderJob.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public ChangeLoaderJob(ChangeProject project, FileRepository repository, List<RevCommit> commits) {
    this.project = project;
    this.repository = repository;
    this.commits = commits;
  }

  @Override
  public String call() throws Exception {
    this.session = null;
    try {
      this.session = OMAPDBManager.getProductionInstance().getSession();

      for (RevCommit commit : commits) {
        processCommit(commit);
      }

    }
    catch (Exception e) {
      return StackTrace.toString(e);
    }
    finally {
      this.session.close();
    }
    return "";
  }

  private void processCommit(RevCommit c) throws LargeObjectException, MissingObjectException,
      IncorrectObjectTypeException, IOException {
    String hash = c.getName();
    consoleLogger.debug("processing commit " + hash);
    Change changeRecord = processChange(c);
    this.session.insert("replaceChange", changeRecord);
    processChangeTargets(changeRecord);
    this.session.update("updateChange", changeRecord);
  }

  private ChangePeople processUser(String name, String email) {
    consoleLogger.debug("checking the user " + email);
    ChangePeople user = this.session.selectOne("getPeopleByEmail", email);
    if (null == user) {
      consoleLogger.info("Added to DB new user, email: " + email + ", name: " + name);
      ChangePeople p = new ChangePeople();
      p.setEmail(email);
      p.setName(name);
      session.insert("saveUser", p);
    }
    else {
      consoleLogger.debug("user record found");
    }
    user = this.session.selectOne("getPeopleByEmail", email);
    if (null == user) {
      throw new RuntimeException("Unable to save a user record");
    }
    return user;
  }

  private Change processChange(RevCommit c) {

    Change change = new Change();
    change.setProject_id(this.project.getId());

    // hashes
    //
    String commitHash = c.getName();
    String treeHash = c.getTree().getName();
    change.setCommit_hash(commitHash);
    change.setTree_hash(treeHash);

    // author & committer
    //
    String authorEmail = c.getAuthorIdent().getEmailAddress();
    ChangePeople author = processUser(c.getAuthorIdent().getName(), authorEmail);
    change.setAuthor_id(author.getId());

    String comitterEmail = c.getCommitterIdent().getEmailAddress();
    ChangePeople comitter = processUser(c.getCommitterIdent().getName(), comitterEmail);
    change.setCommitter_id(comitter.getId());

    // timestamps business
    //
    long commitTimestamp = Integer.valueOf(c.getCommitTime()).longValue() * 1000L;
    LocalDateTime utcTimeStamp = new LocalDateTime(commitTimestamp, DateTimeZone.UTC);
    change.setUtc_time(utcTimeStamp);

    long authorTime = c.getAuthorIdent().getWhen().getTime();
    TimeZone aTz = c.getAuthorIdent().getTimeZone();
    LocalDateTime authorDateTime = new LocalDateTime(authorTime, DateTimeZone.forTimeZone(aTz));
    change.setAuthor_date(authorDateTime);

    long comitterTime = c.getCommitterIdent().getWhen().getTime();
    TimeZone cTz = c.getAuthorIdent().getTimeZone();
    LocalDateTime comitterDateTime = new LocalDateTime(comitterTime, DateTimeZone.forTimeZone(cTz));
    change.setCommitter_date(comitterDateTime);

    // le message
    //
    change.setSubject(c.getFullMessage());

    return change;
  }

  private Change processChangeTargets(Change change) throws LargeObjectException,
      MissingObjectException, IncorrectObjectTypeException, IOException {

    RevCommit c = CommitUtils.getCommit(repository, change.getCommit_hash());

    consoleLogger.debug(change.getCommit_hash() + " has " + c.getParentCount() + " parent(s)");
    //
    // global counters for the change - will use them to update the record
    //
    int commitTotalFilesAdd = 0;
    int commitTotalFilesEdit = 0;
    int commitTotalFilesDelete = 0;
    int commitTotalFilesRenamed = 0;
    int commitTotalFilesCopied = 0;

    int commitTotalLinesAdd = 0;
    int commitTotalLinesEdit = 0;
    int commitTotalLinesDelete = 0;

    if (c.getParents().length > 0) {

      // get diff filter run over the CHANGE and a parent
      //
      CommitLimitFilter limitFilter = new CommitLimitFilter(1);
      STADiffFileCountFilter filter = new STADiffFileCountFilter();
      CommitFinder fileFinder = new CommitFinder(this.repository);
      fileFinder.setFilter(new AndCommitFilter(limitFilter, filter));
      // fileFinder.setFilter(new AndCommitFilter(filter, limitFilter));
      fileFinder.findFrom(c);

      commitTotalFilesAdd += filter.getAdded();
      commitTotalFilesEdit += filter.getModified();
      commitTotalFilesDelete += +filter.getDeleted();
      commitTotalFilesRenamed += +filter.getRenamed();
      commitTotalFilesCopied += +filter.getCopied();

      // check ADDED targets
      //
      if (filter.getAdded() > 0) {
        consoleLogger.debug(" added: " + map2NewPathString(filter.getAddedTargets()));

        // iterate over added files counting lines
        for (DiffEntry diff : filter.getAddedTargets().values()) {
          int addedLines = 0;
          if (RawText.isBinary(repository.open(diff.getNewId().toObjectId(), OBJ_BLOB)
              .getCachedBytes(MAX_VALUE))) {
            consoleLogger.debug(" [added] \'" + diff.getNewPath() + ", binary");
          }
          else {
            addedLines = (new RawText(repository.open(diff.getNewId().toObjectId(), OBJ_BLOB)
                .getCachedBytes(MAX_VALUE))).size();
            consoleLogger
                .debug(" [added] \'" + diff.getNewPath() + "\': added lines " + addedLines);
          }

          ChangeTarget dbTargetRecord = getTargetRecord(change.getId(), diff.getNewPath());

          if (null == dbTargetRecord) {
            String msg = " [new target record] for change " + change.getId() + ", hash: \""
                + change.getCommit_hash() + "\"" + ", [target add], newPath " + diff.getNewPath()
                + ", oldPath " + diff.getOldPath();
            consoleLogger.debug(msg);
            ChangeTarget target = new ChangeTarget();
            target.setChange_id(change.getId());
            target.setTarget(diff.getNewPath());
            target.setAdded(true);
            target.setAdded_lines(addedLines);
            saveTargetRecord(target);
          }
          else {
            dbTargetRecord.setAdded(true);
            dbTargetRecord.setAdded_lines(addedLines);
            updateTargetRecord(dbTargetRecord);
          }
          commitTotalLinesAdd += addedLines;
        }
      }

      // check MODIFIED tagets
      //
      if (filter.getModified() > 0) {
        consoleLogger.debug(" modified: " + map2NewPathString(filter.getModifiedTargets()));
        for (DiffEntry diff : filter.getModifiedTargets().values()) {
          int addedLines = 0;
          int editedLines = 0;
          int deletedLines = 0;
          if (null != diff.getOldId() && null != diff.getNewId()) {
            for (Edit hunk : BlobUtils.diff(repository, diff.getOldId().toObjectId(), diff
                .getNewId().toObjectId()))
              switch (hunk.getType()) {
              case DELETE:
                deletedLines += hunk.getLengthA();
                break;
              case INSERT:
                addedLines += hunk.getLengthB();
                break;
              case REPLACE:
                editedLines += hunk.getLengthB();
                break;
              case EMPTY:
                break;
              }
            consoleLogger
                .debug(" [modified] \'" + diff.getNewPath() + "\': added lines " + addedLines
                    + ", changed lines " + editedLines + ", deleted lines " + deletedLines);

            ChangeTarget dbTargetRecord = getTargetRecord(change.getId(), diff.getNewPath());

            if (null == dbTargetRecord) {
              String msg = " [new target record] for change " + change.getId() + ", hash: \""
                  + change.getCommit_hash() + "\"" + ", [target modify], newPath "
                  + diff.getNewPath() + ", oldPath " + diff.getOldPath();
              consoleLogger.debug(msg);
              ChangeTarget target = new ChangeTarget();
              target.setChange_id(change.getId());
              target.setTarget(diff.getNewPath());
              target.setEdited(true);
              target.setAdded_lines(addedLines);
              target.setDeleted_lines(deletedLines);
              target.setEdited_lines(editedLines);
              saveTargetRecord(target);
            }
            else {
              dbTargetRecord.setEdited(true);
              dbTargetRecord.setAdded_lines(addedLines);
              dbTargetRecord.setDeleted_lines(deletedLines);
              dbTargetRecord.setEdited_lines(editedLines);
              updateTargetRecord(dbTargetRecord);
            }
            commitTotalLinesAdd += addedLines;
            commitTotalLinesEdit += editedLines;
            commitTotalLinesDelete += deletedLines;
          }
          else {
            String msg = " Marked as modified, "
                + "but one of Ids is null, skipped !\ncommit hash: " + c.getName()
                + "\ncommit target old path " + diff.getOldPath() + ", target new path "
                + diff.getNewPath();
            consoleLogger.error("#$%*" + msg);
          }
        }
      }

      // process DELETED files
      //
      if (filter.getDeleted() > 0) {
        consoleLogger.debug(" deleted: " + map2OldPathString(filter.getDeletedTargets()));
        for (DiffEntry diff : filter.getDeletedTargets().values()) {

          int deletedLines = 0;
          if (RawText.isBinary(this.repository.open(diff.getOldId().toObjectId(), OBJ_BLOB)
              .getCachedBytes(MAX_VALUE))) {
            consoleLogger.debug("  [deleted] \'" + diff.getNewPath() + ", binary");
          }
          else {
            deletedLines = (new RawText(repository.open(diff.getOldId().toObjectId(), OBJ_BLOB)
                .getCachedBytes(MAX_VALUE))).size();
            consoleLogger.debug("  [deleted] \'" + diff.getNewPath() + "\': added lines "
                + deletedLines);
          }
          ChangeTarget dbTargetRecord = getTargetRecord(change.getId(), diff.getOldPath());

          if (null == dbTargetRecord) {
            String msg = " [new target record] for change " + change.getId() + ", hash: \""
                + change.getCommit_hash() + "\"" + ", newPath " + diff.getNewPath() + ", oldPath "
                + diff.getOldPath();
            consoleLogger.debug(msg);
            ChangeTarget target = new ChangeTarget();
            target.setChange_id(change.getId());
            target.setTarget(diff.getOldPath());
            target.setDeleted(true);
            target.setDeleted_lines(deletedLines);
            saveTargetRecord(target);
          }
          else {
            dbTargetRecord.setDeleted(true);
            dbTargetRecord.setDeleted_lines(deletedLines);
            updateTargetRecord(dbTargetRecord);
          }
          commitTotalLinesDelete += deletedLines;
        }
      }

      // Renamed files
      //
      if (filter.getRenamed() > 0) {
        consoleLogger.info(" renamed: " + map2OldPathString(filter.getRenamedTargets()));
        for (DiffEntry diff : filter.getRenamedTargets().values()) {

          ChangeTarget dbTargetRecord = getTargetRecord(change.getId(), diff.getOldPath());

          if (null == dbTargetRecord) {
            String msg = " [new target record] for change " + change.getId() + ", hash: \""
                + change.getCommit_hash() + "\"" + ", newPath " + diff.getNewPath() + ", oldPath "
                + diff.getOldPath();
            consoleLogger.info(msg);
            ChangeTarget target = new ChangeTarget();
            target.setChange_id(change.getId());
            target.setTarget(diff.getOldPath());
            target.setRenamed(true);
            saveTargetRecord(target);
          }
          else {
            dbTargetRecord.setRenamed(true);
            updateTargetRecord(dbTargetRecord);
          }
        }
      }

      if (filter.getCopied() > 0) {
        consoleLogger.info(" copied: " + map2OldPathString(filter.getCopiedTargets()));
        for (DiffEntry diff : filter.getCopiedTargets().values()) {

          ChangeTarget dbTargetRecord = getTargetRecord(change.getId(), diff.getOldPath());

          if (null == dbTargetRecord) {
            String msg = " [new target record] for change " + change.getId() + ", hash: \""
                + change.getCommit_hash() + "\"" + ", newPath " + diff.getNewPath() + ", oldPath "
                + diff.getOldPath();
            consoleLogger.info(msg);
            ChangeTarget target = new ChangeTarget();
            target.setChange_id(change.getId());
            target.setTarget(diff.getOldPath());
            target.setCopied(true);
            saveTargetRecord(target);
          }
          else {
            dbTargetRecord.setRenamed(true);
            updateTargetRecord(dbTargetRecord);
          }
        }
      }

    }

    change.setAdded_files(commitTotalFilesAdd);
    change.setEdited_files(commitTotalFilesEdit);
    change.setRemoved_files(commitTotalFilesDelete);
    change.setRenamed_files(commitTotalFilesRenamed);
    change.setCopied_files(commitTotalFilesCopied);

    change.setAdded_lines(commitTotalLinesAdd);
    change.setEdited_lines(commitTotalLinesEdit);
    change.setRemoved_lines(commitTotalLinesDelete);

    return change;
  }

  private void updateTargetRecord(ChangeTarget dbTargetRecord) {
    this.session.update("saveTarget", dbTargetRecord);
  }

  private void saveTargetRecord(ChangeTarget target) {
    this.session.insert("saveTarget", target);
  }

  private ChangeTarget getTargetRecord(Integer changeId, String newPath) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("change_id", changeId);
    params.put("target", newPath);
    ChangeTarget res = null;
    try {
      res = this.session.selectOne("getChangeTarget", params);
    }
    catch (Exception e) {
      System.out.println("change: " + changeId + ", target: " + newPath);
      throw new RuntimeException(StackTrace.toString(e));
    }
    return res;
  }

  private static String map2NewPathString(Map<AbbreviatedObjectId, DiffEntry> diffMap) {
    StringBuilder sb = new StringBuilder();
    for (Entry<AbbreviatedObjectId, DiffEntry> entry : diffMap.entrySet()) {
      sb.append("[" + entry.getKey().name() + ", " + entry.getValue().getNewPath() + "], ");
    }
    if (sb.length() > 0) {
      sb.delete(sb.length() - 2, sb.length());
    }
    return sb.toString();
  }

  private static String map2OldPathString(Map<AbbreviatedObjectId, DiffEntry> diffMap) {
    StringBuilder sb = new StringBuilder();
    for (Entry<AbbreviatedObjectId, DiffEntry> entry : diffMap.entrySet()) {
      sb.append("[" + entry.getKey().name() + ", " + entry.getValue().getOldPath() + "], ");
    }
    if (sb.length() > 0) {
      sb.delete(sb.length() - 2, sb.length());
    }
    return sb.toString();
  }

}
