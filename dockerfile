FROM java:8

WORKDIR /

COPY target/pubsub-1.0-SNAPSHOT-jar-with-dependencies.jar publisher.jar

CMD ["java","-jar","publisher.jar"]