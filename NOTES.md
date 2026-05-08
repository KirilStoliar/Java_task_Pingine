В качестве задания выполнил:
- пункт 1 (реализовал метод detect(List<TelemetryPoint>) в классе TripDetectorTest.java)
- пункт 2 (реализовал метод getLastTrip(String vehicleId) в классе TripServiceImpl.java),
добавил метод findByVehicleIdOrderByTsAsc(String vehicleId) в интерфейс TripQueryRepository 
и реализацию этого метода в TripQueryRepositoryImpl.java
- пункт 3 (реализовал парочку тестов в TripControllerTest.java)
- добавил сборку проекта в docker-compose
- создал Dockerfile
- создал application-docker.yml

Для меня неудобно видеть разросанные по проекту сущности, ошибки и т.д. 
Поэтому я бы изменил струкруту проекта на следующую:
Java_task_Pingine:
- src/main/java/com/pingine/fleetpulse
   - api/controller
      - TripController.java
      - VehicleController.java
   - config
      - JacksonConfig.java
      - MongoSeedLoader.java
      - OpenApiConfig.java
      - RabbitConfig.java
      - VehicleRegistryFeignConfig.java
   - entity
      - Trip.java
      - VehicleEntity.java
      - DriverEntity.java
      - TelemetryPoint.java
      - VehicleEnrichment.java
   - dto
      - TelemetryEvent.java
      - TripResponse.java
      - VehicleResponse.java
   - integration
      - VehicleRegistryClient.java
   - messaging
      - TelemetryConsumer.java
   - error
      - GlobalExceptionHandler.java
      - VehicleNotFoundException.java
   - repository
      - VehicleRepository.java
      - TripQueryRepository.java
      - TelemetryRepository.java
      - impl
         - TripQueryRepositoryImpl.java
   - service
      - TripService.java
      - VehicleService.java
      - impl
         TripServiceImpl.java
   - trip
      - GeoDistance.java
      - TripDetector.java

Данную архитектуру считаю более удобной для читаемости кода и поддержания в будущем. 
Также могу аргументировать тем, что при расширении проекта будет появляться, например, 
больше перехватчиков ошибок и очень удобно, если не нужно искать классы-перехватчики по 
всему проекту, а всё хранится в одной директории. Также директории должно соответствовать 
хранимой в ней информации и не содержать лишнего. 

Также не хватает логирования (удобного Slf4j) в ключевых точках проекта - эндпоинты и все методы
в сервисах.
Применял бы валидацию при вводе параметров пользователями.
Можно добавить description для каждого поля в Swagger, чтобы было понятно пользователю 
какие конкретно данные от него требуются.

