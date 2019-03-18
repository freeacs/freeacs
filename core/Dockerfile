FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/*.zip /app/app.zip
RUN unzip *.zip && rm -rf *.zip
RUN mv *-* app
CMD ["sh", "/app/app/start.sh"]