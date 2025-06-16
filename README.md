
# Password Manager avec RPC et SSL sous Docker

## 📝 Description
Application de gestion de mots de passe sécurisée utilisant **RPC** (Remote Procedure Call) et **SSL** pour la communication client-serveur, conteneurisée avec **Docker**.

---

## ✅ Prérequis

- Docker
- Java 17
- X11 (pour l'interface graphique)
- `keytool` (pour générer les certificats SSL)

---

## 📁 Structure du projet

```

passmanager/
├── \*.java                  # Fichiers source Java
├── json-simple-1.1.1.jar  # Librairie JSON
├── server-keystore.p12    # Keystore SSL
├── Dockerfile             # Dockerfile
├── start.sh               # Script de démarrage
├── manifest.txt           # Manifest pour le JAR
└── run.sh                 # Script de lancement complet

````

---

## 🔐 Configuration SSL

### Génération du keystore :

```bash
keytool -genkeypair \
  -alias server \
  -keyalg RSA \
  -keysize 2048 \
  -keystore server-keystore.p12 \
  -storepass password \
  -storetype PKCS12 \
  -validity 365 \
  -dname "CN=localhost, OU=Dev, O=MyOrg, L=City, ST=State, C=FR"
````

### Vérification du keystore :

```bash
keytool -list -keystore server-keystore.p12 -storepass password
```

---

## ⚙️ Construction de l'application

### Compilation Java :

```bash
javac -cp json-simple-1.1.1.jar:. *.java
```

### Création du fichier manifest :

```bash
echo "Main-Class: Main" > manifest.txt
```

### Génération du JAR :

```bash
jar cfm password-manager.jar manifest.txt *.class json-simple-1.1.1.jar
```

---

## 🐳 Docker

### Dockerfile :

```Dockerfile
FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

# Installation des dépendances
RUN apt-get update && apt-get install -y \
    openjdk-17-jre \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY password-manager.jar /app/
COPY json-simple-1.1.1.jar /app/
COPY server-keystore.p12 /app/
COPY start.sh /app/

RUN chmod +x /app/start.sh

EXPOSE 9443

ENV JAVA_OPTS="-Djavax.net.ssl.keyStore=/app/server-keystore.p12 -Djavax.net.ssl.keyStorePassword=password"

CMD ["/app/start.sh"]
```

---

## 🚀 Script de démarrage (`start.sh`)

```bash
#!/bin/bash
# Démarrer le serveur en arrière-plan
java $JAVA_OPTS -cp json-simple-1.1.1.jar:password-manager.jar Main &
SERVER_PID=$!

# Attendre le démarrage du serveur
sleep 2

# Démarrer le client
java -cp json-simple-1.1.1.jar:password-manager.jar cMain

# Arrêter le serveur quand le client se ferme
kill $SERVER_PID
```

---

## 🔧 Construction et exécution

### Construire l'image Docker :

```bash
docker build -t password-manager .
```

### Exécuter le conteneur :

```bash
xhost +local:docker

docker run -it \
  --name password-manager-server \
  -e DISPLAY=$DISPLAY \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  -v $(pwd)/server-keystore.p12:/app/server-keystore.p12 \
  --network host \
  password-manager
```

---

## 🧰 Script d'automatisation (`run.sh`)

```bash
#!/bin/bash

# Arrêter et supprimer l'ancien conteneur s'il existe
docker stop password-manager-server 2>/dev/null
docker rm password-manager-server 2>/dev/null

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
```

---

## ▶️ Utilisation

1. Cloner le projet
2. Générer le certificat SSL
3. Donner les droits d'exécution au script :

```bash
chmod +x run.sh
./run.sh
```

---

## 🔒 Sécurité

* Communication client-serveur sécurisée via SSL
* Stockage local sécurisé des mots de passe
* Isolation par conteneurisation Docker

---

## 🛠️ Dépannage

### Problèmes d'affichage X11 :

* Vérifiez que le serveur X11 fonctionne
* Exécutez : `xhost +local:docker`

### Erreurs SSL :

* Vérifiez que `server-keystore.p12` existe
* Vérifiez les permissions sur ce fichier

### Conflit de ports :

* Vérifiez que le port `9443` est libre :

```bash
netstat -tulpn | grep 9443
```

---

## ℹ️ Notes

* Le mot de passe du keystore est `password` (**à changer en production**)
* L'application utilise le port `9443` pour la communication sécurisée
* L'interface graphique nécessite un environnement X11 fonctionnel

---

## 🧹 Maintenance

### Nettoyer les ressources Docker :

```bash
docker stop password-manager-server
docker rm password-manager-server
docker rmi password-manager
```


