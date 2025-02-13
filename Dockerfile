FROM openjdk:11

ARG JAR_FILE
ARG VERSION
COPY ${JAR_FILE} /deployments/nabu-${VERSION}.jar


# Définition de l'encodage de l'environnement à UTF-8
ENV LANG=C.UTF-8

# Commande pour lancer l'application Spring Boot
