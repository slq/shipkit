package org.shipkit.internal.gradle

import org.gradle.api.GradleException
import org.shipkit.internal.gradle.configuration.LazyConfiguration
import testutil.PluginSpecification

class BintrayPluginTest extends PluginSpecification {

    def setup() {
        project.plugins.apply("org.shipkit.bintray")
    }

    def "deferred configuration"() {
        project.version = "1.0"
        project.description = "some proj"
        project.plugins.apply("org.shipkit.bintray")

        project.shipkit.dryRun = true
        project.shipkit.gitHub.repository = 'repo'
        project.bintray.user = 'szczepiq'

        when:
        project.evaluate()

        then:
        project.bintray.pkg.version.vcsTag == "v1.0"
        project.bintray.dryRun == true
        project.bintray.pkg.vcsUrl == "https://github.com/repo.git"
        project.bintray.pkg.issueTrackerUrl == "https://github.com/repo/issues"
        project.bintray.pkg.websiteUrl == "https://github.com/repo"
        project.bintray.pkg.desc == "some proj"
    }

    def "deferred configuration honors user settings"() {
        project.version = "1.0"
        project.description = "some proj"
        project.plugins.apply("org.shipkit.bintray")

        project.shipkit.dryRun = true
        project.shipkit.gitHub.repository = 'repo'

        project.bintray.dryRun = false //this one is not honored at the moment, we're ok with this
        project.bintray.user = 'szczepiq'
        project.bintray.key = 'xyz'
        project.bintray.pkg.vcsUrl = "vcs"
        project.bintray.pkg.version.vcsTag = "v4.0"
        project.bintray.pkg.issueTrackerUrl = "issueTracker"
        project.bintray.pkg.websiteUrl = "website"
        project.bintray.pkg.desc = "my desc"

        when:
        project.evaluate()
        LazyConfiguration.forceConfiguration(project.tasks.bintrayUpload)

        then:
        project.bintray.dryRun == true
        project.bintray.key == 'xyz'
        project.bintray.pkg.vcsUrl == "vcs"
        project.bintray.pkg.issueTrackerUrl == "issueTracker"
        project.bintray.pkg.websiteUrl == "website"
        project.bintray.pkg.desc == "my desc"
        project.bintray.pkg.version.vcsTag == "v4.0"
    }

    def "fails if bintray.user is not set"() {
        project.plugins.apply("org.shipkit.bintray")

        project.bintray.key = 'xyz'

        when:
        project.evaluate()
        LazyConfiguration.forceConfiguration(project.tasks.bintrayUpload)

        then:
        def ex = thrown(GradleException)
        ex.message ==
                "Missing 'bintray.user' value.\n" +
                "  Please configure Bintray extension."
    }
}
