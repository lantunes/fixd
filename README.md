# Fixd

Fixd is an HTTP server fixture for testing web clients. Unlike a mock web server, it 
is backed by a fully functional HTTP server, bound to a port of your choosing. Its 
fluent interface allows you to quickly and easily script responses to client requests.
Its clear, declarative interface also makes the setup portion of your unit tests easy
to read and understand.

Fixd is backed by a lean, high-performance Java HTTP server, requiring little overhead,
and allowing a server instance to be created per test. You no longer have to share a
single, expensive fixture between all tests. 

With Fixd, you can:

* create complex routing rules, based on HTTP method, content type, and URI patterns
* use sessions for tests that require state between requests
* capture requests and make assertions on them
* configure asynchronous HTTP responses
* setup asynchronous HTTP subscribe-broadcast scenarios 
* delay responses for tests that require a delayed response

Finally, Fixd is not limited to being used in the context of unit tests. It can also be
used as a standalone HTTP server, in any context. In this sense, it can also be viewed
as a Java micro web framework.

### Table of Contents

* [Getting Started](#getting-started)
* [Path Parameters](#path-parameters)
* [Splat Parameters](#splat-parameters)
* [Request Values](#request-values)
* [Capturing Requests](#capturing-requests)
* [URI Pattern Matching](#uri-pattern-matching)
* [Sessions for Stateful Requests](#sessions-for-stateful-requests)
* [Setting Headers in the Response](#setting-headers-in-the-response)
* [Delaying a Response](#delaying-a-response)
* [Periodic Asynchronous Responses](#periodic-asynchronous-responses)
* [Subscribe-Broadcast](#subscribe-broadcast)
* [Handling Requests By Content Type](#handling-requests-by-content-type)
* [Custom Request Handling](#custom-request-handling)
* [Content Marshalling](#content-marshalling)
* [Convenient Redirects](#convenient-redirects)
* [Escaping Interpolated Values](#escaping-interpolated-values)
* [Tear Down](#tear-down)

### Getting Started

Download [the latest .jar](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=org.bigtesting&a=fixd&v=LATEST). 
It depends on SLF4J, and the Simple Framework (http://www.simpleframework.org), amongst others.
Or, add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>org.bigtesting</groupId>
    <artifactId>fixd</artifactId>
    <version>1.0.3</version>
</dependency>
```

Next, you should declare an **org.bigtesting.fixd.ServerFixture** field, like "server", and 
initialize it before each test:

```java
@Before
public void beforeEachTest() throws Exception {
  /*
   * Instantiate the server fixture before each test.
   * It could also have been initialized as part of the
   * server field declaration, as JUnit will 
   * instantiate this Test class for each of its tests.
   */
  server = new ServerFixture(8080);
  server.start();
}
```

Then, we can write some tests. Here's a test demonstrating a simple GET:

```java
server.handle(Method.GET, "/")
      .with(200, "text/plain", "Hello");
        
Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/")
                .execute()
                .get();
       
assertEquals("Hello", resp.getResponseBody().trim());
```

### Path Parameters

You can also use path parameters as part of your request, and access them in the 
body of your response:

```java
server.handle(Method.GET, "/name/:name")
      .with(200, "text/plain", "Hello :name");
       
Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/name/Tim")
                .execute()
                .get();
       
assertEquals("Hello Tim", resp.getResponseBody().trim());
```

### Splat Parameters

You can use splat parameters as part of your request, and access them in the
body of your response:

```java
server.handle(Method.GET, "/firstName/*/lastName/*")
      .with(200, "text/plain", "Hello *[0] *[1]");
       
Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/firstName/John/lastName/Doe")
                .execute()
                .get();
       
assertEquals("Hello John Doe", resp.getResponseBody().trim());
```

In the example above, the *0* and *1* represent the indices of the splat
parameters in the request path.

### Request Values

You can access the request body in the body of your response:

```java
server.handle(Method.PUT, "/name")
      .with(200, "text/plain", "Hello [request.body]");
//supported request properties: body, method, time, path, query, major, minor, target
   
Response resp = new AsyncHttpClient()
                .preparePut("http://localhost:8080/name")
                .setBody("Tim")
                .execute()
                .get();
   
assertEquals("Hello Tim", resp.getResponseBody().trim());
```

...and the request parameters:

```java
server.handle(Method.GET, "/greeting")
      .with(200, "text/plain", "Hello [request?name]");
 
Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/greeting?name=Tim")
                .execute()
                .get();
   
assertEquals("Hello Tim", resp.getResponseBody().trim());
```

...and also the request headers:

```java
server.handle(Method.GET, "/say-user-agent")
      .with(200, "text/plain", "Value: [request$User-Agent]");
 
Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/say-user-agent")
                .execute()
                .get();
   
assertEquals("Value: NING/1.0", resp.getResponseBody().trim());
```

### Capturing Requests

You can capture requests, and make assertions on the captured requests:

```java
server.handle(Method.GET, "/say-hello")
      .with(200, "text/plain", "Hello!");
        
server.handle(Method.PUT, "/name/:name")
      .with(200, "text/plain", "OK");

new AsyncHttpClient()
    .prepareGet("http://localhost:8080/say-hello")
    .execute().get();

new AsyncHttpClient()
    .preparePut("http://localhost:8080/name/Tim")
    .execute().get();

CapturedRequest firstRequest = server.request();
assertEquals("GET /say-hello HTTP/1.1", firstRequest.getRequestLine());

CapturedRequest secondRequest = server.request();
assertEquals("PUT /name/Tim HTTP/1.1", secondRequest.getRequestLine());
```

You can limit the number of captured requests that are stored by calling
**server.setMaxCapturedRequests()**.

### URI Pattern Matching

Path parameters can be required to conform to certain rules, specified through Regex 
expressions:

```java
server.handle(Method.GET, "/name/:name<[A-Za-z]+>")
      .with(200, "text/plain", "Hello :name");
       
Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/name/Tim")
                .execute()
                .get();
assertEquals("Hello Tim", resp.getResponseBody().trim());
        
resp          = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/name/123")
                .execute()
                .get();
assertEquals(404, resp.getStatusCode());
``` 

### Sessions for Stateful Requests

You can create sessions for requests, and access the session variables in the body of
your response:

```java
server.handle(Method.PUT, "/name/:name")
      .with(200, "text/plain", "OK")
      .withSessionHandler(new PathParamSessionHandler());
        
server.handle(Method.GET, "/name")
      .with(200, "text/html", "Name: {name}");

WebClient client = new WebClient();
        
Page page = client.getPage(new WebRequest(new URL(
            "http://localhost:8080/name/Tim"), 
            HttpMethod.PUT));
assertEquals(200, page.getWebResponse().getStatusCode());
        
page      = client.getPage(new WebRequest(new URL(
            "http://localhost:8080/name"), 
            HttpMethod.GET));
assertEquals("Name: Tim", page.getWebResponse().getContentAsString().trim());
```

The **PathParamSessionHandler** above takes the value of the *name* path parameter and
creates an entry in the session called *name*. The *{}* syntax in the body of the response
means: get the current session for this client, and get the value for the *name* property.
(NOTE: You can also use a **RequestParamSessionHandler**, which is also provided.)

### Setting Headers in the Response

Setting headers in the response is easy:

```java
server.handle(Method.GET, "/")
      .with(302, "text/plain", "page moved")
      .withHeader("Location", "http://localhost:8080/new-location");
``` 

### Delaying a Response

You can delay a response, as well:

```java
server.handle(Method.GET, "/suspend")
      .with(200, "text/plain", "OK")
      .after(100, TimeUnit.SECONDS);

try {
            
  new AsyncHttpClient()
      .prepareGet("http://localhost:8080/suspend")
      .execute()
      .get(1, TimeUnit.SECONDS);
            
  fail("request should have timed out after 1 second");
        
} catch (Exception e) {}
```

The call to **after()** in the snippet above means: return the response after 100 seconds have
elapsed, which is more than the 1 second this client is willing to wait.

### Periodic Asynchronous Responses

You can asynchronously send a response at a fixed time interval, as below:

```java
server.handle(Method.GET, "/echo/:message")
      .with(200, "text/plain", "message: :message")
      .every(1, TimeUnit.SECONDS, 2);
        
final List<String> chunks = new ArrayList<String>();
ListenableFuture<Integer> f = new AsyncHttpClient()
      .prepareGet("http://localhost:8080/echo/hello")
      .execute(new AddToListOnBodyPartReceivedHandler(chunks));
        
assertEquals(200, (int)f.get());
assertEquals("[message: hello, message: hello]", chunks.toString());
```

The call to **every()** in the snippet above means: return the response every second, a maximum
of 2 times.

### Subscribe-Broadcast

You can also asynchronously send a response in a subscribe-broadcast scenario:

```java
server.handle(Method.GET, "/subscribe")
      .with(200, "text/plain", "message: :message")
      .upon(Method.GET, "/broadcast/:message");
        
final List<String> broadcasts = new ArrayList<String>();
ListenableFuture<Integer> f = new AsyncHttpClient()
      .prepareGet("http://localhost:8080/subscribe")
      .execute(new AddToListOnBodyPartReceivedHandler(broadcasts));
 
/* need some time for the above request to complete
 * before the broadcast requests can start */
Thread.sleep(50);
        
for (int i = 0; i < 2; i++) {
            
  new AsyncHttpClient()
      .prepareGet("http://localhost:8080/broadcast/hello" + i)
      .execute().get();
            
  /* sometimes the last broadcast request is not
  * finished before f.done() is called */
  Thread.sleep(50);
}
        
f.done(null);
assertEquals("[message: hello0, message: hello1]", broadcasts.toString());
```

In the example above, a request made to "/subscribe" will suspend the response, and the client
will await content from the server. When a separate request is made to "/broadcast", the 
suspended client will receive a message which includes the value of the *message* path parameter.

The call to **upon()** in the snippet above means: upon receiving a GET request
for "/broadcast/:message", send a response to the suspended client which contains
the value of the *message* path parameter in the body.

*NOTE: The implementation of the subscribe-broadcast feature is currently meant for use
in unit test contexts only. It is not currently meant for production-scale use. This will
hopefully change over time.*

### Handling Requests By Content Type

You can handle requests differently based on the request content type:

```java
server.handle(Method.GET, "/resource", "text/plain")
      .with(200, "text/plain", "Received text/plain content");
        
server.handle(Method.GET, "/resource", "application/json")
      .with(200, "text/plain", "Received application/json content");

Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/resource")
                .setHeader("Content-Type", "text/plain")
                .execute().get();
assertEquals("Received text/plain content", resp.getResponseBody().trim());

resp          = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/resource")
                .setHeader("Content-Type", "application/json")
                .execute().get();
assertEquals("Received application/json content", resp.getResponseBody().trim());
```

### Custom Request Handling

You are not limited to responding to requests with interpolated strings. You can also
provide your own custom request handling logic:

```java
server.handle(Method.GET, "/name/:name")
      .with(new HttpRequestHandler() {
        public void handle(HttpRequest request, HttpResponse response) {
            
            response.setStatusCode(200);
            response.setContentType("text/plain");
            response.setBody("Hello " + request.getPathParameter("name"));
            //you can also set a byte[] or InputStream in the response body
        }
    });
```

By providing an **HttpRequestHandler**, you have more control over how a 
request is handled.

### Content Marshalling

You can automatically marshal and unmarshal content using the content-type of
the request and response.

The following illustrates marshalling:

```java
server.marshal("application/json")
      .with(new JSONMarshaller());

server.handle(Method.GET, "/marshal")
      .with(200, "application/json", new SimplePojo("marshalledJSON"));

Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/marshal")
                .execute().get();

assertEquals("{\"val\":\"marshalledJSON\"}", resp.getResponseBody().trim());
```

In the example above, we register a Marshaller with the server, indicating
the content-type. Then, we return a custom entity that our Marshaller will
know how to convert. In this case, the SimplePojo has a single field called
*val*, and the entity is converted into JSON in the response to the client.

The following illustrates unmarshalling:

```java
server.unmarshal("application/json")
      .with(new JSONUnmarshaller());

server.handle(Method.PUT, "/unmarshal", "application/json")
      .with(new HttpRequestHandler() {
        public void handle(HttpRequest request, HttpResponse response) {
            response.setStatusCode(200);
            response.setContentType("text/plain");
            SimplePojo entity = request.getBody(SimplePojo.class);
            response.setBody(entity != null ? entity.getVal() : "error");
        }
    });

Response resp = new AsyncHttpClient()
                .preparePut("http://localhost:8080/unmarshal")
                .setHeader("Content-Type", "application/json")
                .setBody("{\"val\":\"unmarshalledJSON\"}")
                .execute().get();
assertEquals("unmarshalledJSON", resp.getResponseBody().trim());
```

In the example above, we register an Unmarshaller with the server, indicating
the content-type. Then, we call the **request.getBody()** method that accepts
the type the content should be unmarshalled to. In this case, the SimplePojo
has a single field called *val*, and the request content is converted from
JSON into a new instance of the entity.

### Convenient Redirects

You can send redirects conveniently:

```java
server.handle(Method.GET, "/")
      .withRedirect("http://localhost:8080/new-location");
```

...and you can do the same in a custom handler:

```java
server.handle(Method.GET, "/")
        .with(new HttpRequestHandler() {
          public void handle(HttpRequest request, HttpResponse response) {
              
              response.redirect("http://localhost:8080/new-location");
          }
      });
```

Each of the redirect methods above have signatures that allow you to specify 
a custom status code as well.

### Escaping Interpolated Values

You can escape interpolated values if you need to, using the *^* character:

```java
server.handle(Method.GET, "/name/:name")
      .with(200, "text/plain", "Hello ^:name :name");
       
Response resp = new AsyncHttpClient()
                .prepareGet("http://localhost:8080/name/Tim")
                .execute()
                .get();
       
assertEquals("Hello :name Tim", resp.getResponseBody().trim());
```

In the example above, the first occurrence of *:name* is prefixed by *^*,
indicating that the value should not be interpolated. You don't need to
escape *^* characters on their own, however, unless they occur next
to a value that would otherwise be interpolated.

### Tear Down

Finally, remember to stop the server fixture after each test:

```java
@After
public void afterEachTest() throws Exception {
  server.stop();
}
```

For more examples, have a look at the ServerFixture test class: [TestServerFixture](https://github.com/lantunes/fixd/blob/master/src/test/java/org/bigtesting/fixd/tests/TestServerFixture.java)

#### NOTE:

In the examples above, we're using the ning.com AsyncHttpClient. Check it out: 
[https://github.com/AsyncHttpClient/async-http-client](https://github.com/AsyncHttpClient/async-http-client) 

We're also using the HtmlUnit WebClient. Check it out:
[http://htmlunit.sourceforge.net/](http://htmlunit.sourceforge.net/)
