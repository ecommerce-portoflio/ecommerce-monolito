FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR application

COPY mvnw .
RUN chmod +x mvnw

COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests
RUN cp target/*.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:21-jre-alpine
WORKDIR application

COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]