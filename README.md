[![Build Status](https://travis-ci.org/Krijger/docker-gradle.svg?branch=master)](https://travis-ci.org/Krijger/docker-gradle)
# docker-gradle
Gradle plugin for building, testing and deploying Docker images using docker compose

## Usage

For any of the use cases below, your projects gradle file should include the plugin:

    // build.gradle
    buildscript {
      repositories {
        jcenter()
      }
      dependencies {
        classpath group: 'nl.qkrijger.gradle', name: 'docker-gradle', version: '0.3'
      }
    }
    apply plugin: 'groovy'
    apply plugin: 'docker'

### Building, tagging and pushing an image

For tagging, make sure your image has a name and a version:

    // build.gradle
    version = '1.1-SNAPSHOT'
    docker.imageName = 'my-image'

Run by calling the Gradle `pushImage` task.

### Run groovy unit tests against an image built in your project

To allow your tests access to the service's (i.e. running container) host and port, you need to make your tests depend
on the `runDockerCompose` task and use the serviceInfo from the docker object populated by that task:

    // build.gradle
    tasks.test.doFirst {
      project.docker.services.each { serviceName, serviceInfo ->
        serviceInfo.each { infoKey, infoVal ->
          systemProperties["docker.$serviceName.$infoKey"] = infoVal
        }
      }
    }.dependsOn tasks.runDockerCompose

Clean up after yourself, also in case of failing tests by:

    tasks.runDockerCompose.finalizedBy tasks.cleanDockerCompose
    tasks.stopDockerCompose.mustRunAfter tasks.test

## Features

### Gradle docker object (runtime data)

The plugin exports a property object 'docker' that you can use in your Gradle build file.
The object is reachable via `project.docker.[property]`. For the following build tasks, these properties are

build task | property
---------- | -------------
n.a. done always | host
buildImage | imageId
tagImage   | fullImageName
generateDockerComposeFile | composeFile
runDockerCompose | services.SERVICE_NAME.host
runDockerCompose | services.SERVICE_NAME.port


### Gradle task buildImage

The `buildImage` task executes a docker build command in the root of your project.
You can have only a single Dockerfile, and the deliverable of the project is a single Docker image.
The resulting image id is exposed to Gradle via the docker object.

### Gradle task generateDockerComposeFile

The `generateDockerComposeFile` task parses the `docker-compose.yml.template` file in the projects `src/test` with all
project properties. Possible usage is a template file containing

    service:
      image: ${docker.imageId}
      
The resulting file including its path is exposed to Gradle via the Docker object for depending tasks.

Dependency: buildImage

### Gradle task runDockerCompose

The `runDockerCompose` task runs the generated docker compose file. It also exposes host and port for each service
through the docker object in the form of a map.

Dependency: generateDockerComposeFile

### Gradle task stopDockerCompose

The `stopDockerCompose` task stops the docker compose services.

Dependency: runDockerCompose 

### Gradle task cleanDockerCompose

The `cleanDockerCompose` task cleans the stopped docker compose services.

Dependency: stopDockerCompose 

                               
### Gradle task tagImage

The `tagImage` task tags the image built during `buildImage` with the `project.version` (as tag) and `docker.imageName`
(as name). The task will fail in case `project.version` or `docker.imageName` doesn't exist.
The full image name including tag is exposed to Gradle via the docker object.
E.g. the `build.gradle` can contain:

    version = '1.1-SNAPSHOT'
    docker.imageName = 'my-image'

Dependency: buildImage

### Gradle task pushImage

The `pushImage` task pushes the image tagged during `tagImage`. All logic for repositories, namespaces etc. comes from
the `docker.imageName`.

Dependency: tagImage

## Get the plugin

The plugin is currently released to [Bintray][bt], while snapshots are pushed to [JFrog][jf] under [builds][jfb].

[bt]: https://bintray.com/qkrijger/gradle-plugins/docker-gradle
[jf]: https://oss.jfrog.org
[jfb]: https://oss.jfrog.org/artifactory/webapp/#/builds/gradle-docker
