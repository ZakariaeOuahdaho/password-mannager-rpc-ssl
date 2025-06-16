#!/bin/bash

# Permettre l'accès à X11
xhost +local:docker

# Construire l'image
docker build -t password-manager .

# Lancer le conteneur avec support GUI et SSL
docker run -d \
    --name password-manager-server \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -v $(pwd)/server-keystore.p12:/app/server-keystore.p12 \
    -p 9443:9443 \
    password-manager
