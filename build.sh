#!/usr/bin/env bash
# Compile all Java sources, cleaning stale bytecode first.
cd "$(dirname "$0")" || exit 1
rm -f *.class
rm -f out/*.class
javac *.java
