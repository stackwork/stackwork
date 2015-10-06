package nl.qkrijger.gradle.docker

enum DockerModuleType {
  TEST ('test'),
  TEST_IMAGE ('test-image'),

  private final String value

  private DockerModuleType(String s) {
    value = s;
  }

  String toString() {
    return this.value;
  }

  String getValue() {
    return value
  }
}