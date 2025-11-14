FROM debian:trixie

LABEL maintainer="matthieu.corageoud@usb.ch"
LABEL version="1.0.0"
LABEL description="Rodano development environment"

#run apt installation in multiple steps
#that's because some tools are required to install others (wget to retrieve the keys)
RUN apt update && apt -y install \
	curl \
	wget \
	gnupg2 \
	jq \
	unzip \
	vim \
	git \
	maven

RUN mkdir -p /etc/apt/keyrings

RUN curl -fsSL "https://deb.nodesource.com/gpgkey/nodesource.gpg.key" | gpg --dearmor | tee /etc/apt/keyrings/nodejs.gpg >/dev/null
COPY resources/nodejs.sources /etc/apt/sources.list.d/nodejs.sources

RUN curl -o /etc/apt/keyrings/mariadb.gpg "https://mariadb.org/mariadb_release_signing_key.pgp"
COPY resources/mariadb.sources /etc/apt/sources.list.d/mariadb.sources

RUN apt update && apt -y install \
	nodejs \
	mariadb-server \
	&& apt-get clean

RUN mkdir -p /opt/jdk
RUN wget https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.2%2B10/OpenJDK25U-jdk_x64_linux_hotspot_25.0.2_10.tar.gz
RUN tar -xvf OpenJDK25U-jdk_x64_linux_hotspot_25.0.2_10.tar.gz -C /opt/jdk
RUN update-alternatives --install /usr/bin/java java /opt/jdk/jdk-25.0.2+10/bin/java 1

ENTRYPOINT [ "/bin/bash" ]
