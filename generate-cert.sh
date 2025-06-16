#!/bin/bash

# Générer le keystore
keytool -genkeypair \
    -alias server \
    -keyalg RSA \
    -keysize 2048 \
    -keystore server-keystore.p12 \
    -storepass password \
    -storetype PKCS12 \
    -validity 365 \
    -dname "CN=localhost, OU=Dev, O=MyOrg, L=City, ST=State, C=FR"
