package nl.qkrijger.gradle.docker.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class TagImageTask extends Exec {

  TagImageTask() {
    doFirst {
      if (project.version == Project.DEFAULT_VERSION) {
        throw new IllegalStateException('No project version defined. Cannot tag image. Please set "project.version".')
      }
      if (!project.docker.imageName) {
        throw new IllegalStateException('No docker image name defined. Cannot tag image. Please set "docker.imageName".')
      }
    }
    commandLine 'docker', 'tag', "${-> project.docker.imageId}", "${-> project.docker.imageName}:${-> project.version}"
    doLast {
      project.ext.docker.fullImageName = "${project.docker.imageName}:${project.version}"
    }
  }

}
