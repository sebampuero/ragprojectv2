FROM node:24.12.0-alpine as build

WORKDIR /app

COPY package.json /app/package.json

RUN npm install --quiet

COPY . .

RUN npm run build

FROM nginxinc/nginx-unprivileged:stable-alpine

RUN rm -f /etc/nginx/conf.d/default.conf

COPY ./nginx/nginx.conf /etc/nginx/conf.d/app.conf

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8443

CMD ["nginx", "-g", "daemon off;"]      