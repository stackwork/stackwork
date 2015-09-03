#!/usr/bin/env sh

set -eu

dockerContextPath=$1
docker build $dockerContextPath
