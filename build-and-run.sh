set -e
./gradlew clean build
docker-compose down
docker-compose build
docker-compose up