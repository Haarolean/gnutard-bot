FROM ghcr.io/graalvm/native-image:ol8-java17-22 AS builder

LABEL maintainer="Roman Zabaluev<gpg@haarolean.dev>"

RUN microdnf update \
 && microdnf install --nodocs \
    tar \
    gzip \
 && microdnf clean all \
 && rm -rf /var/cache/yum

ARG USER_HOME_DIR="/root"

RUN <<EOF

mkdir /opt/maven

maven_version=$(curl -fsSL https://repo1.maven.org/maven2/org/apache/maven/apache-maven/maven-metadata.xml  \
      | grep -Ev "alpha|beta" \
      | grep -oP '(?<=version>).*(?=</version)'  \
      | tail -n1)

maven_download_url="https://repo1.maven.org/maven2/org/apache/maven/apache-maven/$maven_version/apache-maven-${maven_version}-bin.tar.gz"

echo "Downloading [$maven_download_url]..."

curl -fL $maven_download_url | tar zxv -C /opt/maven --strip-components=1

EOF

ENV MAVEN_HOME /opt/maven
ENV M2_HOME /opt/maven
ENV PATH="/opt/maven/bin:${PATH}"

ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

WORKDIR /build

COPY . /build

RUN mvn -DskipTests --no-transfer-progress clean package

################################################################################################

FROM azul/zulu-openjdk-alpine:21-jre-headless

RUN apk add gcompat

RUN addgroup -S tardbot && adduser -S tardbot -G tardbot

USER tardbot

EXPOSE 8080

VOLUME config

#COPY --from=builder /build/target/core /kapybro
COPY --from=builder /build/target/gnutardbot.jar "/gnutardbot.jar"

CMD java -jar gnutardbot.jar

#HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
#            CMD curl --silent --fail --request GET http://localhost:8080/actuator/health \
#            | jq --exit-status -n 'inputs | if has("status") then .status=="UP" else false end' > /dev/null || exit 1