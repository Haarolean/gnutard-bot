FROM ghcr.io/graalvm/native-image:ol8-java17-22 AS builder

LABEL maintainer="Roman Zabaluev<gpg@haarolean.dev>"

RUN microdnf update \
 && microdnf install --nodocs \
    tar \
    gzip \
    zip \
    unzip \
 && microdnf clean all \
 && rm -rf /var/cache/yum

ARG USER_HOME_DIR="/root"

RUN curl -s "https://get.sdkman.io" | bash

SHELL ["/bin/bash", "-c"]

RUN source "/root/.sdkman/bin/sdkman-init.sh" && sdk install gradle 8.7

ENV PATH="$PATH:/root/.sdkman/candidates/gradle/current/bin"

WORKDIR /build

COPY . /build

RUN gradle build

################################################################################################

FROM azul/zulu-openjdk-alpine:21-jre-headless

RUN apk add gcompat

RUN addgroup -S tardbot && adduser -S tardbot -G tardbot

USER tardbot

EXPOSE 8080

VOLUME config

COPY --from=builder /build/target/gnutardbot.jar "/gnutardbot.jar"

CMD java -jar gnutardbot.jar

#HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
#            CMD curl --silent --fail --request GET http://localhost:8080/actuator/health \
#            | jq --exit-status -n 'inputs | if has("status") then .status=="UP" else false end' > /dev/null || exit 1