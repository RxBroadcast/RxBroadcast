FROM alpine:3.6
RUN apk --update add openjdk8-jre
ADD "build/libs/" "/opt/"
ENTRYPOINT ["java", "-classpath", "/opt/*"]
