FROM openjdk:10.0.2-jdk-slim-sid
VOLUME /tmp
COPY target/blockchain-0.0.1.jar app.jar
ENTRYPOINT ["java", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/var/tmp/heapdump.bin", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
