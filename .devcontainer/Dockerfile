FROM debian:bookworm

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

RUN curl -fsSL "https://deb.nodesource.com/gpgkey/nodesource.gpg.key" | gpg --dearmor | tee /etc/apt/keyrings/nodejs.gpg >/dev/null
COPY resources/nodejs.list /etc/apt/sources.list.d/nodejs.list

RUN curl -o /etc/apt/keyrings/mariadb.pgp "https://mariadb.org/mariadb_release_signing_key.pgp"
COPY resources/mariadb.list /etc/apt/sources.list.d/mariadb.list

RUN apt update && apt -y install \
	nodejs \
	mariadb-server \
	&& apt-get clean

RUN mkdir -p /opt/jdk
RUN wget https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.1%2B11/OpenJDK23U-jdk_x64_linux_hotspot_23.0.1_11.tar.gz
RUN tar -xvf OpenJDK23U-jdk_x64_linux_hotspot_23.0.1_11.tar.gz -C /opt/jdk
RUN update-alternatives --install /usr/bin/java java /opt/jdk/jdk-23.0.1+11/bin/java 1

ENTRYPOINT [ "/bin/bash" ]
