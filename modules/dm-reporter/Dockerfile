FROM rocker/shiny:4.5.3

ENV RENV_CONFIG_REPOS_OVERRIDE="https://packagemanager.posit.co/cran/__linux__/noble/latest"

# Keep the development libraries so packages without a Linux binary can still build.
RUN apt-get update \
	&& apt-get install --yes --no-install-recommends \
		build-essential \
		curl \
		libcairo2-dev \
		libcurl4-openssl-dev \
		libssh2-1-dev \
		libssl-dev \
		libv8-dev \
		libxml2-dev \
	&& rm -rf /var/lib/apt/lists/*

# Copy necessary dirs and files:
RUN mkdir -p /opt/dm-reporter
## app, libs folder
COPY /app /opt/dm-reporter/app
## renv.lock file
COPY /renv.lock /opt/dm-reporter/renv.lock

WORKDIR /opt/dm-reporter

RUN Rscript -e 'install.packages("renv", repos = "https://cloud.r-project.org")' \
	&& Rscript -e 'renv::consent(provided = TRUE); renv::restore(prompt = FALSE)'

# Expose port
EXPOSE 3838

# Healthcheck to verify the Shiny app is responding
HEALTHCHECK --interval=5s --timeout=3s --start-period=10s --retries=3 \
	CMD curl --fail http://localhost:3838 || exit 1

# Run app on container start
CMD ["R", "-e", "shiny::runApp('/opt/dm-reporter/app', host = '0.0.0.0', port = 3838)"]
