FROM node:22-alpine AS builder

WORKDIR /app

RUN npm config set "@rodano:registry" "https://npm.pkg.github.com/"

# main frontend
COPY main/package.json /app/main/package.json
RUN npm install --prefix /app/main
RUN npm run build:prod --prefix /app/main

# epro frontend
COPY epro/package.json /app/epro/package.json
RUN npm install --prefix /app/epro
RUN npm run build:prod --prefix /app/epro
RUN sed -i 's/<base href="\/">/<base href="\/eproapp\/">/g' /app/epro/www/index.html

FROM nginx:1.27-alpine

LABEL org.opencontainers.image.authors="info@rodano.ch"
LABEL org.opencontainers.image.description="Rodano frontend, containing the main frontend, the configurator and ePro"

# copy module bundles
COPY configurator/www /app/configurator
COPY --from=builder /app/main/www/browser /app/main
COPY --from=builder /app/epro/www /app/epro

# copy maintenance file
COPY maintenance.html /app/maintenance/

# copy Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
