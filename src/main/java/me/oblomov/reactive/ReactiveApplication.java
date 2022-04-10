package me.oblomov.reactive;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

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


