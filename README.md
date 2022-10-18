# Task Processor App

This is simple application that process asynchronous tasks with RabbitMQ,
store status/result in MongoDB and expose REST API that allows to create task and get result.

### Limitations
Because of use testcontainers framework to build integration tests you have to have docker daemon available on localhost

### Run app
Requirements: `bash`, `docker` and `docker-compose`

To run app locally use fallowing command:

```
./build-and-run.sh
```

To run rabbitmq/mongodb for local development just run below command and run app from your IDE without additional profiles:

```
# bash, docker and docker-compose is required
./run-dependencies.sh
```