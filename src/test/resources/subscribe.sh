#!/bin/bash

for var in "$@"
do
    for entry in "$var"/*.ttl
    do
        curl -X POST -d @"$entry" --header "Content-Type: text/turtle" http://lod2-test.wolterskluwer.de:2221/register
    done
done

