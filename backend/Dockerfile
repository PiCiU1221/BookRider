FROM eclipse-temurin:17-jre-jammy

ENV TZ=Europe/Warsaw
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Europe/Warsaw", "-jar", "app.jar"]
