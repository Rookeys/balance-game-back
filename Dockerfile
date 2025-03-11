FROM eclipse-temurin:21-jdk AS build
WORKDIR /tmp
COPY . /tmp
RUN chmod +x ./gradlew && ./gradlew clean bootJar

FROM eclipse-temurin:21-jdk
WORKDIR /tmp
COPY --from=build /tmp/build/libs/balance-game-0.1.0.jar /tmp/BalanceGame.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /tmp/BalanceGame.jar"]