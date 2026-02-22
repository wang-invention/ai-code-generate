FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/wang-ai-code-mother.jar /app/wang-ai-code-mother.jar
EXPOSE 8123
ENTRYPOINT ["java","-jar","/app/wang-ai-code-mother.jar"]