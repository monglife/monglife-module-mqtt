# 🚀 Monglife Mqtt

```Mqtt```를 사용하는 모듈에서 의존하는 ```Mqtt Library``` 프로젝트입니다. 본 라이브러리는 ```Mqtt```메시지를 ```Conume```,```Publish```를 하는 경우 ```AOP```를 통해 코드의 가독성을 높이는데 활용됩니다. 본 라이브러리를 통해 ```Mqtt```를 사용하는 모듈이 어노테이션만 사용함으로써 ```Conume```,```Publish```이 가능하도록 지원합니다.

## 🛠 Configurations

### Gradle Configuration
```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

...

dependencies {
    implementation 'com.github.monglife:monglife-module-mqtt:1.0.1'
}
```

### Spring Configuration
```yaml
module:
  mqtt:
    host: <server>
    port: <port>
    user-name: <username>
    password: <password>
    connect-timeout: 30
    keep-alive-interval: 60
    automatic-reconnect: true
    consumer:
      enabled: true
      qos: 2
      base-topic: baseTopic
      completion-timeout: 5000
    publisher:
      qos: 2
      base-topic: baseTopic
      retaind: false
      async: true
```

## 🏗 Annotations

### - ```@MqttConsumer```
- 어노테이션 타입 : ```ElementType.TYPE```
- ```@RestController```와 유사하게 ```@MqttMapping(Mqtt Message Listener)``` 메서드를 담은 클래스를 컴포넌트화 하기 위한 어노테이션 입니다.
    ```java
    @MqttConsumer
    public class DemoConsumer {}
    ```

### - ```@MqttMapping```
- 어노테이션 타입 : ```ElementType.TYPE```,```ElementType.METHOD```
- ```@RequestMapping```과 유사하게 Mqtt Topic을 지정하여 메서드를 실행할 수 있도록 하는 어노테이션 입니다.
- ```value``` 값으로 수신하고 싶은 ```Topic```을 지정합니다.
- ```@MqttConsumer```를 가진 클래스에 지정하는 경우, 컴포넌트 내 메서드의 ```@MqttMapping```에 지정된 Topic의 접두어로 적용됩니다.
    ```java
    @MqttConsumer
    @MqttMapping("/topic")
    public class DemoConsumer {
  
        @MqttMapping("/demo")
        public void demo() {
            // listener topic is "/topic/demo"
        }
    }
    ```

### - ```@PathVariable```
- 어노테이션 타입 : ```ElementType.PARAMETER```
- Mqtt Listener 메서드에서 Topic 내의 값을 변수로 매핑하기 위한 어노테이션 입니다.
    ```java
    @MqttConsumer
    @MqttMapping("/topic")
    public class DemoConsumer {
  
        @MqttMapping("/demo/{id}")
        public void demo(@PathVariable("id") Long id) {
            // mapping "{id}" to "Long id"
        }
    }
    ```
    

### - ```@MqttPayload```
- 어노테이션 타입 : ```ElementType.PARAMETER```
- Mqtt Listener 메서드에서 ```Payload```를 매핑하기 위한 어노테이션 입니다.
    ```java
    @MqttConsumer
    @MqttMapping("/topic")
    public class DemoConsumer {
  
        @MqttMapping("/demo")
        public void demo(@MqttPayload PayloadDto payloadDto) {
            // mapping "mqtt payload" to "PayloadDto payloadDto"
        }
    }
    ```

### - ```@MqttConsumerAdvice```
- 어노테이션 타입 : ```ElementType.TYPE```
- ```@RestControllerAdvice```와 유사하게 ```@MqttExceptionHanlder(Mqtt Listener Exception Handler)``` 메서드를 담은 클래스를 컴포넌트화 하기 위한 어노테이션 입니다.
    ```java
    @MqttConsumerAdvice
    public class DemoConsumerAdvice {}
    ```

### - ```@MqttExceptionHandler```
- 어노테이션 타입 : ```ElementType.METHOD```
- ```@ExceptionHandler```와 유사하게 ```@MqttMapping```를 통한 리스너 메서드의 로직이 진행되는 중, 예외가 발생하는 경우에 전역적으로 예외 처리를 하기 위해 사용되는 어노테이션 입니다.
- 본 어노테이션은 ```@MqttConsumer``` 컴포넌트 내의 메서드 또는 ```@MqttConsumerAdvice``` 컴포넌트 내에 선언할 수 있으며, 이외의 컴포넌트에 선언하는 경우 스캔이 되지 않습니다.)
- 같은 예외 클래스를 처리하는 ```@ExceptionHandler```를 정의한 경우 우선 순위는 ```@MqttConsumer > @MqttConsumerAdvice```로 적용됩니다.
    ```java
    // Exception Handler In MqttConsumer
    @MqttConsumer
    @MqttMapping("/topic")
    public class DemoConsumer {
  
        ...
  
        @MqttExceptionHandler(RuntimeException.class)
        public void runTimeExceptionHandler(RuntimeException e) {
            // exception handling RuntimeException
        }
    }
    ```
    ```java
    // Exception Handler With MqttConsumerAdvice
    @MqttConsumerAdvice
    public class DemoConsumerAdvice {
  
        @MqttExceptionHandler(RuntimeException.class)
        public void runTimeExceptionHandler(RuntimeException e) {
            // exception handling RuntimeException
        }
    }
    ```

### - ```@MqttPublish```
- 어노테이션 타입 : ```ElementType.METHOD```
- Mqtt Message를 전송하는 메서드를 정의하기 위한 메서드 어노테이션 입니다.
- ```value```값으로 전송하기 위한 ```Topic```값을 지정합니다.
- 메서드의 반환 타입은 ```com.monglife.module.mqtt.dto.MqttResponseEntity```으로 해야 하며, 다른 타입으로 지정하는 경우 ```RuntimeException```이 발생합니다.
- ```PathVariable```를 사용하기 위해서는 ```{topic}```이라고 정의하고 ```MqttResponseEntity```의 ```topics```에 ```{topic}```에 들어갈 값을 담아서 ```MqttResponseEntity```를 반환합니다. 
- ```Payload```를 사용하기 위해서는 ```MqttResponseEntity```의 ```body```에 Dto를 담아서 ```MqttResponseEntity```를 반환합니다.
    ```java
    @Component
    public class DemoPublisher {
  
        @MqttPublish("/demo/topic/{topic}")
        public MqttResponseEntity<PayloadDto> demo(Long id, String message) {

            String topic = String.valueOf(id);
            PayloadDto body = new PayloadDto(message);

            return MqttResponseEntity
                    .body(body)
                    .topic(topic);
        }
    }
    ```