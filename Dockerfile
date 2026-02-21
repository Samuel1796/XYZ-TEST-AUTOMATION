# Multi-stage build for test automation
FROM maven:3.8.1-openjdk-17 as builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src
COPY allure.properties .

# Build the project
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-slim

# Install necessary tools
RUN apt-get update && apt-get install -y \
    chromium-browser \
    chromium-chromedriver \
    maven \
    git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built artifacts from builder
COPY --from=builder /app/pom.xml .
COPY --from=builder /app/src ./src
COPY --from=builder /app/allure.properties .

# Set environment variables for headless testing
ENV MAVEN_OPTS="-Xmx1024m -Xms512m"
ENV BROWSER=chrome
ENV HEADLESS_MODE=true

# Create directories for logs and reports
RUN mkdir -p logs screenshots target/allure-results target/allure-report

# Run tests when container starts
CMD ["mvn", "clean", "test", "-Dheadless.mode=true"]

