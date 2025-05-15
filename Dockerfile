FROM eclipse-temurin:23-jdk

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew build -x test --stacktrace

CMD ["java", "-Xmx400m", "-jar", "build/libs/app.jar"]
