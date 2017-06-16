package org.stackwork.gradle.docker.tasks

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.stackwork.gradle.docker.ModuleType
import org.stackwork.gradle.docker.StackworkExtension
import org.stackwork.gradle.docker.StackworkObject
import org.stackwork.gradle.docker.exceptions.IllegalDockerComposeFileVersionException
import org.yaml.snakeyaml.Yaml

import static java.lang.Integer.parseInt
import static org.stackwork.gradle.docker.ModuleType.BUILDER_IMAGE
import static org.stackwork.gradle.docker.ModuleType.TEST_IMAGE

class DockerComposeRunner {

  final Project project
  final StackworkObject stackwork
  final String projectId = createRandomComposeProjectId()
  final String composeFileName
  final boolean buildRunner

  String composeFilePath
  Process composeProcess
  int composeVersion
  List<String> longRunningServices

  private boolean infoLoaded = false
  private Map<String, Object> composeInfo

  static DockerComposeRunner newDockerComposeRunner(Project project, StackworkObject stackwork) {
    new DockerComposeRunner(project, stackwork, false)
  }

  static DockerComposeRunner newBuildDockerComposeRunner(Project project, StackworkObject stackwork) {
    new DockerComposeRunner(project, stackwork, true)
  }

  private DockerComposeRunner(Project project, StackworkObject stackwork, boolean buildRunner) {
    this.project = project
    this.stackwork = stackwork
    this.composeFileName = buildRunner ? 'build.docker-compose.yml' : 'docker-compose.yml'
    this.buildRunner = buildRunner
    // default value for compose file path - can be overridden in case of a docker compose template file
    composeFilePath = "${project.rootDir}/${composeFileName}"
  }

  void parseComposeTemplate() {
    composeFilePath = "${stackwork.buildDir}/${composeFileName}"
    project.copy {
      from project.projectDir
      into stackwork.buildDir
      include "${composeFileName}.template"
      rename { file -> composeFileName }
      expand project.properties
    }
  }

  void run() {
    loadComposeInfo()
    startLongRunningServiceAndWaitForLogMarker()
    writeServiceHostsAndPorts()
  }

  void stop() {
    OutputStream out = new ByteArrayOutputStream()
    project.exec {
      // count the number of containers already exited with a non-zero exit code. These can be either failed executable
      // images such as a TEST_IMAGE, or a long-running service that has been exited prematurely.
      // Running container are seen by docker inspect to have ExitCode = 0
      String commandToSeeIfAnyContainersOfTheStackFailed =
          "docker-compose -f \"${->composeFilePath}\" -p \"${->projectId}\" ps -q | xargs docker inspect -f '{{ .State.ExitCode }}' | grep -v 0 | wc -l | tr -d ' '"
      commandLine 'bash', '-c', commandToSeeIfAnyContainersOfTheStackFailed
      standardOutput = out
    }
    int nrOfNonZeroExitCodes = out.toString() as Integer

    if (nrOfNonZeroExitCodes > 0) {
      project.logger.error("SDFAFDSSDASDAHSDJAKHSDJKAKSDAJFKSDAJFKSDJAKFJSDAKFJSDKAJKSDAJKSDAJFKDSAJFDKSAJ")
      Thread.sleep(500000)
    }

    project.exec {
      commandLine 'docker-compose', '-f', "${->composeFilePath}", '-p', "${->projectId}", 'stop'
    }

    project.logger.info "Stack will be exited. '$nrOfNonZeroExitCodes' container(s) already have a non-zero exit code."
    if (nrOfNonZeroExitCodes != 0) {
      def msg = "The docker compose process in project '$project.name' will be shut down. However, " +
          "'$nrOfNonZeroExitCodes' container(s) exited with a non-zero exit code, so we're failing the build."
      project.logger.error msg
      throw new GradleException(msg)
    }
  }

  void clean() {
    project.exec {
      commandLine 'docker-compose', '-f', "${->composeFilePath}", '-p', "${->projectId}", 'down'
    }
  }

