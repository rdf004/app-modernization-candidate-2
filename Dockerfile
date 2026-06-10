FROM registry.access.redhat.com/ubi9/openjdk-21:1.20 AS build
USER root
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests \
    -Dmaven.compiler.source=21 \
    -Dmaven.compiler.target=21

FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.20
LABEL maintainer="UBS DocPipeline Team"
LABEL description="Modernized Document \
Processing Pipeline — Java 21, Spring Boot 3.3, \
Camel 4.8, running on Red Hat UBI 9"

USER root
RUN microdnf install -y findutils \
    && microdnf clean all

RUN mkdir -p /data/incoming-docs \
    /data/incoming-docs/.done \
    /data/incoming-docs/.error \
    /data/processed \
    /data/reports \
    /data/reports/flagged \
    /data/locks \
    /data/nfs-incoming \
    && chown -R 185:0 /data

USER 185
WORKDIR /data
COPY --from=build \
  /build/target/doc-processing-pipeline.jar \
  /deployments/app.jar

ENV JAVA_OPTS="-Xms256m -Xmx1024m"
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8443
ENTRYPOINT ["java"]
CMD ["-jar", "/deployments/app.jar"]
