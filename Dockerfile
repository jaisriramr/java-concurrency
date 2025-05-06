FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/fileprocessing-0.0.1-SNAPSHOT.jar app.jar

RUN echo $SPRING_REDIS_HOST

ENTRYPOINT ["sh", "-c", "java -Dspring.data.redis.host=$SPRING_REDIS_HOST -Dspring.data.redis.port=$SPRING_REDIS_PORT -jar app.jar"]

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]