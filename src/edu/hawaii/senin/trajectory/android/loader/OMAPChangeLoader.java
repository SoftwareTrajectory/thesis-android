package edu.hawaii.senin.trajectory.android.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.filter.commit.AndCommitFilter;
import org.gitective.core.filter.commit.CommitCursorFilter;
import org.gitective.core.filter.commit.CommitFilter;
import org.gitective.core.filter.commit.CommitLimitFilter;
import org.gitective.core.filter.commit.CommitListFilter;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.util.StackTrace;
import edu.hawaii.senin.trajectory.android.db.OMAPDB;
import edu.hawaii.senin.trajectory.android.db.OMAPDBManager;
import edu.hawaii.senin.trajectory.android.persistence.ChangeProject;

/**
 * This is the runner. It will load all the POSTGRE data into DB.
 * 
 * @author psenin
 * 
 */
public class OMAPChangeLoader {

  private static final String[] branches = { "remotes/origin/android-omap-3.0",
      "remotes/origin/android-omap-panda-3.0", "remotes/origin/android-omap-steelhead-3.0-ics-aah",
      "remotes/origin/android-omap-tuna-3.0", "remotes/origin/android-omap-tuna-3.0-ics-mr1",
      "remotes/origin/android-omap-tuna-3.0-jb-mr0", "remotes/origin/android-omap-tuna-3.0-jb-mr1",
      "remotes/origin/android-omap-tuna-3.0-jb-mr1.1",
      "remotes/origin/android-omap-tuna-3.0-jb-pre1", "remotes/origin/android-omap-tuna-3.0-mr0",
      "remotes/origin/android-omap-tuna-3.0-mr0.1", "remotes/origin/glass-omap-xrr02",
      "remotes/origin/linux-omap-3.0", "remotes/origin/sph-l700-fh05" };

  // the run specific business
  private static final String REPOSITORY_ROOT = "/home/psenin/thesis/android/omap/.git";

  // the amount of threads allowed for parallel execution
  private static final int MAX_THREADS = 3;

  // logger business
  // logger business
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(OMAPChangeLoader.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Main dispatcher thread.
   * 
   * @param args
   * @throws IOException
   * @throws GitAPIException
   * @throws CheckoutConflictException
   * @throws InvalidRefNameException
   * @throws RefNotFoundException
   * @throws RefAlreadyExistsException
   */
  public static void main(String[] args) throws IOException, RefAlreadyExistsException,
      RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {

    // make sure we run in UTC, just in case
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    // get the repository
    FileRepository repository = new FileRepository(REPOSITORY_ROOT);
    Git git = new Git(repository);

    for (String branch : branches) {

      git.checkout().setCreateBranch(true).setName(branch).call();

      repository.getWorkTree();

      // make sure the project is setup
      //
      OMAPDB db = OMAPDBManager.getProductionInstance();
      ChangeProject project = db.getProject("OMAP");

      // we are going to iterate commit by commit from the head by blocks of 100
      // so we not consume much of the memory and time by processing

      // setup
      CommitListFilter blockFilter = new CommitListFilter();
      CommitFilter limitFilter = new CommitLimitFilter(100).setStop(true);
      AndCommitFilter filters = new AndCommitFilter(limitFilter, blockFilter);
      CommitCursorFilter cursor = new CommitCursorFilter(filters);
      CommitFinder finder = new CommitFinder(repository);
      finder.setFilter(cursor);

      // Git git = new Git(repository);
      // git.checkout().setCreateBranch(true).setName("remotes/origin/HEAD").call();
      //
      // System.out.println(repository.getFullBranch().);
      //
      // for(RevCommit b : branches){
      // repository.getBranch();
      // System.out.println(repository.getRef(b.getId().getName()).getTarget().getName());
      // // System.out.println(b);
      // Ref ref = repository..checkout().
      // setCreateBranch(true).
      // setName("branchName").
      // setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
      // setStartPoint("origin/" + branchName).
      // call();
      // }

      // point to the repository HEAD

      // System.exit(10);

      RevCommit commit = CommitUtils.getHead(repository);

      // by now things seems to be in order - get the queue setup
      //
      // create thread pool for processing these users
      //
      ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
      CompletionService<String> completionService = new ExecutorCompletionService<String>(
          executorService);

      // and while the cursor is not null - keep rolling
      int jobsCounter = 0;
      int commitCounter = 0;
      while (commit != null) {

        // get the next block of 100
        finder.findFrom(commit);

        // "unload" a 100 commits out of there, cause things are not thread-safe in here
        List<RevCommit> commits = new ArrayList<RevCommit>();
        for (RevCommit c : blockFilter.getCommits()) {
          commits.add(c);
          commitCounter++;
        }
        final ChangeLoaderJob job = new ChangeLoaderJob(project, repository, commits);
        completionService.submit(job);

        jobsCounter++;
        // create a runnable task and push it into the queue

        commit = cursor.getLast();
        cursor.reset();
      }

      // shutdown the queue
      executorService.shutdown();

      consoleLogger.info("off to process " + commitCounter + " commits.");
      consoleLogger.info("submitted " + jobsCounter + " jobs. shutting down the queue.");

      try {

        while (jobsCounter > 0) {
          //
          // poll with a wait up to FOUR hours
          Future<String> finished = completionService.poll(40, TimeUnit.HOURS);

          if (null == finished) {
            //
            // something went wrong - break from here
            System.err.println("Breaking POLL loop after 40 HOURS of waiting...");
            break;
          }
          else {
            jobsCounter--;
            if (!(finished.get().isEmpty())) {
              System.err.println(finished.get());
            }
          }
        }

        consoleLogger.info("All jobs completed");

      }
      catch (Exception e) {
        System.err.println("Error while waiting results: " + StackTrace.toString(e));
      }
      finally {
        // wait at least 1 more hour before terminate and fail
        try {
          if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
            executorService.shutdownNow(); // Cancel currently executing tasks
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES))
              System.err.println("Pool did not terminate... FATAL ERROR");
          }
        }
        catch (InterruptedException ie) {
          System.err.println("Error while waiting interrupting: " + StackTrace.toString(ie));
          // (Re-)Cancel if current thread also interrupted
          executorService.shutdownNow();
          // Preserve interrupt status
          Thread.currentThread().interrupt();
        }

      }
    }
  }

}
