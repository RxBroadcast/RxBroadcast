FROM alpine:3.3
RUN apk --update add openjdk8-jre
ADD build/libs/rxbroadcast-0.0.1-tests.jar /opt
ENTRYPOINT ["java", "-classpath", "/opt"]
