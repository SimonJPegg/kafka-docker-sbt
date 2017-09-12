# kafka-docker-sbt
Trivial example of using Kafka &amp; Docker with an sbt build 

Requires both `sbt` and `Docker for mac`

Run:

```
sbt clean docker
docker-compose up
```
Should generate **two** Kafka messages and wait. Shutdown the containers with `Ctrl-C`.

 