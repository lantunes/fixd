/*
 * Copyright (C) 2014 BigTesting.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigtesting.fixd.core.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.core.RequestHandlerImpl;
import org.bigtesting.fixd.core.ResponseBody;
import org.bigtesting.fixd.marshalling.MarshallerProvider;
import org.bigtesting.fixd.marshalling.UnmarshallerProvider;
import org.bigtesting.fixd.request.impl.SimpleHttpRequest;
import org.bigtesting.routd.Route;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luis Antunes
 */
public class AsyncTask implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTask.class);

    private final Response subscriberResponse;
    private final Request subscriberRequest;
    private final RequestHandlerImpl handler;
    private final String responseContentType; 
    private final ResponseBody responseBody;
    
    private final List<Subscriber> subscribers;
    
    private final MarshallerProvider marshallerProvider;
    
    private final UnmarshallerProvider unmarshallerProvider;
    
    private Timer broadcastSubscribeTimeoutTimer;
    
    private Subscriber subscriber;
    private final ByteBuffer subscriberConnectedReadBuffer = ByteBuffer.allocate(8192);
    
    public AsyncTask(Request request, Response response, 
            RequestHandlerImpl handler,
            List<Subscriber> subscribers,
            String responseContentType, ResponseBody responseBody,
            MarshallerProvider marshallerProvider,
            UnmarshallerProvider unmarshallerProvider) {
        
        this.subscriberRequest = request;
        this.subscriberResponse = response;
        this.handler = handler;
        this.subscribers = subscribers;
        this.responseContentType = responseContentType;
        this.responseBody = responseBody;
        this.marshallerProvider = marshallerProvider;
        this.unmarshallerProvider = unmarshallerProvider;
    }

    public void run() {
        
        delayIfRequired(handler);
        
        if (handler.isSuspend()) {
            
            subscribe();
            
        } else {
        
            long period = handler.period();
            if (period > -1) {
                respondPeriodically(period);
            } else {
                responseBody.sendAndCommit(subscriberResponse, responseContentType);
            }
        }
    }

    private void subscribe() {
        
        subscriber = new Subscriber(handler);
        subscribers.add(subscriber);

        startTimeoutCountdownIfRequired();
        
        while(true) {
            try {
                
                Broadcast broadcast = subscriber.getNextBroadcast();
                broadcast.sent(false);
                
                if (!subscriberClientStillConnected()) {
                    break;
                }
                
                if (broadcast instanceof SubscribeTimeout) {
                    subscriberResponse.setStatus(Status.REQUEST_TIMEOUT);
                    subscriberResponse.getPrintStream().close();
                    break;
                }
                
                restartTimeoutCountdownIfRequired();
                
                delayIfRequired(handler);
                
                Request request = broadcast.getRequest();
                Route route = broadcast.getRoute();

                /* no support for session variables for now */
                ResponseBody handlerBody = handler.body(
                        new SimpleHttpRequest(request, null, route, unmarshallerProvider), 
                        subscriberResponse, marshallerProvider);
                
                handlerBody.send(subscriberResponse, responseContentType);
                
                broadcast.sent(true);
                
            } catch (Exception e) {
                logger.error("error waiting for, or handling, a broadcast", e);
            }
        }
        
        subscribers.remove(subscriber);
    }
    
    private boolean subscriberClientStillConnected() {
        
        try {
            subscriberConnectedReadBuffer.clear();
            int read =((SocketChannel)subscriberRequest.getAttribute("fixd-socket"))
                    .read(subscriberConnectedReadBuffer);
            if (read == -1) return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    private void delayIfRequired(RequestHandlerImpl handler) {
        
        long delay = handler.delay();
        if (delay > -1) {
            
            try {
                
                TimeUnit delayUnit = handler.delayUnit();
                long delayInMillis = delayUnit.toMillis(delay);
                Thread.sleep(delayInMillis);
                
            } catch (Exception e) {
                throw new RuntimeException("error delaying response", e);
            }
        }
    }

    private void respondPeriodically(long period) {
        
        TimeUnit periodUnit = handler.periodUnit();
        long periodInMillis = periodUnit.toMillis(period);
        final int times = handler.periodTimes();
        final Timer timer = new Timer("ServerFixtureTimer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            
            private int count = 0;
            
            @Override
            public void run() {
                try {
                    
                    if (times > -1 && count >= times) {
                        timer.cancel();
                        timer.purge();
                        subscriberResponse.getPrintStream().close();
                        return;
                    }
                    
                    responseBody.send(subscriberResponse, responseContentType);
                    
                    count++;
                    
                } catch (Exception e) {
                    logger.error("error sending async response at fixed rate", e);
                }
            }
        }, 0, periodInMillis);
    }
    
    private void startTimeoutCountdownIfRequired() {
        
        if (handler.hasTimeout()) {
            broadcastSubscribeTimeoutTimer = new Timer("TimeoutTask", true);
            broadcastSubscribeTimeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    subscriber.addNextBroadcast(new SubscribeTimeout());
                    broadcastSubscribeTimeoutTimer.cancel();
                    broadcastSubscribeTimeoutTimer.purge();
                }
            }, handler.timeoutUnit().toMillis(handler.timeout()));
        }
    }
    
    private void restartTimeoutCountdownIfRequired() {
        
        if (broadcastSubscribeTimeoutTimer != null) {
            broadcastSubscribeTimeoutTimer.cancel();
            broadcastSubscribeTimeoutTimer.purge();
        }
        startTimeoutCountdownIfRequired();
    }
}
