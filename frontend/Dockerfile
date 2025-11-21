# Frontend Dockerfile (PRODUCTION)

# Stage 1 — Build with Node
FROM node:22 AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

COPY . .
RUN npm run build

# Stage 2 — Serve with nginx
FROM nginx:alpine

# Remove default NGINX website
RUN rm -rf /usr/share/nginx/html/*

# Copy Vite static build output
COPY --from=build /app/dist /usr/share/nginx/html

# SPA ROUTING 
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
