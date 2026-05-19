#!/bin/sh

echo "C2SIM-SERVER Launch script"
echo

DEBUG_OPTS=${DEBUG:+-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005}

java $JAVA_OPTS $DEBUG_OPTS -jar server.jar
