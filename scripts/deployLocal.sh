#!/usr/bin/env bash
cd "$(dirname "$0")"
cd ..
mvn clean package -DskipTests
cd scripts
docker-compose up -d --build
