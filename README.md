[![CircleCI](https://circleci.com/gh/echsylon/blocks-callback.svg?style=shield)](https://circleci.com/gh/echsylon/blocks-callback)[![Coverage Status](https://coveralls.io/repos/github/echsylon/blocks-callback/badge.svg)](https://coveralls.io/github/echsylon/blocks-callback)[![Download](https://api.bintray.com/packages/echsylon/maven/callback/images/download.svg)](https://bintray.com/echsylon/maven/callback/_latestVersion)
# Callback
This is a simple callback infrastructure. It will allow you to define "tasks" and attach three different types of optional callback listeners; `SuccessListener`, `ErrorListener` and `FinishListener`. The library also offers a `CallbackManager` impelementation to manage the attached listeners for you and, when the time is right, deliver the result of the task through them.

## Include
Add a gradle dependency in your module build script like so:
```groovy
dependencies {
    compile 'com.echsylon.blocks:callback:{version}'
}
```

## The `Request` interface: Separation of concerns
This library will enable the below asynchronous "task" management:

```java
public class MyActivity extends AppCompatActivity {

    @Override
    public void onResume() {
        super.onResume();
        
        showProgressDialog();
        JsonResponse.execute("http://api.project.com/path/to/resource?id=123")
            .withFinishListener(this::hideProgressDialog)
            .withErrorListener(this::showError)
            .withSuccessListener(this::parseJson);
    }
    
    private void hideProgressDialog() {
        // Hide the progress dialog
    }
    
    private void showError(Throwable cause) {
        // Show an error snackbar
    }
    
    private void parseJson(String json) {
        // Parse the JSON and populate UI
    }
}
```

And in order to achieve Nirvana you'll have to implement your custom task something like this:

```java
public class JsonRequest extends FutureTask<String> implements Request<String> {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    
    public static JsonRequest execute(final String url) {
        Request<String> request = new JsonRequest(() -> {
            String json = getJsonContent(url); // Somehow get the JSON. Heavy work.
            return json;
        });
        
        EXECUTOR_SERVICE.submit(request);
        return request;
    }

    private final CallbackManager callbackManager = new DefaultCallbackManager();
    private JsonRequest(Callable<String> callable) {
        super(callable);
    }
    
    @Override
    public Request<T> withSuccessListener(SuccessListener<T> listener) {
        callbackManager.addSuccessListener(listener);
        return this;
    }

    @Override
    public Request<T> withErrorListener(ErrorListener listener) {
        callbackManager.addErrorListener(listener);
        return this;
    }

    @Override
    public Request<T> withFinishListener(FinishListener listener) {
        callbackManager.addFinishListener(listener);
        return this;
    }
}
```

And the rest is just going to work by itself.
