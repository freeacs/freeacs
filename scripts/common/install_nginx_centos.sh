#!/bin/bash

if rpm -q nginx; then
echo "Ngninx is already installed. If this is fresh install add the following to http.server configuration in nginx or similar"
echo "
    location /tr069 {
      proxy_set_header        X-Real-IP       \$remote_addr;
      proxy_pass http://localhost:8085/tr069;
    }
    location /web/ {
      proxy_pass http://localhost:8081/web/;
    }
    location /monitor/ {
      proxy_pass http://localhost:8090/monitor/;
    }
    location /webservice/ {
      proxy_pass http://localhost:8088/webservice/;
    }
    location /syslog/ {
      proxy_pass http://localhost:8086/syslog/;
    }
    location /core/ {
      proxy_pass http://localhost:8083/core/;
    }
    location /stun/ {
      proxy_pass http://localhost:8087/stun/;
    }
"
else
echo "nginx is not installed, installing it now"
yum install nginx -y
cat > /etc/nginx/nginx.conf <<- EOM
events {
  worker_connections  19000;
}

http {
  server {
    listen       80;
    server_name  localhost;

    # For the geeks: "A man is not dead while his name is still spoken." -Terry Pratchett
    add_header X-Clacks-Overhead "GNU Terry Pratchett";
    location = / {
      return 301 /web/index;
    }
    location /tr069/ {
      proxy_set_header        X-Real-IP       \$remote_addr;
      proxy_pass http://localhost:8085/tr069/;
    }
    location /web/ {
      proxy_pass http://localhost:8081/web/;
    }
    location /monitor/ {
      proxy_pass http://localhost:8090/monitor/;
    }
    location /webservice/ {
      proxy_pass http://localhost:8088/webservice/;
    }
    location /syslog/ {
      proxy_pass http://localhost:8086/syslog/;
    }
    location /core/ {
      proxy_pass http://localhost:8083/core/;
    }
    location /stun/ {
      proxy_pass http://localhost:8087/stun/;
    }
  }
}
EOM
systemctl enable nginx
systemctl restart nginx
fi