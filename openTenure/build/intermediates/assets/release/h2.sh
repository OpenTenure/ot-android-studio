#!/bin/sh
dir=../../../libs
java -cp "$dir/h2-1.3.175.jar:$H2DRIVERS:$CLASSPATH" org.h2.tools.Console "$@"