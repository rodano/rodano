FROM node:24-alpine AS builder

WORKDIR /app

# main frontend
COPY main/package.json /app/main/package.json
COPY main/package-lock.json /app/main/package-lock.json
RUN npm install --prefix /app/main
COPY main/ /app/main/
RUN npm run build:prod --prefix /app/main

FROM nginx:1.30-alpine

LABEL org.opencontainers.image.authors="info@rodano.ch"
LABEL org.opencontainers.image.description="Rodano frontend, containing the main frontend (including ePRO) and the configurator"

# copy module bundles
COPY configurator/www /app/configurator
COPY --from=builder /app/main/www/browser /app/main

# copy Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
