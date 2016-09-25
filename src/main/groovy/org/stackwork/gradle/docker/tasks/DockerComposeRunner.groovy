package org.stackwork.gradle.docker.tasks

import org.gradle.api.Project

class DockerComposeRunner {

  final Project project
  final String projectId = createRandomComposeProjectId()
  String composeFilePath

  DockerComposeRunner(Project project) {
    this.project = project
    composeFilePath = "${project.rootDir}/docker-compose.yml"
  }

  private static String createRandomComposeProjectId() {
    def pool = ['A'..'Z', 0..9].flatten()
    Random rand = new Random(System.currentTimeMillis())
    def passChars = (0..8).collect { pool[rand.nextInt(pool.size())] }
    passChars.join()
  }

}
