package org.shipkit.internal.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Exec;
import org.shipkit.gradle.ReleaseConfiguration;
import org.shipkit.gradle.SecureExecTask;
import org.shipkit.internal.gradle.util.GitUtil;
import org.shipkit.internal.gradle.util.TaskMaker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.shipkit.internal.gradle.configuration.DeferredConfiguration.deferredConfiguration;
import static org.shipkit.internal.gradle.configuration.LazyConfiguration.lazyConfiguration;
import static org.shipkit.internal.gradle.util.GitUtil.getTag;

/**
 * Adds Git-specific tasks needed for the release process:
 *
 * <ul>
 *     <li>gitCommit</li>
 *     <li>gitTag</li>
 *     <li>gitPush</li>
 *     <li>performGitPush</li>
 *
 *     <li>gitCommitCleanUp</li>
 *     <li>gitTagCleanUp</li>
 * </ul>
 */
public class GitPlugin implements Plugin<Project> {

    static final String COMMIT_CLEANUP_TASK = "gitCommitCleanUp";
    static final String TAG_CLEANUP_TASK = "gitTagCleanUp";
    static final String GIT_TAG_TASK = "gitTag";
    static final String GIT_PUSH_TASK = "gitPush";
    static final String PERFORM_GIT_PUSH_TASK = "performGitPush";
    public static final String GIT_COMMIT_TASK = "gitCommit";

    public void apply(final Project project) {
        final ReleaseConfiguration conf = project.getPlugins().apply(ReleaseConfigurationPlugin.class).getConfiguration();
        final GitStatusPlugin.GitStatus gitStatus = project.getPlugins().apply(GitStatusPlugin.class).getGitStatus();

        TaskMaker.task(project, GIT_COMMIT_TASK, GitCommitTask.class, new Action<GitCommitTask>() {
            public void execute(final GitCommitTask t) {
                t.setDescription("Commits all changed files using generic --author and aggregated commit message");
                t.doFirst(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        List<Object> arguments = new ArrayList<Object>();
                        arguments.add("git");
                        arguments.add("commit");
                        arguments.add("--author");
                        arguments.add(GitUtil.getGitGenericUserNotation(conf));
                        arguments.add("-m");
                        arguments.add(GitUtil.getCommitMessage(conf, t.getAggregatedCommitMessage()));
                        arguments.addAll(t.getFiles());

                        t.commandLine(arguments);
                    }
                });
            }
        });

        TaskMaker.execTask(project, GIT_TAG_TASK, new Action<Exec>() {
            public void execute(final Exec t) {
                t.mustRunAfter(GIT_COMMIT_TASK);
                final String tag = GitUtil.getTag(conf, project);
                t.setDescription("Creates new version tag '" + tag + "'");
                deferredConfiguration(project, new Runnable() {
                    @Override
                    public void run() {
                        t.commandLine("git", "tag", "-a", tag, "-m",
                                GitUtil.getCommitMessage(conf, "Created new tag " + tag));
                    }
                });
            }
        });

        TaskMaker.task(project, GIT_PUSH_TASK, SecureExecTask.class, new Action<SecureExecTask>() {
            public void execute(final SecureExecTask t) {
                t.setDescription("Pushes automatically created commits to remote repo.");
                t.mustRunAfter(GIT_COMMIT_TASK);
                t.mustRunAfter(GIT_TAG_TASK);

                t.doFirst(new Action<Task>() {
                    public void execute(Task task) {
                        //Getting the branch during task execution time so that integ testing is easier
                        //TODO revisit. How can we make integ testing easy without using doFirst to configure tasks?
                        String branch = gitStatus.getBranch();
                        t.setCommandLine(GitUtil.getGitPushArgs(conf, project, branch));
                    }
                });
                lazyConfiguration(t, new Runnable() {
                    public void run() {
                        t.setSecretValue(conf.getGitHub().getWriteAuthToken());
                    }
                });
            }
        });

        TaskMaker.task(project, PERFORM_GIT_PUSH_TASK, Task.class, new Action<Task>() {
            public void execute(final Task t) {
                t.setDescription("Performs gitCommit, gitTag and gitPush tasks and all tasks dependent on them.");
                t.dependsOn(GIT_COMMIT_TASK, GIT_TAG_TASK, GIT_PUSH_TASK);
            }
        });

        TaskMaker.execTask(project, COMMIT_CLEANUP_TASK, new Action<Exec>() {
            public void execute(final Exec t) {
                t.setDescription("Removes last commit, using 'reset --soft HEAD~'");
                //TODO replace with combination of 'git reset --soft HEAD~ && git stash' so that we don't lose commits
                t.commandLine("git", "reset", "--soft", "HEAD~");
            }
        });

        TaskMaker.execTask(project, TAG_CLEANUP_TASK, new Action<Exec>() {
            public void execute(final Exec t) {
                t.setDescription("Deletes version tag '" + getTag(conf, project) + "'");
                t.commandLine("git", "tag", "-d", getTag(conf, project));
            }
        });
    }

    public static void registerChangesForCommitIfApplied(final List<File> changedFiles,
                                                         final String changeDescription, final Task changingTask){
        final Project project = changingTask.getProject();
        project.getPlugins().withType(GitPlugin.class, new Action<GitPlugin>() {
            @Override
            public void execute(GitPlugin gitPushPlugin) {
                GitCommitTask gitCommitTask = (GitCommitTask) project.getTasks().findByName(GitPlugin.GIT_COMMIT_TASK);
                gitCommitTask.addChange(changedFiles, changeDescription, changingTask);
            }
        });
    }
}
