# Lurker

## Compile Environment

[//]: # (install libsqlite3-dev)

[//]: # ()
[//]: # (```shell)

[//]: # (apt install libsqlite3-dev)

[//]: # (```)


## build

```shell
docker run --rm -v "$(pwd)":/lurker -w /lurker openjdk:17.0.2-jdk-bullseye /bin/bash -c "./gradlew nativeBinaries"
```
