FROM openjdk:11
ARG JAR_FILE


EXPOSE 8585

COPY ${JAR_FILE} nabu.jar
CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-jar", "nabu.jar"]
