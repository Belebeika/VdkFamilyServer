FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

CMD ["java", "-Xmx400m", "-jar", "build/libs/app.jar"]
