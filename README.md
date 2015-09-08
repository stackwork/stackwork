[![Build Status](https://travis-ci.org/Krijger/docker-gradle.svg?branch=master)](https://travis-ci.org/Krijger/docker-gradle)
# docker-gradle
Gradle plugin for building, testing and deploying Docker images using docker compose

## Features

### Gradle docker object (runtime data)

The plugin exports a property object 'docker' that you can use in your Gradle build file.
The object is reachable via `project.docker.[property]`. For the following build tasks, these properties are

build task | property
---------- | -------------
buildImage | imageId


### Gradle task buildImage

The `buildImage` task executes a docker build command in the root of your project.
You can have only a single Dockerfile, and the deliverable of the project is a single Docker image.
The resulting image id is exposed to Gradle via the docker object.

