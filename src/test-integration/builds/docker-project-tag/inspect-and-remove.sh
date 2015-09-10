#!/usr/bin/env bash

set -eux

imageName=$1
echo "imageName = ${imageName}"
version=$2
echo "version = ${version}"

imageRef="$imageName:${version}"

docker inspect $imageRef
docker rmi $imageRef
