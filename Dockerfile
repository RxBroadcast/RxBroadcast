FROM alpine:3.6
RUN apk --update add openjdk8-jre
ADD build/libs/rxbroadcast-1.2.1-tests.jar /opt
ENTRYPOINT ["java", "-classpath", "/opt"]
