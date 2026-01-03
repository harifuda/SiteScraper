FROM mcr.microsoft.com/playwright/java:v1.50.0-noble

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY snapshots ./snapshots

RUN mvn install

COPY wait-for-it.sh .

CMD ["mvn", "exec:java", "-Dexec.mainClass=com.foobar.sitescraper.App"]