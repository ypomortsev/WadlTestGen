#!/bin/sh
mvn exec:java -Dexec.mainClass="com.pomortsev.wadltestgen.WadlTestGen" -Dexec.args="$*"
