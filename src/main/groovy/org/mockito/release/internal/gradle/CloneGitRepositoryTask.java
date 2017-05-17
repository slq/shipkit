package org.mockito.release.internal.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.mockito.release.exec.ProcessRunner;

import java.io.File;

/**
 * This task clone git project from repository to target dir.
 * It support clone from remote server and from local filesystem.
 *
 * TODO ms - when you are ready, please move the new task types to the public packages,
 *   for example "org.mockito.release.*". With 1.0 we need all task types to be public.
 *   It's because users interface with task types when they work with Gradle build scripts.
 *   So it makes sense to be explicit that those types are public and we guarantee compatibility.
 *   See also README.md on the compatibility where I attempted to describe this ;)
 */
public class CloneGitRepositoryTask extends DefaultTask {

    private static final Logger LOG = Logging.getLogger(CloneGitRepositoryTask.class);

    private String repositoryUrl;
    private File targetDir;

    @TaskAction
    public void cloneRepository() {
        LOG.lifecycle("  Cloning repository {}\n    into {}", repositoryUrl, targetDir);
        getProject().getBuildDir().mkdirs();    // build dir can be not created yet
        ProcessRunner processRunner = org.mockito.release.exec.Exec.getProcessRunner(getProject().getBuildDir());
        processRunner.run("git", "clone", repositoryUrl, targetDir.getAbsolutePath());
    }

    //   I don't know if repository should be a name of repo or valid url to the repo
    // TODO sf we clone from *-pristine to *-work so we need url here

    /**
     * See {@link #getRepositoryUrl()}
     */
    @Input
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    /**
     * Repository URL, clone source location. It accept any kind of url accepted by git clone command.
     * Examples:
     * <ul>
     *      <li>https://github.com/mockito/mockito</li>
     *      <li>/Users/mstachniuk/code/mockito</li>
     *      <li>file:///Users/mstachniuk/code/mockito</li>
     * </ul>
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    /**
     * See {@link #getTargetDir()}
     * @param targetDir
     */
    @OutputDirectory
    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    /**
     * A path where to clone a repository.
     */
    public File getTargetDir() {
        return targetDir;
    }
}
