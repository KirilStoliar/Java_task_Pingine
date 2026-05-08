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
Java_test_task
-- src/main/java/com/pingine/fleetpulse
   -- api/controller
      TripController
      VehicleController
   -- config
      JacksonConfig
      MongoSeedLoader
      OpenApiConfig
      RabbitConfig
      VehicleRegistryFeignConfig
   -- entity
      Trip
      VehicleEntity
      DriverEntity
      TelemetryPoint
      VehicleEnrichment
   -- dto
      TelemetryEvent
      TripResponse
      VehicleResponse
   -- integration
      VehicleRegistryClient
   -- messaging
      TelemetryConsumer
   -- error
      GlobalExceptionHandler
      VehicleNotFoundException
   -- repository
      VehicleRepository
      TripQueryRepository
      TelemetryRepository
      -- impl
         TripQueryRepositoryImpl
   -- service
      TripService
      VehicleService
      -- impl
         TripServiceImpl
   -- trip
      GeoDistance
      TripDetector

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

