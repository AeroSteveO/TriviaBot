FROM openjdk:8-alpine

RUN apk add --no-cache apache-ant

COPY . /usr/src/triviabot
WORKDIR /usr/src/triviabot
RUN ant clean-build
CMD ["ant", "run"]
