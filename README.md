## Бэкенд учебного приложения "Сервис совместного использования вещей" на основе микросервисной архитектуры с юнит и интеграционными тестами

### Стек технологий:
Java SE, Spring Boot, Hibernate, PostgreSQL, Maven, Docker, Lombok, JDBC, JUnit, mockito

### Функциональность
Приложение предоставляет пользователям возможность рассказывать, какими вещами они готовы поделиться, искать нужную вещь и брать её в аренду на время.
Позволяет не только бронировать вещь на определённые даты, но и закрывать к ней доступ на время бронирования от других желающих.
Если нужной вещи на сервисе нет, имеется возможность оставлять запросы. По запросу можно добавлять новые вещи для шеринга.

### Структура
Два сервиса:
* *gateway* - отвечает за взаимодействие с пользователями, содержит логику валидации входных данных (за исключением требующей работы с БД), переадресовывает запросы в основной сервис
* *server* - выполняет основную работу, в том числе, обращается к БД (PostgreSQL 42.5.0)

Сервисы и БД располагаются в отдельных Docker-контейнерах<br />
Порядок запуска контейнеров: БД, server, gateway<br />
Взаимодействие сервисов организовано через RestTemplate

### Запуск:
* требуется установленные Docker (при разработке использовалась версия 3.8) и Docker Compose
* склонировать репозиторий
* в корневой папке проекта открыть терминал и выполнить команду 'docker-compose up'

### Визуализация результатов работы
[Postman коллекция](https://github.com/Evgeny2835/ShareIt/blob/main/postman/sprint.json)

### ER диаграмма
![ER_diagram](ER_diagram.png)

### Основные сценарии:
<details>
  <summary>
    добавление новой вещи  
  </summary>
  
  * POST /item
</details>

<details>  
  <summary>
    редактирование вещи  
  </summary>
  
  * PATCH /items/{itemId}
  * изменить можно название, описание и статус доступа к аренде
  * редактировать вещь может только её владелец
</details>

<details>  
  <summary>
    просмотр владельцем списка всех его вещей  
  </summary>
  
  * GET /items
  * с указанием названия и описания для каждой вещи
</details>

<details>  
  <summary>
    поиск вещи потенциальным арендатором  
  </summary>
  
  * GET /items/search?text={text}
  * пользователь передаёт в строке запроса текст, и система ищет вещи, содержащие этот текст в названии или описании
  * в "text" передаётся текст для поиска
  * учитываются только доступные для аренды вещи
</details>

<details>  
  <summary>
    добавление нового запроса на бронирование  
  </summary>
  
  * POST /bookings
  * запрос может быть создан любым пользователем, а затем подтверждён владельцем вещи
  * после создания запрос находится в статусе WAITING — "ожидает подтверждения"
</details>

<details>  
  <summary>
    подтверждение или отклонение запроса на бронирование  
  </summary>
  
  * PATCH /bookings/{bookingId}?approved={approved}
  * параметр "approved" может принимать значения "true" или "false"
  * подтверждение может быть выполнено только владельцем вещи, затем статус бронирования становится либо APPROVED, либо REJECTED
</details>

<details>  
  <summary>
    получение данных о конкретном бронировании, включая статус  
  </summary>
  
  * GET /bookings/{bookingId}
  * запрос возможен инициатором бронирования либо владельцем вещи, к которой относится бронирование
</details>

<details>  
  <summary>
    получение списка всех бронирований текущего пользователя  
  </summary>
  
  * GET /bookings?state={state}
  * параметр "state" необязательный и по умолчанию равен ALL, также он может принимать значения CURRENT (текущие), PAST (завершённые), FUTURE (будущие), WAITING (ожидающие подтверждения), REJECTED (отклонённые)
  * бронирования возвращаются отсортированными по дате от более новых к более старым
</details>

<details>  
  <summary>
    добавление запроса вещи  
  </summary>
  
  * POST /requests)
  * основная часть запроса — текст запроса, где пользователь описывает, какая именно вещь ему нужна
</details>

<details>  
  <summary>
    получение списка своих запросов с данными об ответах на них  
  </summary>
  
  * GET /requests
  * для каждого запроса указываются описание, дата и время создания и список ответов
  * запросы возвращаются в отсортированном порядке от более новых к более старым
</details>

<details>
  <summary>
    получение списка запросов, созданных другими пользователями  
  </summary>
  
  * GET /requests/all?from={from}&size={size} <br />
  * пользователи могут просматривать запросы, на которые они могли бы ответить <br />
  * запросы сортируются по дате создания: от более новых к более старым, результаты возвращаются постранично, для чего передаются два параметра: "from" — индекс первого элемента, начиная с 0, и "size" — количество элементов для отображения
</details>
  
### Тестирование
Реализовано юнит и интеграционное тестирование<br />
Примеры кода:
```java
  @Test
    void create_shouldAnswer404WhenUserIsOwnerOfItem() throws Exception {
        when(bookingService.create(USER_ID, bookingCreateDto))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().is(404));
    }
```
```java
  @Test
    void create_shouldReturnNewUser() {
        UserDto userDto = userService.create(userCreateDto);
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("item_name")
                .description("item_description")
                .available(true)
                .build();
        itemService.create(userDto.getId(), itemCreateDto);

        String sql = "select * from items where name = ?";

        Item item = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToItem(rs),
                        itemCreateDto.getName())
                .stream()
                .findFirst()
                .orElse(null);

        assertNotNull(item);
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemCreateDto.getName()));
        assertThat(item.getDescription(), equalTo(itemCreateDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemCreateDto.getAvailable()));
        assertThat(item.getOwner().getId(), equalTo(userDto.getId()));
    }
```
