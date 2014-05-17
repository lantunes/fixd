/*
 * Copyright (C) 2013 BigTesting.org
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

import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.core.RequestHandlerImpl;
import org.bigtesting.fixd.core.ResponseBody;
import org.bigtesting.fixd.request.impl.SimpleHttpRequest;
import org.bigtesting.fixd.routing.Route;
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

    private final Response response;
    private final RequestHandlerImpl handler;
    private final String responseContentType; 
    private final ResponseBody responseBody;
    
    private final BlockingQueue<Broadcast> broadcasts = 
            new LinkedBlockingQueue<Broadcast>();
    
    private final List<Queue<Broadcast>> subscribers;
    
    private Timer broadcastSubscribeTimeoutTimer;
    
    public AsyncTask(Response response, 
            RequestHandlerImpl handler,
            List<Queue<Broadcast>> subscribers,
            String responseContentType, ResponseBody responseBody) {
        
        this.response = response;
        this.handler = handler;
        this.subscribers = subscribers;
        this.responseContentType = responseContentType;
        this.responseBody = responseBody;
    }

    public void run() {
        
        delayIfRequired(handler);
        
        if (handler.isSuspend()) {
            
            handleBroadcasts();
            
        } else {
        
            long period = handler.period();
            if (period > -1) {
                respondPeriodically(period);
            } else {
                responseBody.sendAndCommit(response, responseContentType);
            }
        }
    }

    private void handleBroadcasts() {
        
        subscribers.add(broadcasts);
        
        startTimeoutCountdownIfRequired();
        
        while(true) {
            try {
                
                Broadcast broadcast = broadcasts.take();
                if (broadcast instanceof SubscribeTimeout) {
                    response.setStatus(Status.REQUEST_TIMEOUT);
                    response.getPrintStream().close();
                    break;
                }
                
                restartTimeoutCountdownIfRequired();
                
                delayIfRequired(handler);
                
                Request request = broadcast.getRequest();
                Route route = broadcast.getRoute();

                /* no support for session variables for now */
                ResponseBody handlerBody = handler.body(
                        new SimpleHttpRequest(request, null, route), response);
                
                handlerBody.send(response, responseContentType);
                
            } catch (Exception e) {
                logger.error("error waiting for, or handling, a broadcast", e);
            }
        }
        
        subscribers.remove(broadcasts);
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
                        response.getPrintStream().close();
                        return;
                    }
                    
                    responseBody.send(response, responseContentType);
                    
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
                    broadcasts.add(new SubscribeTimeout());
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
