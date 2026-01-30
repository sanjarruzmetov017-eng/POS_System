#!/bin/bash
mvn compile dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp target/classes:$(cat cp.txt) com.smartpos.SmartPosLauncher
