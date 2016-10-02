#!/bin/bash

set -eu

if [ $(cqlsh -f query-data.cql cassandra | grep my_value_1 | wc -l) -eq 1 ] ; then exit 0 ; else exit 1 ; fi
