package org.stackwork.gradle.docker.tasks

import org.gradle.api.Project
import org.stackwork.gradle.docker.ModuleType
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject
import org.stackwork.gradle.docker.exceptions.IllegalDockerComposeFileVersionException
import org.yaml.snakeyaml.Yaml

import static java.lang.Integer.parseInt
import static org.stackwork.gradle.docker.ModuleType.TEST_IMAGE

class DockerComposeRunner {

  final Project project
  final String projectId = createRandomComposeProjectId()

  String composeFilePath
  int composeVersion
  List<String> longRunningServices

  private boolean infoLoaded = false
  private Map<String, Object> composeInfo

  DockerComposeRunner(Project project) {
    this.project = project
    composeFilePath = "${project.rootDir}/docker-compose.yml"
  }

  /**
   * Must run after {@link GenerateDockerComposeFileTask}, since that may change the {@link this.composeFilePath}
   * @return
   */
  void loadComposeInfo() {
    composeInfo = new Yaml().load(project.file(composeFilePath).text) as Map<String, Object>
    infoLoaded = true
    findComposeVersion()
    findLongRunningServices()
  }

  int getComposeVersion() {
    validateInfoLoaded 'getComposeVersion'
    composeVersion
  }

  List<String> getLongRunningServices() {
    validateInfoLoaded 'getLongRunningServices'
    longRunningServices
  }

  private void findComposeVersion() {
    composeVersion = composeInfo.containsKey('version') ? parseInt(composeInfo.version as String) : 1
    if (composeVersion != 1 && composeVersion != 2) {
      throw new IllegalDockerComposeFileVersionException(composeFilePath, composeVersion)
    } else {
      project.logger.info "${composeFilePath} uses Docker compose version ${composeVersion}"
    }
  }

  private void findLongRunningServices() {
    validateInfoLoaded 'findLongRunningServices'

    Map<String, Object> composeServices
    if (composeVersion == 1) {
      composeServices = composeInfo
    } else if (composeVersion == 2) {
      composeServices = composeInfo.services as Map<String, Object>
    } else {
      throw new IllegalDockerComposeFileVersionException(composeFilePath, composeVersion)
    }

    def isExecutableImage = { serviceName ->
      Project composeProject = project.extensions.getByType(StackworkExtension).composeProject
      StackworkObject composeProjectStackwork = composeProject.stackwork
      boolean serviceImageIsBuiltInModule = composeProjectStackwork.modules["$serviceName"]
      if (!serviceImageIsBuiltInModule) return false

      ModuleType moduleType = composeProjectStackwork.modules["$serviceName"]
      moduleType == TEST_IMAGE
    }

    List<String> allServices = []
    allServices.addAll(composeServices.keySet())
    List<List<String>> splitServices = allServices.split { isExecutableImage(it) }
    project.logger.info 'The following docker compose services were found to be executable: {}', splitServices[0]
    longRunningServices = splitServices[1]
    project.logger.info 'The following docker compose services were found to be long-running: {}', longRunningServices
    if (longRunningServices.empty) {
      throw new IllegalStateException('No long-running service defined in docker compose file')
    }
  }

  private validateInfoLoaded(String method) {
    if (!infoLoaded) {
      throw new IllegalStateException("Cannot call ${method} before loading the compose file")
    }
  }

  private static String createRandomComposeProjectId() {
    def pool = ['A'..'Z', 0..9].flatten()
    Random rand = new Random(System.currentTimeMillis())
    def passChars = (0..8).collect { pool[rand.nextInt(pool.size())] }
    passChars.join()
  }

}
