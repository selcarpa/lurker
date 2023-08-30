FROM gradle:latest

WORKDIR /lurker
COPY ../lurker .

RUN ./gradlew build

