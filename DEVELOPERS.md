# Developer information

This document summarizes information relevant to committers and contributors. It includes information about the
development processes and policies as well as the tools we use to facilitate those.

## Welcome

Thanks for reading this document. We love to hear from people willing to contribute to this project. For now we are
working hard at getting the first version up and running. If you have an idea or feature request it's best to get in
touch with us before creating a pull request.

## Releasing

Our version numbers follow the [semantic versioning](http://semver.org/) style. e.g.
`major.minor.patch-<prerelease>+<metadata>`.

We differentiate between the following releases;

* snapshot
* release candidate
* final

Releases are created using the Nebula Release plugin. To create a release execute the following command:

    ./gradlew <candidate|final> -x artifactoryPublish -x bintrayUpload

This will create a git tag which will be pushed.

## References

We use Netflix's Nebula project for versioning and releasing. Please see the following links for more information on
relevant parts:

* [release plugin](https://github.com/nebula-plugins/nebula-release-plugin)
* [bintray plugin](https://github.com/nebula-plugins/nebula-bintray-plugin)
