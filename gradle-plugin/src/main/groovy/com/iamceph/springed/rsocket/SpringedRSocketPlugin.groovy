package com.iamceph.springed.rsocket

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringedRSocketPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create("springedRSocket", SpringedRSocketExtension)
        extension.springedRsocketVersion = project.version

        project.afterEvaluate {
            project.getPluginManager().withPlugin("java", plugin -> {
                project.apply {
                    it.from(getClass().getResource("/springed-rsocket-server.gradle"))
                }
            })
        }
    }
}
