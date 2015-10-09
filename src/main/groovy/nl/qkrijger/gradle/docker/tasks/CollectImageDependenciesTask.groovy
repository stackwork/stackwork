package nl.qkrijger.gradle.docker.tasks

import org.gradle.api.tasks.Copy

class CollectImageDependenciesTask extends Copy {

  CollectImageDependenciesTask() {
    description = 'Copies the Docker dependencies to the build folder to make them available during an image build'
    group = 'Docker'

    from project.configurations.docker
    into "${project.buildDir}/docker-deps"
  }
}
