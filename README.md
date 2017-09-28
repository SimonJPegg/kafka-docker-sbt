# kafka-docker-sbt
Trivial example of using Kafka &amp; Docker with an sbt build 

Requires both `sbt` and `Docker for mac`

Run:

```
sbt clean docker
docker-compose
```

or 
```$xslt
sbt clean dockerComposeUp
```

Should generate lines from Shakespeare's plays along with the year the were written.

 