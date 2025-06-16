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

# Créer le répertoire de l'application
WORKDIR /app

# Copier les fichiers nécessaires
COPY password-manager.jar /app/
COPY json-simple-1.1.1.jar /app/
COPY server-keystore.p12 /app/
COPY start.sh /app/

# Rendre le script exécutable
RUN chmod +x /app/start.sh

# Exposer le port
EXPOSE 9443

# Configuration de l'environnement Java pour SSL
ENV JAVA_OPTS="-Djavax.net.ssl.keyStore=/app/server-keystore.p12 -Djavax.net.ssl.keyStorePassword=password"

# Point d'entrée
CMD ["/app/start.sh"]
