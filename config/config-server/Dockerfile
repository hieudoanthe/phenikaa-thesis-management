FROM eclipse-temurin:17-jre

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080
CMD ["java", "-jar", "target/config-server-*.jar"]
