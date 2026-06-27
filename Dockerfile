FROM eclipse-temurin:21
COPY build/libs/stats-0.0.1-SNAPSHOT.war app.war
ENTRYPOINT ["java","-jar","/app.war"]