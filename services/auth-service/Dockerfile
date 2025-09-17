FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8090
CMD ["java", "-jar", "-Dserver.port=8090", "target/auth-service-*.jar"]