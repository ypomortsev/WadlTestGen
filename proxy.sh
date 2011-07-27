#!/bin/sh
mvn exec:java -q -Dexec.mainClass="com.pomortsev.wadltestgen.Proxy" -Dexec.args="$*"
