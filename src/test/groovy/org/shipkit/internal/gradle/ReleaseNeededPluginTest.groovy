package org.shipkit.internal.gradle

import testutil.PluginSpecification

class ReleaseNeededPluginTest extends PluginSpecification {

    def "applies"() {
        expect:
        project.plugins.apply("org.shipkit.release-needed")
    }
}
