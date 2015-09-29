#!/usr/bin/env bash

set -eux

# image reference can be an image name or id
imageReference=$1

docker inspect $imageReference
docker rmi $imageReference