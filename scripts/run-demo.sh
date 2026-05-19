#!/bin/bash
java -javaagent:../profiler-agent/target/profiler-agent-1.0.0.jar=package=com.demo,port=9999 \
     -cp ../profiler-demo-app/target/profiler-demo-app-1.0.0.jar:../profiler-agent/target/profiler-agent-1.0.0.jar \
     com.demo.DemoApp
