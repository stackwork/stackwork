FROM qkrijger/wiremock:0.1

ADD frontendstub-config.json /root/mappings/config.json

RUN apt-get -y update
RUN apt-get -y install socat

RUN echo 'socat TCP-LISTEN:81,fork TCP:localhost:80 &' > /wiremock-on-two-ports
RUN echo 'java -jar /usr/lib/wiremock/wiremock-standalone.jar --port 80' >> /wiremock-on-two-ports

EXPOSE 81

ENTRYPOINT []
CMD [ "bash", "/wiremock-on-two-ports" ]
