package com.projectreact.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * Publisher sends all the objects requested.
 * Publisher sends all the object it has. (onComplete) subscriber and subscription will be canceled.
 * There is an error. (onError) -> subscriber and subscription will be canceled.
 *
 * Mono = 1 obj or nothing
 */
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "Petterson Santos";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("-------------------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Petterson Santos";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));
        log.info("-------------------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Petterson Santos";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing mono with error");});

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);
        log.info("-------------------------------------");
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "Petterson Santos";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));

        log.info("-------------------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "Petterson Santos";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5));

        log.info("-------------------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Petterson Santos";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something..."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s ->log.info("doOnSuccess executed"));

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5));

        log.info("-------------------------------------");
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .doOnNext(s -> log.info("Executing this doOnNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "Petterson Santos";

        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Executing this doOnNext");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "Petterson Santos";

        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Executing this doOnNext");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
