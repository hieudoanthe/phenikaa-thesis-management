FROM eclipse-temurin:17-jdk

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8089
CMD ["java", "-jar", "target/eval-service-*.jar"]
