FROM java:8

RUN apt-get update && apt-get install -y git

RUN apt-get autoremove -y && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV CHERRY=/home/cherry
RUN mkdir -p $CHERRY
WORKDIR $CHERRY

ADD . $CHERRY/

RUN ./gradlew fatJar

CMD ["/bin/bash"]