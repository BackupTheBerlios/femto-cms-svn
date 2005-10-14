#! /bin/sh
# Copyright (C) 2005 mobizcorp Europe Ltd., all rights reserved.
if [ -z "$WINDIR" ]; then
  PATHSEP=':'
else
  PATHSEP=';'
fi

if [ -z "$JAVA_HOME" ]; then
  for dir in `ls -rd /opt/jre1.5* /opt/jdk1.5* 2>/dev/null`; do
    if [ -x "$dir"/bin/java ]; then
      JAVA_HOME="$dir"
      break;
    fi
  done
  if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME not set, and cannot find Java." 1>&2
    exit 1
  else
    export JAVA_HOME
  fi
fi

cp=bin
for jar in jar/*.jar; do
  cp="$cp$PATHSEP$jar"
done
