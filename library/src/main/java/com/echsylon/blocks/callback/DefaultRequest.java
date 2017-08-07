package com.echsylon.blocks.callback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

/**
 * This class implements the default behavior of a request. It interacts with a
 * shared executor service where all requests are being enqueued and executed
 * from. It also maintains a callback manager which will deliver the response
 * from this request to the caller.
 * <p>
 * NOTE! Any extending classes or direct consumers of this class MUST call the
 * {@link #enqueue(DefaultRequest)} method manually in order to have the request
 * enqueued and eventually executed.
 */
public class DefaultRequest<T> extends FutureTask<T> implements Request<T> {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);
    protected final DefaultCallbackManager<T> callbackManager;


    public DefaultRequest(Callable<T> callable) {
        super(callable);
        callbackManager = new DefaultCallbackManager<>();
    }


    /**
     * Enqueues the provided callable to the internal executor service. The
     * callable may be executed at any time after this method returns.
     *
     * @param request The request to enqueue.
     */
    public static void enqueue(DefaultRequest<?> request) throws RejectedExecutionException, NullPointerException {
        EXECUTOR.submit(request);
    }


    /**
     * Attaches a success listener to this request. Note that the listener will
     * only be called once and if the request produces an error, the listener
     * won't be called at all.
     *
     * @param listener The success listener.
     * @return This request object, allowing chaining of requests.
     */
    @Override
    public Request<T> withSuccessListener(SuccessListener<T> listener) {
        callbackManager.addSuccessListener(listener);
        return this;
    }

    /**
     * Attaches an error listener to this request. Note that the listener will
     * only be called once and if the request doesn't produce an error, the
     * listener won't be called at all.
     *
     * @param listener The error listener.
     * @return This request object, allowing chaining of requests.
     */
    @Override
    public Request<T> withErrorListener(ErrorListener listener) {
        callbackManager.addErrorListener(listener);
        return this;
    }

    /**
     * Attaches a finished state listener to this request. Note that the
     * listener will be called exactly once regardless if the produced result is
     * a success or a failure.
     *
     * @param listener The finish state listener.
     * @return This request object, allowing chaining of requests.
     */
    @Override
    public Request<T> withFinishListener(FinishListener listener) {
        callbackManager.addFinishListener(listener);
        return this;
    }

    /**
     * Used internally by the java concurrency framework. Don't call this method
     * on your own.
     */
    @Override
    protected void done() {
        super.done();

        try {
            callbackManager.deliverSuccessOnMainThread(get());
        } catch (InterruptedException | ExecutionException e) {
            callbackManager.deliverErrorOnMainThread(e.getCause());
        }
    }

}
