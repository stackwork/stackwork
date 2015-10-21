package nl.qkrijger.gradle.docker.tasks

import nl.qkrijger.gradle.docker.DockerExtension
import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Copy

class PrepareDockerFileTask extends AbstractTask {

  final static NAME = 'prepareDockerFile'

  PrepareDockerFileTask() {

    group = 'Docker'
    description = 'Prepares the Dockerfile, activates parsing a template if needed'

    dependsOn project.task("parseDockerFileTemplate", type: Copy) {
      // disable task caching
      onlyIf { dependsOnBaseImage() }
      outputs.upToDateWhen { false }
      description = 'Parsing the Dockerfile.template'

      from project.projectDir
      into project.docker.buildDir
      include 'Dockerfile.template'
      rename { file -> 'Dockerfile' }
      expand project.properties
    }

    doLast {
      project.docker.dockerFile = dependsOnBaseImage() ?
              project.file("${project.docker.buildDir}/Dockerfile").absolutePath :
              project.file('Dockerfile').absolutePath
    }
  }

  boolean dependsOnBaseImage() {
    def baseImageProject = getBaseImageProject()
    baseImageProject != null && baseImageProject != project
  }

  Project getBaseImageProject() {
    project.extensions.getByType(DockerExtension).baseImageProject
  }

}
