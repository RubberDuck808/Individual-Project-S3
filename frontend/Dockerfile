# Stage 1 — Build
FROM node:22 AS build
WORKDIR /app

# Copy dependencies
COPY package*.json ./
RUN npm ci

# Build-time arguments (pass secrets here)
ARG VITE_MAPBOX_TOKEN
ARG VITE_API_URL

# Set environment variables for Vite
ENV VITE_MAPBOX_TOKEN=$VITE_MAPBOX_TOKEN
ENV VITE_API_URL=$VITE_API_URL

# Optional debug (remove after verification)
RUN echo "VITE_MAPBOX_TOKEN=$VITE_MAPBOX_TOKEN"
RUN echo "VITE_API_URL=$VITE_API_URL"

# Copy project files
COPY . .

# Build frontend
RUN npm run build

# Stage 2 — Serve with Nginx
FROM nginx:alpine

# Remove default site
RUN rm -rf /usr/share/nginx/html/*

# Copy built frontend
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx config for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
