package org.shipkit.gradle

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class UpdateReleaseNotesTaskTest extends Specification {

    @Rule
    TemporaryFolder tmp = new TemporaryFolder()

    def project = new ProjectBuilder().build()
    def tasks = project.getTasks();
    def incrementalNotesGenerator = Mock(UpdateReleaseNotesTask.IncrementalNotesGenerator)

    UpdateReleaseNotesTask underTest = tasks.create("updateReleaseNotes", UpdateReleaseNotesTask)

    void setup(){
        underTest.incrementalNotesGenerator = incrementalNotesGenerator
        underTest.gitHubUrl = "https://github.com"
    }

    @Unroll
    def "should fail if gitHubUrl is not set"() {
        given:
        underTest.gitHubUrl = gitHubUrl

        when:
        underTest.updateReleaseNotes()

        then:
        def ex = thrown(GradleException)
        ex.message == "':updateReleaseNotes.gitHubUrl' must be configured."

        where:
        gitHubUrl << ["     ", "", null]
    }

    def "should fail if gitHubRepository null" (){
        when:
        underTest.updateReleaseNotes()

        then:
        def ex = thrown(GradleException)
        ex.message == "':updateReleaseNotes.gitHubRepository' must be configured."
    }

    def "should fail if gitHubRepository empty" (){
        given:
        underTest.gitHubRepository == ""

        when:
        underTest.updateReleaseNotes()

        then:
        def ex = thrown(GradleException)
        ex.message == "':updateReleaseNotes.gitHubRepository' must be configured."
    }

    def "should fail if releaseNotesFile is not configured and not in preview mode" (){
        given:
        underTest.gitHubRepository = "mockito/mockito"

        when:
        underTest.updateReleaseNotes()

        then:
        def ex = thrown(GradleException)
        ex.message == "':updateReleaseNotes.releaseNotesFile' must be configured."
    }

    def "should fail if releaseNotesFile is a directory and not in preview mode" (){
        given:
        underTest.gitHubRepository = "mockito/mockito"
        def dir = tmp.newFolder("release-notes")
        underTest.setReleaseNotesFile(dir)

        when:
        underTest.updateReleaseNotes()

        then:
        def ex = thrown(GradleException)
        ex.message == "':updateReleaseNotes.releaseNotesFile' must be a file."
    }

    def "should create releaseNotesFile automatically if does not exist and not in preview mode" (){
        given:
        def file = new File(tmp.root.absolutePath + "/docs/release-notes.md")
        underTest.releaseNotesFile = file
        underTest.gitHubRepository = "mockito/mockito"

        when:
        underTest.updateReleaseNotes()

        then:
        file.parentFile.directory
        file.file
    }

    def "should update release notes if not in preview mode" (){
        given:
        def file = tmp.newFile("release-notes.md")
        file << ""
        underTest.releaseNotesFile = file
        underTest.gitHubRepository = "mockito/mockito"
        incrementalNotesGenerator.generateNewContent() >> "content"

        when:
        underTest.updateReleaseNotes()

        then:
        file.text == "content"
    }

    def "should not modify releaseNotesFile if in preview mode" (){
        given:
        def file = tmp.newFile("release-notes.md")
        file << ""
        underTest.releaseNotesFile = file
        underTest.gitHubRepository = "mockito/mockito"
        incrementalNotesGenerator.generateNewContent() >> "content"

        underTest.setPreviewMode(true)

        when:
        underTest.updateReleaseNotes()

        then:
        file.text == ""
    }
}
