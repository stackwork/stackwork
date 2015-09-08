#!/usr/bin/env bash

set -eu

imageId=$1

docker inspect $imageId
docker rmi $imageId
