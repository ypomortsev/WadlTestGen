#!/bin/sh
java -cp "out/artifacts/WadlTestGen/WadlTestGen.jar:lib/freemarker.jar:lib/jcommander-1.13.jar:lib/snakeyaml-1.8.jar" com.pomortsev.wadltestgen.WadlTestGen $*
