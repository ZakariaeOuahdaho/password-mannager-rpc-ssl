#!/bin/bash

# Arrêter et supprimer l'ancien conteneur s'il existe
docker stop password-manager-server 2>/dev/null
docker rm password-manager-server 2>/dev/null

# Créer le script de démarrage
echo '#!/bin/bash
java $JAVA_OPTS -cp json-simple-1.1.1.jar:password-manager.jar Main &
SERVER_PID=$!
sleep 2
java -cp json-simple-1.1.1.jar:password-manager.jar cMain
kill $SERVER_PID' > start.sh

# Rendre le script exécutable
chmod +x start.sh

# Reconstruire l'image
docker build -t password-manager .

# Autoriser l'accès X11
xhost +local:docker

# Lancer le conteneur
docker run -it \
    --name password-manager-server \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -v $(pwd)/server-keystore.p12:/app/server-keystore.p12 \
    --network host \
    password-manager
