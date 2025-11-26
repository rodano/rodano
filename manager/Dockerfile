FROM python:3.14.0

LABEL org.opencontainers.image.authors="info@rodano.ch"
LABEL org.opencontainers.image.description="Rodano manager"

#install OS dependencies
RUN apt-get update && apt-get install -y zip jq docker-cli && apt-get clean

#install Python dependencies
COPY website/requirements.txt /app/website/requirements.txt
RUN pip install -r /app/website/requirements.txt

#copy application files
COPY website/ /app/website
COPY scripts/ /app/scripts
RUN chmod -R +x /app/scripts

WORKDIR /app

ENTRYPOINT ["python", "website/server.py"]
