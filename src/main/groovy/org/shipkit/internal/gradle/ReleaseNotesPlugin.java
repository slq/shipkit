package org.shipkit.internal.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.shipkit.gradle.UpdateReleaseNotesTask;
import org.shipkit.gradle.ReleaseConfiguration;
import org.shipkit.gradle.ReleaseNotesFetcherTask;
import org.shipkit.internal.gradle.util.TaskMaker;
import org.shipkit.internal.version.VersionInfo;

import java.io.File;
import java.util.Arrays;

import static org.shipkit.internal.gradle.configuration.DeferredConfiguration.deferredConfiguration;

/**
 * The plugin adds following tasks:
 *
 * <ul>
 *     <li>fetchReleaseNotes - fetches release notes data, see {@link ReleaseNotesFetcherTask}</li>
 *     <li>updateReleaseNotes - updates release notes file in place, or only displays preview if project property 'preview' exists, see {@link UpdateReleaseNotesTask}</li>
 * </ul>
 *
 * It also adds updates release notes changes if {@link GitPlugin} applied
 */
public class ReleaseNotesPlugin implements Plugin<Project> {

    private static final String FETCH_NOTES_TASK = "fetchReleaseNotes";
    public static final String UPDATE_NOTES_TASK = "updateReleaseNotes";

    public void apply(final Project project) {
        final ReleaseConfiguration conf = project.getPlugins().apply(ReleaseConfigurationPlugin.class).getConfiguration();
        project.getPlugins().apply(VersioningPlugin.class);
        project.getPlugins().apply(ContributorsPlugin.class);

        releaseNotesTasks(project, conf);
    }

    private static void releaseNotesTasks(final Project project, final ReleaseConfiguration conf) {
        final ReleaseNotesFetcherTask releaseNotesFetcher = TaskMaker.task(project, FETCH_NOTES_TASK, ReleaseNotesFetcherTask.class, new Action<ReleaseNotesFetcherTask>() {
            public void execute(final ReleaseNotesFetcherTask t) {
                t.setDescription("Fetches release notes data from Git and GitHub and serializes them to a file");
                t.setOutputFile(new File(project.getBuildDir(), "detailed-release-notes.ser"));
                t.setGitHubApiUrl(conf.getGitHub().getApiUrl());
                t.setGitHubReadOnlyAuthToken(conf.getGitHub().getReadOnlyAuthToken());
                t.setGitHubRepository(conf.getGitHub().getRepository());
                t.setPreviousVersion(conf.getPreviousReleaseVersion());
                t.setIgnoreCommitsContaining(conf.getReleaseNotes().getIgnoreCommitsContaining());
            }
        });

        final AllContributorsFetcherTask contributorsFetcher = (AllContributorsFetcherTask) project.getTasks().getByName(ContributorsPlugin.FETCH_ALL_CONTRIBUTORS_TASK);

        TaskMaker.task(project, UPDATE_NOTES_TASK, UpdateReleaseNotesTask.class, new Action<UpdateReleaseNotesTask>() {
            public void execute(final UpdateReleaseNotesTask t) {
                t.setDescription("Updates release notes file. Run with '-Ppreview' if you only want to see the preview.");

                configureDetailedNotes(t, releaseNotesFetcher, project, conf, contributorsFetcher);

                boolean previewMode = project.hasProperty(UpdateReleaseNotesTask.PREVIEW_PROJECT_PROPERTY);
                t.setPreviewMode(previewMode);

                if(!previewMode){
                    File releaseNotesFile = project.file(conf.getReleaseNotes().getFile());
                    GitPlugin.registerChangesForCommitIfApplied(
                        Arrays.asList(releaseNotesFile), "release notes updated", t);
                    t.getOutputs().file(releaseNotesFile);
                }
            }
        });
    }

    private static void configureDetailedNotes(final UpdateReleaseNotesTask task,
                                               final ReleaseNotesFetcherTask releaseNotesFetcher,
                                               final Project project,
                                               final ReleaseConfiguration conf,
                                               final AllContributorsFetcherTask contributorsFetcher) {
        task.dependsOn(releaseNotesFetcher);
        task.dependsOn(contributorsFetcher);

        task.setVersion(project.getVersion().toString());
        task.setTagPrefix(conf.getGit().getTagPrefix());

        task.setDevelopers(conf.getTeam().getDevelopers());
        task.setContributors(conf.getTeam().getContributors());
        task.setGitHubLabelMapping(conf.getReleaseNotes().getLabelMapping()); //TODO make it optional
        task.setReleaseNotesFile(project.file(conf.getReleaseNotes().getFile())); //TODO add sensible default
        task.setGitHubRepository(conf.getGitHub().getRepository());
        task.setPreviousVersion(project.getExtensions().getByType(VersionInfo.class).getPreviousVersion());

        deferredConfiguration(project, new Runnable() {
            public void run() {
                task.setReleaseNotesData(releaseNotesFetcher.getOutputFile());
                task.setContributorsDataFile(contributorsFetcher.getOutputFile());
            }
        });
    }
}
