# azurite-event-camel

azurite-event-camel

## knowen issues

if you have this error

```declarative
[jbang] [ERROR] Could not download https://github.com/apache/camel/blob/HEAD/dsl/camel-jbang/camel-jbang-main/dist/CamelJBang.java
[jbang] Run with --verbose for more details. The --verbose must be placed before the jbang command. I.e. jbang --verbose run [...]
```

this error can happen after new camel release,remove all camel image and clear docker builder

```bash
docker builder prune
```

## DEV

container with name 'default' is used, can be changed in *Out.java file.

## compose profile

| profile | description                                   |
|---------|-----------------------------------------------|
| docker  | use camel as docker                           |
| local   | use local camel, log will me in compose/.logs |

### azure storage queue

```bash
export TARGET_NAME=BlobQueue
docker compose  --profile docker up 

### Kafka

```bash
export TARGET_NAME=Kafka
docker compose --profile kafka --profile docker up 
```

go to http://localhost:8000/ and check topic "azurite" after uploading file to the container.

### servicebus /amqp

```bash
export TARGET_NAME=Amqp
docker compose --profile servicebus --profile docker up 
```