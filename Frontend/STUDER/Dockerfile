# Multi-stage: build Angular y servir con nginx
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
# Copiar el build de Angular (soporta dist/recu-fe o dist/recu-fe/browser)
COPY --from=build /app/dist/studer /usr/src/app/dist
RUN if [ -d "/usr/src/app/dist/browser" ]; then \
      cp -r /usr/src/app/dist/browser/* /usr/share/nginx/html/; \
    else \
      cp -r /usr/src/app/dist/* /usr/share/nginx/html/; \
    fi
# Configuración nginx para SPA del frontend
COPY nginx-spa.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