  /**
   * Must run after {@link GenerateDockerComposeFileTask}, since that may change the {@link this.composeFilePath}
   * @return
   */
  private void loadComposeInfo() {
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
    if (composeVersion != 1 && composeVersion != 2 && composeVersion != 3) {
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
    } else if (composeVersion == 2 || composeVersion == 3) {
      composeServices = composeInfo.services as Map<String, Object>
    } else {
      throw new IllegalDockerComposeFileVersionException(composeFilePath, composeVersion)
    }

    def isExecutableImage = { serviceName ->
      def projectRelatedToService = project.rootProject.findProject(":${serviceName}")
      if (projectRelatedToService) {
        def moduleType = projectRelatedToService.extensions.findByType(StackworkExtension)?.moduleType
        return TEST_IMAGE == moduleType || BUILDER_IMAGE == moduleType
      }
      return false
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

  /**
   * Start the long running services for the compose project and monitor it's output
   */
  private void startLongRunningServiceAndWaitForLogMarker() {
    String[] command = ['docker-compose', '-f', composeFilePath, '-p', projectId, 'up'] + this.longRunningServices

    String marker = buildRunner ?
        project.extensions.getByType(StackworkExtension).buildStackIsRunningWhenLogContains :
        project.extensions.getByType(StackworkExtension).stackIsRunningWhenLogContains

    if (marker) {
      project.logger.info 'Log marker defined: "{}". Using this to scan logs for start indicator.', marker
    } else {
      project.logger.info 'No log marker defined, compose will be started "fire and forget" style.'
    }

    project.logger.info 'Starting Docker Compose using command: {}', Arrays.toString(command)

    spawnProcessAndWaitFor command, { String line ->
      project.logger.info line
      if (marker == null || line == null) {
        boolean allServicesHaveStarted = true
        longRunningServices.each { String serviceName ->
          String containerId = this.askComposeServicesContainerId(serviceName)
          if (containerId == null || containerId.empty) allServicesHaveStarted = false
        }
        return allServicesHaveStarted
      }

      if (line.contains(marker)) {
        project.logger.info 'Found marker "{}" in compose logs. Stack started.', marker
        return true
      }

      return false
    }
  }

  private void writeServiceHostsAndPorts() {
    longRunningServices.each { String serviceName ->
      Map<String, Object> serviceInfo = [:]
      String containerId = this.askComposeServicesContainerId(serviceName)
      Map<String, Object> containerInfo = this.dockerInspectContainer(containerId)

      // dockerHost is null in case of local Docker deamon, or the host to connect to for a remote Docker host

      containerInfo.NetworkSettings.Ports.each { String exposedPortProto, forwardedPorts ->
        int exposedPort = (exposedPortProto =~ /\d+/)[0] as int

        if (stackwork.host) {
          if (!forwardedPorts) {
            project.logger.warn("No port forwarding defined for service '$serviceName'. " +
                "No port configuration will be exposed.")
            return
          }
          if (forwardedPorts.isEmpty()) {
            project.logger.warn("No forwarded port found for exposed port: '$exposedPort'")
          } else {
            if (forwardedPorts.size() > 1) {
              project.logger.warn("Multiple forwarded ports found for exposed port: '$exposedPort'. " +
                  "Continuing with a randomly selected port.")
            }
            serviceInfo.port = forwardedPorts.first().HostPort
          }
          serviceInfo.host = stackwork.host
        } else {
          serviceInfo.port = exposedPort
          serviceInfo["port.${exposedPort}"] = exposedPort
          if (composeVersion == 1) {
            serviceInfo.host = containerInfo.NetworkSettings.IPAddress
          } else if (composeVersion == 2) {
            Map<String, Object> networks = containerInfo.NetworkSettings.Networks
            if (networks.size() != 1) {
              throw new IllegalStateException("${networks.size()} networks found, only 1 supported!")
            }
            serviceInfo.host = networks.values().first().IPAddress
          } else {
            throw new IllegalStateException('Unsupported docker compose version')
          }
        }
      }
      stackwork.services["$serviceName"] = serviceInfo
      project.logger.info "Setting stackwork.services.$serviceName: $serviceInfo"
    }
  }

  private String askComposeServicesContainerId(String serviceName) {
    OutputStream os = new ByteArrayOutputStream()
    project.exec {
      commandLine 'docker-compose', '-f', composeFilePath, '-p', projectId, 'ps', '-q', serviceName
      standardOutput = os
    }
    os.toString().trim()
  }

  private Map<String, Object> dockerInspectContainer(String containerId) {
    OutputStream containerInfoOS = new ByteArrayOutputStream()
    project.exec {
      commandLine 'docker', 'inspect', containerId
      standardOutput = containerInfoOS
    }
    (new Yaml().load(containerInfoOS.toString()))[0] as Map<String, Object>
  }

  /**
   * Spawn an external process and monitor it's standard and error output for a certain predicate
   *
   * TODO: add configurable time-out functionality in case the predicate is never fulfilled
   */
  private void spawnProcessAndWaitFor(String[] command, Closure logPredicate) {

    File logDir = project.file("${stackwork.buildDir}/logs")
    logDir.mkdirs()
    File logFile = buildRunner ?
        new File(logDir, "build.docker-compose-${projectId}.log") :
        new File(logDir, "docker-compose-${projectId}.log")
    if (buildRunner) {
      stackwork.buildComposeLogFile = logFile
    } else {
      stackwork.composeLogFile = logFile
    }
    logFile.createNewFile()
    Process compose = new ProcessBuilder(command).
        redirectErrorStream(true).
        redirectOutput(logFile).
        start()
    this.composeProcess = compose

    logFile.withReader { reader ->
      String line
      while (reader.ready() || compose.alive) {
        line = reader.readLine()
        if (line) {
          if (logPredicate(line)) {
            break
          }
        } else {
          // compose is still running, so we can expect new log lines later
          Thread.sleep(100)
        }
      }
    }
  }

  private static String createRandomComposeProjectId() {
    def pool = ['A'..'Z', 0..9].flatten()
    Random rand = new Random(System.currentTimeMillis())
    (0..8).collect({ pool[rand.nextInt(pool.size())] }).join('')
  }
}
