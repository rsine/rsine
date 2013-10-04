#!/bin/bash

curl -X POST -d @logOnConceptPrefLabelChangeSubscription.ttl --header "Content-Type: text/turtle" http://localhost:2221/register
