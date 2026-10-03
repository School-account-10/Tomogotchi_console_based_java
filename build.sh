#!/usr/bin/env bash
cd "$(dirname "$0")" || exit 1
rm -f *.class
rm -f out/*.class
javac *.java
