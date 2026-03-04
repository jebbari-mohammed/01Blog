# Pre-built Angular app is copied from the host (built via npm run build -- --configuration production)
FROM nginx:alpine

# Copy built Angular app (Angular 17 output goes to dist/frontend/browser)
COPY dist/frontend/browser /usr/share/nginx/html

# Use custom Nginx config with API proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
