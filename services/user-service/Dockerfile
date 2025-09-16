FROM eclipse-temurin:17-jdk

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests
RUN cp target/*.jar app.jar

EXPOSE 8080
#CMD ["java", "-jar", "target/user-service-*.jar"]
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]
