package me.oblomov.reactive;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveApplication.class, args);
    }

}

@Controller()
@ResponseBody
class CustomerController {

    private final CustomerRepository repository;

    CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers")
    Flux<Customer> get() {
        return this.repository.findAll();
    }
}

@Component
class Initializer implements ApplicationRunner {

    private final CustomerRepository repository;

    Initializer(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var customers = Flux.just("Abidin", "Taner", "Mehmet", "Engin")
                .map(name -> new Customer(null, name))
                .flatMap(this.repository::save);

        this.repository
                .deleteAll()
                .thenMany(customers)
                .subscribeOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(10)))
                .subscribe(System.out::println);
    }
}

record Customer(@Id Integer id, String name) {

}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

}

@Controller
@ResponseBody
class HealthRestController {
    private final ApplicationContext context;

    HealthRestController(ApplicationContext context) {
        this.context = context;
    }

    @GetMapping("/wait")
    Mono<Integer> relax() {
        return Mono.just(1)
                .doOnSubscribe(s-> System.out.println("Starting subscription"))
                .doFinally(s-> System.out.println("Stopping subscription"))
                .delayElement(Duration.ofSeconds(45));
    }

    @EventListener
    public void onHealthStateChange(AvailabilityChangeEvent<LivenessState> livenessStateChange) {
        var message = switch (livenessStateChange.getState()) {
            case CORRECT -> "Horay :)";
            case BROKEN -> "Shit happened!";
        };
        System.out.println(AvailabilityChangeEvent.class.getName() + " change: " + message);
    }

    @PostMapping("/down")
    public void down() {
        AvailabilityChangeEvent.publish(this.context, LivenessState.BROKEN);
    }
}



