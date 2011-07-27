#!/bin/sh
mvn exec:java -q -Dexec.mainClass="com.pomortsev.wadltestgen.WadlTestGen" -Dexec.args="$*"
