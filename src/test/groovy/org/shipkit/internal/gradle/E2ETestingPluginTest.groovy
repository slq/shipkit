package org.shipkit.internal.gradle

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class E2ETestingPluginTest extends Specification {

    def project = new ProjectBuilder().build()

    def "e2e with example project by default"() {
        project.plugins.apply("org.shipkit.e2e-test")

        expect:
        project.tasks.'runTestReleaseShipkit-example'
    }

    def "should extract project name correctly"() {
        E2ETestingPlugin.E2ETest sut = new E2ETestingPlugin.E2ETest(project)

        when:
        sut.create("https://github.com/mockito/mockito")

        then:
        project.tasks.runTestReleaseMockito
        project.tasks.cloneProjectFromGitHubMockito
        project.tasks.cloneProjectToWorkDirMockito
    }

    def "should extract project name correctly when slash is the last char in url"() {
        E2ETestingPlugin.E2ETest sut = new E2ETestingPlugin.E2ETest(project)

        when:
        sut.create("https://github.com/xx/yyy/")

        then:
        project.tasks.runTestReleaseYyy
        project.tasks.cloneProjectFromGitHubYyy
        project.tasks.cloneProjectToWorkDirYyy
    }
}
