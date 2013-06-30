# Fixd
------

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
* configure asynchronous HTTP responses
* setup asynchronous HTTP subscribe-broadcast scenarios 
* delay responses for tests that require a delayed response

## Getting Started
------------------

First you'll need to clone this project and build it (mvn clean install), and use the resulting jar. It has
only two dependencies: SLF4J, and the Simple Framework (http://www.simpleframework.org).
Fixd will eventually move to Maven central.

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

### Request Values

You can access the request body in the body of your response:

```java
server.handle(Method.PUT, "/name")
      .with(200, "text/plain", "Hello [request.body]");
   
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
      .withNewSession(new PathParamSessionHandler());
        
server.handle(Method.GET, "/name")
      .with(200, "text/html", "Name: {name}");

/* we're using the HtmlUnit client for this test */
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
Future<Integer> f = new AsyncHttpClient()
 .prepareGet("http://localhost:8080/echo/hello")
 .execute(
   new AsyncCompletionHandler<Integer>() {
            
     public Integer onCompleted(Response r) throws Exception {
       return r.getStatusCode();
     }
                
     public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
       String chunk = new String(bodyPart.getBodyPartBytes()).trim();
       if (chunk.length() != 0) chunks.add(chunk);
       return STATE.CONTINUE;
     }
 });
        
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
Future<Integer> f = new AsyncHttpClient()
 .prepareGet("http://localhost:8080/subscribe")
 .execute(
   new AsyncCompletionHandler<Integer>() {
              
     public Integer onCompleted(Response r) throws Exception {
       return r.getStatusCode();
     }
                 
     public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
       String chunk = new String(bodyPart.getBodyPartBytes()).trim();
       if (chunk.length() != 0) broadcasts.add(chunk);
       return STATE.CONTINUE;
     }
 });
        
/* sometimes the first broadcast request is made
* before the subscribing client has finished its request */
Thread.sleep(50);
        
for (int i = 0; i < 2; i++) {
            
  new AsyncHttpClient()
      .prepareGet("http://localhost:8080/broadcast/hello" + i)
      .execute().get();
            
  /* sometimes the last broadcast request is not
  * finished before f.cancel() is called */
  Thread.sleep(50);
}
        
f.cancel(false);
assertEquals("[message: hello0, message: hello1]", broadcasts.toString());
```

In the example above, a request made to "/subscribe" will suspend the response, and the client
will await content from the server. When a separate request is made to "/broadcast", the 
suspended client will receive a message which includes the value of the *message* path parameter.
For this to work, a client must first make a request to "/subscribe", otherwise no handler will
be available for "/broadcast".

The call to **upon()** in the snippet above means: upon receiving a GET request
for "/broadcast/:message", send a response to the suspended client which contains
the value of the *message* path parameter in the body.

### Tear Down

Finally, remember to stop the server fixture after each test:

```java
@After
public void afterEachTest() throws Exception {
  server.stop();
}
```

For more examples, have a look at the ServerFixture test class: [TestServerFixture](https://github.com/lantunes/fixd/blob/master/src/test/java/org/bigtesting/fixd/tests/TestServerFixture.java)
