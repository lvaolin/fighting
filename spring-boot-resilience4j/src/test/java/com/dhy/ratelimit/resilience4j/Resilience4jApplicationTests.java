package com.dhy.ratelimit.resilience4j;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@SpringBootTest
class Resilience4jApplicationTests {

    @Test
    void contextLoads() {

        System.out.println("resilience4j test");
    }

    /**
     * 限速器：每秒钟请求速率限制
     */
    @Test
    void testRateLimiter() {
        LoginService loginService = new LoginService();
        // 限速配置信息
        RateLimiterConfig config = RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(100))
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(1)
                .build();
        // 生成一个限速器
        RateLimiter rateLimiter = RateLimiter.of("backendName", config);


       // while (true) {
            //将限速器和目标方法关联起来
            Supplier<Boolean> restrictedSupplier = RateLimiter
                    .decorateSupplier(rateLimiter, loginService::login);

            long start = System.currentTimeMillis();
            IntStream.rangeClosed(1, 500)
                    .parallel()
                    .forEach(i -> {
                        Try<Boolean> aTry = Try.ofSupplier(restrictedSupplier);
                        System.out.println(aTry.isSuccess());
                    });

            long stop = System.currentTimeMillis();
            System.out.println((stop - start)+"毫秒");

        // }

    }

    /**
     * 熔断器：当出现异常时进行断路处理
     */
    @Test
    public void testCircuitBreaker() {
        // Create a CircuitBreaker (use default configuration)
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig
                .custom()
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .build();
        CircuitBreaker circuitBreaker = CircuitBreaker
                .of("backendName", circuitBreakerConfig);
        while (true) {
            LoginService loginService = new LoginService();
            Boolean result = circuitBreaker.executeSupplier(() -> {
                System.out.println("----");
                return true;
            });
            System.out.println(result);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 当响应时间超过指定时间后进行取消、终止请求处理
     */
    @Test
    public void testTimelimiter() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(600))
                .cancelRunningFuture(true)
                .build();
        TimeLimiter timeLimiter = TimeLimiter.of(config);

        while (true) {
            LoginService loginService = new LoginService();

            Supplier<Future<Boolean>> futureSupplier = () -> {
                return executorService.submit(loginService::login);
            };
            Callable<Boolean> restrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);
            Try.of(restrictedCall::call)
                    .onFailure(throwable -> System.out.println("We might have timed out or the circuit breaker has opened."));

        }
    }

    /**
     * 重试器，当出现失败时进行重试
     */
    @Test
    public void testRetry(){
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendName");
        // Create a Retry with at most 3 retries and a fixed time interval between retries of 500ms
        Retry retry = Retry.ofDefaults("backendName");

        while (true) {
            LoginService loginService = new LoginService();
            // Decorate your call to BackendService.doSomething() with a CircuitBreaker
            Supplier<Boolean> decoratedSupplier = CircuitBreaker
                    .decorateSupplier(circuitBreaker, loginService::login);

            // Decorate your call with automatic retry
            decoratedSupplier = Retry
                    .decorateSupplier(retry, decoratedSupplier);

            // Execute the decorated supplier and recover from any exception
            Boolean result = Try.ofSupplier(decoratedSupplier)
                    .recover(throwable -> true).get();
            System.out.println(result);
        }

    }

    /**
     * 舱壁模式限流：限制同时执行的线程并发数，可以使用信号量实现也可以使用固定线程池实现
     */
    @Test
    public  void testBulkhead(){
        LoginService loginService = new LoginService();

        //bulkhead 配置信息
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(1)
               //.maxWaitDuration(Duration.ofMillis(100))
               // .writableStackTraceEnabled(true)
                //.fairCallHandlingStrategyEnabled(true)
                .build();

        //实例化一个叫 order 的限流器
        Bulkhead bulkhead = Bulkhead.of("order", bulkheadConfig);

        Function<String, String> stringStringFunction = Bulkhead.decorateFunction(bulkhead, loginService::hello);

        IntStream.range(1,1000)
                .parallel()
                .forEach((i)->{
                    String lval = stringStringFunction.apply("lval");
                    System.out.println(lval);
                });


    }

}
