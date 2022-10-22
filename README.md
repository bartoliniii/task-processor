# Task Processor App

This is simple application that process asynchronous tasks with RabbitMQ,
store status/result in MongoDB and expose REST API that allows to create task and get result.

Tests are based on testcontainers, so running Docker engine is required to run tests.

### Limitations
Because of use testcontainers framework to build integration tests you have to have docker daemon available on localhost

### Run app
Requirements: `bash`, `docker` and `docker-compose`

To build and run app locally in Docker container use following command:
```
./build-and-run.sh
```

To run rabbitmq/mongodb for local development just run below command and run app from your IDE without additional profiles:
```
./run-dependencies.sh
```


### Usage of API

Creating task:
```
curl -X POST localhost:8080/task -H 'Content-Type: application/json' \
--data-raw '{
    "pattern": "test",
    "input": "aaaatesa"
}'
```

Getting statuses of all tasks:
```
curl localhost:8080/task
```

Getting status of task with given id:
```
curl localhost:8080/task/{id}
```
