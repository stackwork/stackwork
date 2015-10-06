# use java:openjdk-8u66-jre as base image because this has curl and we know it is already downloaded
# for the project's main image, because it is the parent image of qkrijger/wiremock:0.1
FROM java:openjdk-8u66-jre
CMD ["wget", "-O-", "--retry-connrefused", "--waitretry", "1", "--dns-timeout", "1", "-t", "2", "not.the.correct.domain"]
