# mqtt-spring-boot-starter

#### 介绍
   MQTT Client 客户端服务，方便用户可以快速集成，详细请参考工程 mqtt-spring-boot-starter-example;

#### application-test.yml
```
spring:
  mqtt:
    url: tcp://127.0.0.1:1883
    userName: admin
    password: 123456
    inbound:
      clientId: iot-thinker-inbound
      topics: /test/123456
    outbound:
      clientId: iot-thinker-outbound
      topics: /test/123456
```
