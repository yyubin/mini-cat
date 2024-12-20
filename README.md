현재 구현한 코드는 BIO (Blocking I/O) 방식으로 동작하며, 톰캣의 전통적인 방식과 유사합니다. 요청마다 하나의 스레드를 할당하여 클라이언트의 연결을 처리하는 방식인데, 톰캣의 BIO 방식과의 관계를 포함해 이를 정리해보겠습니다.

#### 1. 전체적인 흐름  
   Main 클래스에서는 ServerSocket을 사용해 클라이언트의 요청을 수신합니다.
   클라이언트가 연결되면, ExecutorService의 submit 메서드를 통해 클라이언트 요청을 처리할 스레드를 할당합니다.
   각 클라이언트의 요청은 ClientHandler 객체에 의해 처리됩니다.
   ClientHandler는 요청을 읽고 응답을 작성하여 클라이언트에게 전송한 후, 연결을 종료합니다.
#### 2. BIO 방식  
   ServerSocket.accept() 메서드는 클라이언트의 연결을 기다리는 blocking 호출입니다. 클라이언트가 연결될 때까지 서버는 대기 상태에 놓이게 됩니다.
   각 요청은 새로운 스레드에서 처리됩니다. 클라이언트 요청을 받으면 ExecutorService를 사용하여 새로운 스레드가 할당되고, 해당 스레드는 클라이언트와의 I/O 작업을 처리합니다.
   각 클라이언트 요청마다 하나의 스레드를 생성하여 처리하는 방식은 **BIO (Blocking I/O)**에 해당합니다. 이는 톰캣이 과거에 사용했던 방식입니다. 클라이언트가 보내는 요청을 처리할 때까지 스레드가 blocking 상태로 대기합니다.
#### 3. ExecutorService  
   ExecutorService를 사용해 스레드를 관리하는 방식은 스레드 풀을 활용하여 스레드를 효율적으로 관리합니다.
   Executors.newFixedThreadPool(10)을 사용하여 최대 10개의 스레드를 유지하고, 클라이언트 요청을 처리할 때마다 풀에서 하나의 스레드를 꺼내서 요청을 처리합니다. 요청이 완료되면, 해당 스레드는 풀로 반환됩니다.  
   - 장점: 많은 요청을 받았을 때, 스레드 수를 제한하여 리소스를 절약할 수 있습니다. 새로 스레드를 생성하는 비용을 줄이고, 한정된 스레드를 재사용하여 성능을 향상시킬 수 있습니다.
#### 4. 톰캣에서의 BIO 방식과 차이점  
   톰캣도 과거에는 BIO 방식으로 동작했으나, 요청이 많아지면 성능 문제가 발생할 수 있었습니다. BIO 방식에서는 클라이언트가 보내는 요청을 처리할 때마다 새로운 스레드를 생성하고, 그 스레드가 요청을 처리하는 동안 blocking 상태로 대기합니다. 이런 방식은 클라이언트가 많은 경우 스레드 생성/소멸 비용과 메모리 소비가 커질 수 있습니다.
   톰캣의 NIO (Non-blocking I/O) 방식에서는 단일 스레드가 여러 클라이언트의 요청을 처리할 수 있도록 하여, 스레드 생성 비용을 줄이고, 동시성을 높였습니다. 이 방식은 요청에 대한 처리 결과가 준비되면, 해당 요청을 처리할 수 있는 스레드를 즉시 할당하고 다른 요청을 기다리지 않습니다.
#### 5. 차이점 및 비교  
   - BIO  
      - 장점: 구현이 간단하고, 각 요청마다 스레드 하나씩 할당하여 요청을 처리하므로 직관적입니다.
      - 단점: 클라이언트가 많아질수록 스레드 수가 급격히 증가하여 성능 저하가 발생할 수 있습니다. 메모리와 리소스 소모가 크고, 대량의 클라이언트 요청을 처리할 때 비효율적입니다.
   - NIO (톰캣)
     - 장점: 싱글 스레드로 여러 클라이언트 요청을 비동기적으로 처리할 수 있어 고성능을 제공합니다. 스레드의 수를 제한할 수 있어 리소스 소비를 줄일 수 있습니다.
     - 단점: 구현이 복잡하고, 비동기 처리에 대한 관리가 필요합니다. 특히 I/O 작업이 많이 발생하는 애플리케이션에서는 event-driven 방식으로 처리가 필요합니다.
#### 6. 현재 구현에서 개선할 점  
   - 스레드 수 관리
     - 현재 ExecutorService를 사용하여 고정된 스레드 수만큼만 스레드를 생성합니다. 스레드 풀의 크기를 동적으로 관리하거나 미리 스레드를 생성하여 커넥션을 대기하도록 하는 방법도 고려할 수 있습니다.
   - NIO 적용
     - 클라이언트가 많아질 경우, BIO 방식은 성능이 떨어지므로, 비동기 방식인 NIO로 전환을 고려할 수 있습니다. NIO는 톰캣에서 사용하는 방식으로, 하나의 스레드에서 여러 클라이언트를 처리할 수 있습니다.

---
#### 결론  
   현재 코드에서는 BIO 방식을 사용하고 있으며, ExecutorService로 스레드를 관리하여 클라이언트 요청을 처리하고 있습니다. 이 방식은 단기적으로 간단하고 직관적인 방법이지만, 클라이언트 수가 많아지면 성능 문제가 발생할 수 있습니다. 톰캣의 NIO 방식을 적용하면, 성능을 더욱 향상시킬 수 있으며, 많은 클라이언트를 처리하는 데 유리합니다.  
    이전에 서블릿 컨테이너의 요청-응답 래핑, 요청 라우팅 및 서블릿 처리, 환경 설정 등을 구현한 `JAVA-CRUD` 프로젝트와 결합하여 개선하면 실제 was 처럼 동작할 수 있을 것이라 사료됩니다.