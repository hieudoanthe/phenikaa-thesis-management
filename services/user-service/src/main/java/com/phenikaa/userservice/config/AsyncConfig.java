package com.phenikaa.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("UserService-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean
    public CallableProcessingInterceptor callableProcessingInterceptor() {
        return new CallableProcessingInterceptor() {
            @Override
            public <T> void beforeConcurrentHandling(org.springframework.web.context.request.NativeWebRequest request, Callable<T> task) throws Exception {
                // Log before processing
            }

            @Override
            public <T> void preProcess(org.springframework.web.context.request.NativeWebRequest request, Callable<T> task) throws Exception {
                // Log pre-processing
            }

            @Override
            public <T> void postProcess(org.springframework.web.context.request.NativeWebRequest request, Callable<T> task, Object concurrentResult) throws Exception {
                // Log post-processing
            }

            @Override
            public <T> Object handleTimeout(org.springframework.web.context.request.NativeWebRequest request, Callable<T> task) throws Exception {
                // Handle timeout gracefully
                return null;
            }

            @Override
            public <T> void afterCompletion(org.springframework.web.context.request.NativeWebRequest request, Callable<T> task) throws Exception {
                // Cleanup after completion
            }
        };
    }

    @Bean
    public DeferredResultProcessingInterceptor deferredResultProcessingInterceptor() {
        return new DeferredResultProcessingInterceptor() {
            @Override
            public <T> boolean handleTimeout(org.springframework.web.context.request.NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception {
                // Handle timeout gracefully
                deferredResult.setErrorResult("Request timeout");
                return false;
            }

            @Override
            public <T> void beforeConcurrentHandling(org.springframework.web.context.request.NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception {
                // Log before processing
            }

            @Override
            public <T> void preProcess(org.springframework.web.context.request.NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception {
                // Log pre-processing
            }

            @Override
            public <T> void postProcess(org.springframework.web.context.request.NativeWebRequest request, DeferredResult<T> deferredResult, Object concurrentResult) throws Exception {
                // Log post-processing
            }

            @Override
            public <T> void afterCompletion(org.springframework.web.context.request.NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception {
                // Cleanup after completion
            }
        };
    }
}
