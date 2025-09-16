FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests && mv target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]