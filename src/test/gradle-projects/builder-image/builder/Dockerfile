FROM alpine:3.4
RUN apk add --no-cache openssh-client
RUN echo 'echo "added via ssh by a builder image"' > /message
CMD ['scp', '/message', 'root@server:/']
