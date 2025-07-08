FROM mariadb:11.7

LABEL org.opencontainers.image.authors="info@rodano.ch"
LABEL org.opencontainers.image.description="Rodano database dumper container"

COPY dump.sh /usr/local/bin/dump.sh
RUN chmod +x /usr/local/bin/dump.sh

#disable entry point from the parent image
ENTRYPOINT []
