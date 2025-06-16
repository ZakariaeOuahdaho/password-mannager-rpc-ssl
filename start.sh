#!/bin/bash
java $JAVA_OPTS -cp json-simple-1.1.1.jar:password-manager.jar Main &
SERVER_PID=$!
sleep 2
java -cp json-simple-1.1.1.jar:password-manager.jar cMain
kill $SERVER_PID
