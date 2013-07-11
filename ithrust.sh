#!/bin/bash

NATIVE_DIR=lib/native/`uname -m`
CLASSPATH=lib/ithrust.jar:lib/itchy.jar:lib/jame.jar
MAIN=uk.co.nickthecoder.ithrust.Thrust


java -Djava.library.path=${NATIVE_DIR} -classpath "${CLASSPATH}" "${MAIN}" "$@"

