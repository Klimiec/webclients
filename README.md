

## HTTP Client
Niniejszy artykuł ma na celu przedstawienie jak zaprojektować, przetestować i monitorować klienta restowego. 
Do artykułu dołączone jest repozytorium z klientami napisanymi z wykorzystanie między innymi: WebClient, RestClient, Retrofit, Ktor Client
[WebClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-webclient)
[RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-restclient)
[Retrofit](https://square.github.io/retrofit/)
[Ktor Client](https://ktor.io/docs/getting-started-ktor-client.html)
w których pokazane zostało jak wysłać i pobrać coś z zewnętrznej usługi, dodać warstwę cache oraz adaptera (anti-corruption layer).


### Wstęp

Dlaczego w ogóle potrzebujemy w projekcie obiektów, które enkpasulują wykorzystywane przez nas klienty HTTP? 
Po pierwsze chcemy odseparować naszą domenę do szczegółów technicznych, w jaki sposób pobieramy/wysyłamy dane oraz obsługi błędów, która w przypadku klientów http potrafi być naprawdę rozbudowana. 
Po drugie testowalność. Nawet jeżeli na co dzień nie wykorzystujemy [architektury hexagonalej](https://blog.allegro.tech/2020/05/hexagonal-architecture-by-example.html)  w naszej aplikacji warto starać się odeseparowywać wastwę infrastruktury od warstwy serwisów ponieważ poprawia to jej testowalność. 
Weryfikacja klienta http nie jest prosta i wymaga rozpatrzenia wielu przypadków. Posiadanie osobnej „foremki”, która enkapsuluje całość komunikacji zdecydowanie ułatwia testowalność. 
Próba przetestowania wszystkich przypadków od góry (end-to-end) może okazać się trudna lub wręcz niemożliwa. 
Po trzecie re-używalność. Raz napisanego klienta można z powodzeniem użyć w innych projektach.


### Client Design

Do analizy struktury klienta posłuży nam przykładowa implementacja wykorzystująca [retrofit](https://square.github.io/retrofit/) służąca do pobierania danych z usługi Order-Core-Service.

```kotlin
class OrderCoreServiceClient(
    private val orderCoreServiceApi: OrderCoreServiceApi,
    private val clientName: String,
) {
    suspend fun getOrdersFor(clientId: ClientId): List<Order> {
        logger.info { "[$clientName] Get orders for a clientId= ${clientId.clientId}" }
        return handleHttpResponse(
            response = orderCoreServiceApi.getOrdersFor(clientId.clientId.toString()),
            failureMessage = "[$clientName] Failed to get orders for clientId=${clientId.clientId}"
        ).also {
            logger.info("[$clientName] Returned orders for a clientId= ${clientId.clientId} $it")
        }
    }
}
```

#### Nazwa klasy

Jeżeli mam napisać integrację z usługą Order-Core-Service to taką „foremkę” nazwę ```OrderCoreServiceClient``` - ogólny pattern *EnternalServiceClient* (nazwa wołanej usługi + Client)

Jeżeli użyta przez nas technologia wykorzystuje interface do opisu wołanego REST API (RestClient, WebClient, Retrofit) to taki interfejs nazwiemy ```OrderCoreServiceApi``` - ogólny pattern *ExternalServiceApi* nazwa wołanej usługi + Api.

Nazwy te mogą wydawać się intuicyjne i oczywiste, ale jeżeli nie ma ustalonej konwencji nazwniczej szybko możemy skończyć z projektem w którym poszczególne integrację będą maiły następujące suffixy: OrderCoreService**HttpClient**, OrderCoreService**Facade**, OrderCoreService**WebClient**, OrderCoreService**Adapter**, OrderCore**Service**.
Ważne jest, aby mieć stałą konwencję i przestrzegać ją w całym projekcie.

#### Logowanie

```kotlin
class OrderCoreServiceClient(
    private val orderCoreServiceApi: OrderCoreServiceApi,
    private val clientName: String,
) {
    suspend fun getOrdersFor(clientId: ClientId): List<Order> {
        (1)     logger.info { "[$clientName] Get orders for a clientId= ${clientId.clientId}" }
                return handleHttpResponse(
                  response = orderCoreServiceApi.getOrdersFor(clientId.clientId.toString()),
        (2.1)     failureMessage = "[$clientName] Failed to get orders for clientId=${clientId.clientId}"
                ).also {
        (2.2)     logger.info("[$clientName] Returned orders for a clientId= ${clientId.clientId} $it")
                }
    }
}
```
Pobierając dane z zewnętrznej usługi logujemy początek interakcji czyli naszą **intencję** pobrania zasobu(1) oraz jej **efekt** (2). 
Przy czym efektem może być sukces(2.2) czyli zwrócenie odpowiedzi z kodem typu 2xx albo porażka(2.1).

Porażka może być sygnalizowana kodami błędu (3xx, 4xx, 5xx), wynikać z niemożności zdeserializowania otrzymanej odpowiedzi do obiektu, przekroczeniem czasu odpowiedzi etc. 
Ogólnie rzecz biorąc [bardzo dużo rzeczy może pójść nie tak](https://blog.allegro.tech/2015/07/testing-server-faults-with-Wiremock.html). W zależności od przyczyny porażki możemy chcieć zalogować rezultat interakcji na różnych poziomach (warn/error).
Są błędy krytyczne, które warto wyróżnić (error) oraz takie, które od czasu do czasu będą się pojawiać (warn) i nie wymagają pilnej interwencji.

Logi powinny zawierać informację o tym, jaki zasób chcemy pobrać, dla jakich parametrów oraz jakie dane otrzymaliśmy w wyniku zapytania. Szczegóły techniczne można ograniczyć do informacji o wołanym URL, użytej metodzie http oraz kodzie odpowiedzi. 
Wszystkie logi poprzedzamy nazwą serwisu, z którym się komunikujemy.


<blockquote>
OrderCoreServiceClient   : [order-core-service] Get orders for a clientId= ed50b5c0-03a1-4458-be63-d3f9df1b4a26</br>
okhttp3.OkHttpClient                     : --> GET http://localhost:8082/ed50b5c0-03a1-4458-be63-d3f9df1b4a26/order</br>
okhttp3.OkHttpClient                     : <-- 200 OK http://localhost:8082/ed50b5c0-03a1-4458-be63-d3f9df1b4a26/order (81ms, unknown-length body)</br>
OrderCoreServiceClient   : [order-core-service] Returned orders for a clientId= ed50b5c0-03a1-4458-be63-d3f9df1b4a26 [Order(orderId=7952a9ab-503c-4483-beca-32d081cc2446, categoryId=327456, countryCode=PL, clientId=1a575762-0903-4b7a-9da3-d132f487c5ae, price=Price(amount=1500, currency=PLN))]
</blockquote>

Przykład poprawnie zalogowanej interakcji.

Z logami jest jak z backupem. Dopiero kiedy ich potrzebujesz, bo albo biznes prosi o analizę jakiegoś przypadku albo rozwiązujemy incydent wtedy okazuje się czy je mamy i ile są warte.

#### Obsługa błędów

Znaczna część kodu klienta to obsługa błędów. Składa się ona z dwóch rzeczy: logowania oraz rzucania customowy wyjątków przykrywające wyjątki techniczne rzucane przez użytego klienta.
Już sama nazwa takiego customowego wyjątku mówi nam dokładnie co poszło nie tak. Dodatkowo można o nie zbudować wizualizacje na Kibanie pokazujące ile wyjątków danego typu wystąpiło w naszej usłudze.

Pisząc kod klienta chcemy maksymalnie uwypuklić to w jaki sposób wysyłamy/pobieramy dane i ukryć szum, który wynika z obsługi błędów. Obsługa błędów jest dość rozbudowana, ale na tyle generyczna, że powstały kod można napisać raz i re-użyć przy tworzeniu kolejnych klientów.

Ważne, aby przemyśleć wszystkie przypadki, które chcemy zaadresować i je przetestować.
Dokładny opis rozpatrywanych błędów znajduje się w sekcji testing. Zasadniczo im więcej przypadków rozpatrzymy i obsłużymy tym prostsza będzie analiza potencjalnych błędów.


### Testowanie

#### Zaślepki

Aby zweryfikować różne scenariusze działania naszego klienta HTTP należy odpowiednio zaślepić wołane endpointy w testach. W tym celu użyjemy biblioteki [wiremock](https://wiremock.org/).

Dość istotne jest, aby szczegóły techniczne tworzonych zaślepek nie wyciekały do testów.
Test powinien opisywać testowane zachowanie i <ins>enkapsulować szczegóły techniczne</ins>.
Zmiana frameworku do zaślepiania endpointów nie powinna mieć wpływu na sam test.
W tym celu dla każdej usługi, dla której piszemy klienta tworzymy obiekt typu ```StubBuilder```. StubBuilder pozwala ukryć szczegóły stubbowania i weryfikacji wołanych endpointów za czytelnym API.

```kotlin
stubs.orderCoreService().willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer())
```

Czytając powyższy kod od razu wiemy z **jakim** serwisem odbywa się interakcja (Order Core Service) oraz **co** zostanie z niego zwrócone (Orders).
Szczegóły techniczne stubbowanego enpointu, czyli **jak** to jest zrobione wyniesione zostały do obiektu StubBuilder'a. Testy powinny uwypuklać **co** i enkapsulować **jak**. Dzięki czemu mogą one pełnić role dokumentacji.

```kotlin
fun willReturnOrdersFor(
    clientId: ClientId,
    response: List<Order>,
) {
    WireMock.stubFor(
        getOrdersFor(clientId).willReturn(
            WireMock.aResponse()
                .withFixedDelay(responseTime)
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(response))
        )
    )
}
```
Szczegóły stubbowanego endpointy schowane są za metodą ```willReturnOrdersFor```


#### Dane Testowe

Dane zwracane przez nasze stubby możemy przygotować na 3 sposoby:


- a) Odczytać cały response z pliku/stringa
- b) Przygotować response z wykorzystaniem obiektów wykorzystywanych w usłudze do deserializacji odpowiedzi z wołanych serwisów.
- c) Utworzyć zestaw osobnych obiektów modelujących zwracany response z usługi na potrzeby testów.

Który wariant wybrać?
Aby odpowiedzieć na to pytanie należy przeanalizować wady i zalety każdego z nich.


###### Opcja A

Tworzenie odpowiedzi z serwisów jest bardzo szybkie i proste.
Pozwala **zweryfikować kontrakt** pomiędzy klientem a supplierem (przynajmniej na czas pisania testu). Wyobraźmy sobie, że podczas refactoringu przypadkowo zmianie uległo jego z pól w obiekcie odpowiedzi. W takim przypadku testy klienta wykryją powstały defekt, zanim kod jeszcze trafi na produkcję.

Trzymanie danych w plikach/stringach jest niestety trudne w utrzymaniu i re-użyciu.
Programiści czesto kopjują całe pliki na potrzeby nowych testów wprowadzając w nich minimalne zmiany. Pojawia się problem z nazywaniem tych plików i ich refactoringiem, kiedy wołany serwis wprowadzić niekompatybilną zmianę.


###### Opcja B

Pozwala pisać jednolinijkowe, czytelne asercje oraz maksymalnie re-używać powstałe już dane.
Zwłaszcza z wykorzystaniem [test data builders](https://www.natpryce.com/articles/000714.html).

```kotlin
    @Test
    fun `should return orders for a given clientId`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        val clientOrders = OrderCoreServiceFixture.ordersPlacedByPolishCustomer(clientId = clientId.toString())
        stubs.orderCoreService().willReturnOrdersFor(clientId, response = clientOrders)

        // when
        val response = orderCoreServiceClient.getOrdersFor(clientId)

        // then
        response shouldBe clientOrders
    }

```
Z drugiej strony defekt w postaci **naruszenia kontraktu** pomiędzy klientem a supplier nie zostanie wyłapany. W Efekcie możemy mieć idealnie przetestowaną komunikację w testach integracyjncyh, która na produkcji nie zadziała.


###### Opcja C

Ma wszystkie zalety opcji A oraz B, czyli utrzymywalność, re-używalność oraz weryfikację kontraktu pomiędzy klientem a supplierem. Niestety utrzymanie osobnego modelu na potrzeby testów wiąże z pewnym narzutem i dyscypliną po stronie developerów, którą trudno utrzymać.

Którą opcję wybrać?
Osobiście preferuję hybrydę opcji A oraz B.
Na potrzeby testu ścieżki „happy path’” w testach klienta zwracam response, który jest w całości zapisany w stringu (alternatywnie można go odczytać z pliku). Taki test pozwala nie tylko zweryfikować kontrakt, ale również poprawność dezerializacji otrzymanej odpowiedzi do obiektu.

W pozostałych testach (cache, adapter) oraz na poziomie testów end-to-end odpowiedzi zwracane przez stubbowany endpoint tworzę z wykorzystaniem tych samych obiektów do którym następnie będe deserializował otrzymaną odpowiedź.

Przykładowe dane testowe warto wynieść do osobnej dedykowanej klasy typu Fixture dla każdej integracji (e.g. ```OrderCoreServiceFixture```). Pozwala to lepiej reużywać powstały już kod i podnosi czytelność samych testów.


#### Scenariusze Testowe


###### Happy Path

**Pobranie zasobu**</br>
Weryfikacja czy klient jest w stanie pobrać dane z zaślepionego wcześniej endpointu oraz zdeserializować je do obiektu odpowiedzi.

```kotlin
@Test
fun `should return orders for a given clientId`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stubs.orderCoreService().willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer())

        // when
        val response = orderCoreServiceClient.getOrdersFor(clientId)

        // then
        response.size shouldBe 1
        response[0].orderId shouldBe "7952a9ab-503c-4483-beca-32d081cc2446"
        response[0].categoryId shouldBe "327456"
        response[0].countryCode shouldBe "PL"
        response[0].clientId shouldBe "1a575762-0903-4b7a-9da3-d132f487c5ae"
        response[0].price.amount shouldBe "1500"
        response[0].price.currency shouldBe "PLN"
}
```

Sekcja asercji (then) może lekko przerażać, ale to rezultat struktury obiektu responsu, tego, w jaki sposób przygotowane zostały dane testowe oraz tego, co chcemy zweryfikować. 
Ważną częścią testu dla happy path jest weryfikacja kontraktu między clientem a supplierem. Metoda ```ordersPlacedByPolishCustomer()``` zwraca przykładowy response gwarantowny przez suppliera (usługa order-core-service). Natomiast po stronie klienta tej usługi w teście następuje jego weryfikacja. Na sztywny przepisujemy fragmenty kontraktu z metody ```ordersPlacedByPolishCustomer()``` w spodziewane miejsca w obiekcie response w sekcji asercji. Ta duplikacja jest spodziewana i pożądana. To właśnie ona pozwala wykryć potencjalny defekt złamania kontraktu, który może się pojawić w wyniku błędów w strukturze obiektu responsu po stronie klienta.


**Wysłanie zasobu**</br>
Weryfikacja czy klient wysyła dane na wskazany URL w formacie akceptowalnym przez zastubbowany wcześniej endpoint.

```kotlin
@Test
fun `should successfully publish InvoiceCreatedEvent`(): Unit = runBlocking {
    // given
    val invoiceCreatedEvent = HermesFixture.invoiceCreatedEvent()
    stubs.hermes().willAcceptInvoiceCreatedEvent()

    // when
    hermesClient.publish(invoiceCreatedEvent)

    // then
    stubs.hermes().verifyInvoiceCreatedEventPublished(event = invoiceCreatedEvent)
}
```
Stubbowane endpointy metod akceptujących request body (e.g. POST, PUT) nie powinny weryfikować wartości otrzymanego request body a jedynie jego <ins>strukturę</ins>.

```kotlin
fun willAcceptInvoiceCreatedEvent() {
    WireMock.stubFor(
        invoiceCreatedEventTopic()
            .withRequestBody(WireMock.matchingJsonPath("$.invoiceId"))
            .withRequestBody(WireMock.matchingJsonPath("$.orderId"))
            .withRequestBody(WireMock.matchingJsonPath("$.timestamp"))
            .willReturn(
                WireMock.aResponse()
                    .withFixedDelay(responseTime)
                    .withStatus(HttpStatus.OK.value())
            )
    )
}
```

Zawartość request body weryfikujemy natomiast w sekcji asercji (// then). Tutaj również techniczne aspekty wykonania asercji chcemy ukryć za metodą.


```kotlin
stubs.hermes().verifyInvoiceCreatedEventPublished(event = invoiceCreatedEvent)
```


```kotlin
fun verifyInvoiceCreatedEventPublished(event: InvoiceCreatedEventDto) {
    WireMock.verify(1,
        WireMock.postRequestedFor(WireMock.urlPathEqualTo("/topics/${HermesApi.TOPIC_INVOICE_CREATED_EVENT}"))
            .withRequestBody(WireMock.matchingJsonPath("$.invoiceId", WireMock.equalTo(event.invoiceId)))
            .withRequestBody(WireMock.matchingJsonPath("$.orderId", WireMock.equalTo(event.orderId)))
            .withRequestBody(WireMock.matchingJsonPath("$.timestamp", WireMock.equalTo(event.timestamp)))
    )
}
```

Dlaczego nie warto łączyć stubbowania i weryfikacji requestu w jednej metodzie?
Robienie zaślepek w ten sposób powoduje, że korzystanie z nich staje się mało komfortowe (niski Developer Experience). Nie w każdym teście bowiem chcemy dokładnie weryfikować co jest wysyłane w request body.

Zdecydowana większość testów będzie zaślepiać (stubbować) endpoint na zasadzie zaakcepuj danych request tak długo jak jego struktura jest zachowana i będzie weryfikowała inne hipotezy niż to co zostało wysłane w body requestu (głownie testy end-to-end).


###### Client Error - błąd po stronie klienta

Dla błędów typu 4xx zweryfikować chcemy następujące przypadki:

- a) Braku szukanego zasobu sygnalizowane kodem błędu 404 i customowym wyjątkiem ```ExternalServiceResourceNotFoundException```
- b) Błąd walidacji sygnalizowany kodem błędu 422 i customowym wyjątkiem ```ExternalServiceRequestValidationException```
- c) Pozostałem błędy dla dowolnego kodu rzutowane na błąd ```ExternalServiceClientException```


```kotlin

    @ParameterizedTest(name = "{index}) http status code: {0}")
    @MethodSource("clientErrors")
    fun `when receive response with 4xx status code then throw exception`(
        exceptionClass: Class<Exception>,
        statusCode: Int,
        responseBody: String?,
    ): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stubs.orderCoreService().willReturnResponseFor(clientId, status = statusCode, response = responseBody)

        // when
        val exception = shouldThrowAny {
            orderCoreServiceClient.getOrdersFor(clientId)
        }

        // then
        exception.javaClass shouldBeSameInstanceAs exceptionClass
        exception.message shouldContain clientId.clientId.toString()
        exception.message shouldContain properties.clientName
    }
```

W systemach rozproszonych błąd typu [404](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/404) jest dość powszechny i może wynikać z chwilowej niespójności całego systemu (eventually consistent). Jego wystąpienie sygnalizuję wyjątkiem ```ExternalServiceResourceNotFoundException``` oraz logiem na poziomie warn. Tutaj jesteśmy zainteresowani bardziej skalą występowania niż analizą poszczególnych przypadków.


Sytuacja wygląda trochę inaczej w przypadku błędów o kodzie [422](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/422).
Jeżeli request został odrzucony z powodu błędów walidacji to albo nasza usługa ma defekt i produkuje błędne dane, albo pochodzą one z innej usługi (dlatego tak ważne jest, aby logować co przychodzi do nas z zewnętrznych serwisów). Ewentualnie błąd znajduje się po stronie odbiorcy w logice walidującej otrzymany request. Warto przeanalizować każdy taki przypadek dlatego błędy tego typu loguję na poziomie error i syngalizuję wyjątkiem ```ExternalServiceRequestValidationException```.

Pozostałe błędy z rodziny 4xx występują zdecydowanie rzadziej.
Oznaczam jest wszystkie poprzez wyjątek ```ExternalServiceClientException``` i logiem na poziomie error. 

###### Server Error - błąd po stronie servera

Bez względu na powód wystąpienia błędu 5xx loguje je wszystkie na poziomie warn i sygnalizujemy wyjątkiem ```ExternalServiceServerException```. Podobnie jak w przypadku błędów 404 tutaj również interesuje nas zbiorcza infomracja o tym ile tego typu błędów jest niż analiza każdego przypadku z osobna - dlatego poziom logowania warn.

W testach rozpatrujemy dwa przypadki poniżewasz response z usługi może posiadać body albo nie. Jeżeli response posiada body to chcemy je zalogować. 


###### Read Timeout

Jeżeli konfiguracja naszego klienta HTTP określa timeout na czas odpowiedzi, warto napisać test integracyjny, który zweryfikuje czy klienta został poprawnie skonfigurowany.
Opóźnienie stubbowane enpointu można zasymulować za pomocą metody ```withFixedDelay```.


```kotlin
        stubs.orderCoreService()
            .withDelay(properties.readTimeout.toInt())
            .willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer(clientId = clientId.toString()))
```

Czy to jest testowanie propertisów w testach? Nie.
Jest to weryfikacja, czy konfiguracja która pochodzi z propertisów faktycznie została zaaplikowana dla danego klienta. Dostarczenie odpowiedzi w określonym czasie może być częścią wymagań niefunkcjonalnych i wymaga przetestowania. 

###### Niepoprawny response body

Rozpatrywane przypadki:
- response body nie zawiera wymaganych pól
- response body jest pusty
- response ma niepoprawny format

Błędy tego typu sygnalizowane są poprzez  ```ExternalServiceIncorrectResponseBodyException``` i logiem na poziomie ERROR.


###### Weryfikacja metryk

Metryki stanowią ważne źródło informacji o zachowaniu naszego klienta (patrz sekcja metryki). Aby móc w prosty sposób rozróżnić metryki generowane przez różnych klientów warto wzbogacić je o tag **service.name** nadając mu odpowiednią wartość.

Poniższy test sprawdza czy jedna z matryk generowana przez naszego klienta **http.client.requests** zawiera wspomniany tag.


```kotlin
    @Test
    fun `verify custom tags for order-core-service (metrics)`(): Unit = runBlocking {
        // given
        meterRegistry.clear()
        // and
        val clientId = anyClientId()
        stubs.orderCoreService()
            .willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer(clientId = clientId.toString()))

        // when
        orderCoreServiceClient.getOrdersFor(clientId)

        // then
        meterRegistry.get("http.client.requests").tags("service.name", properties.clientName).timer()
            .count() shouldBeExactly 1
    }
```

### Metryki

TODO
