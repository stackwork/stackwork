package org.stackwork.gradle.docker.tasks

import org.gradle.api.tasks.Copy

class CollectImageDependenciesTask extends Copy {

  final static NAME = 'collectImageDependencies'

  CollectImageDependenciesTask() {
    description = 'Copies the Docker dependencies to the build folder to make them available during an image build'
    group = 'Stackwork'

    from project.configurations.stackwork
    into "${project.buildDir}/stackwork-deps"
  }
}
