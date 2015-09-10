#!/usr/bin/env bash

set -eux

imageId=$1

docker inspect $imageId
docker rmi $imageId
