set -e
./gradlew clean build
docker-compose build
docker-compose down
docker-compose up