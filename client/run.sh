#!/usr/bin/env bash
export JAVA_PROGRAM_ARGS=`echo "$@"`
mvn exec:exec -Dexec.executable="java" -Dtruststore.path="-Djavax.net.ssl.trustStore=/home/pomatti/sandbox/mutual-tls/client/keystore.jks"