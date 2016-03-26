package org.stackwork.gradle.docker.tasks

import org.gradle.api.internal.AbstractTask

class CollectImageDependenciesTask extends AbstractTask {

  final static NAME = 'collectImageDependencies'

  CollectImageDependenciesTask() {
    description = 'Copies the Docker dependencies to the build folder to make them available during an image build'
    group = 'Stackwork'

    doLast {
      project.configurations.stackwork.resolvedConfiguration.resolvedArtifacts.each { art ->
        project.copy {
          project.logger.info 'art : {}', art
          from art.file
          into project.file('build/stackwork-deps')
          String filename = "${art.name}${art.classifier ? '-' + art.classifier : ''}.${art.extension}"
          println "Preparing build/docker-artifacts/${filename}"
          rename art.file.name, filename
        }
      }
    }
  }
}
