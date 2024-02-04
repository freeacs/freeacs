FROM openjdk:22-ea-slim
WORKDIR /app
RUN apt-get update && apt-get install -y unzip
COPY target/*.zip /app/app.zip
RUN unzip *.zip && rm -rf *.zip
RUN mv *-* app
EXPOSE 8085
CMD ["sh", "/app/app/start.sh"]