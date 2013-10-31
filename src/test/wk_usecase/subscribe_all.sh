#!/bin/bash

for entry in ./*
do
  curl -X POST -d @"$entry" --header "Content-Type: text/turtle" http://localhost:2221/register
done
