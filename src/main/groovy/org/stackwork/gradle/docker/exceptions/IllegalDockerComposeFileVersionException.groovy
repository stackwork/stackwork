package org.stackwork.gradle.docker.exceptions

class IllegalDockerComposeFileVersionException extends RuntimeException {
  IllegalDockerComposeFileVersionException(String composeFilePath, int i) {
    super("Docker compose file '$composeFilePath' was not version 1 or 2 (but '$i' instead) and is unsupported")
  }
}
