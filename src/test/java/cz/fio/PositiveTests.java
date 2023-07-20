package cz.fio;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

public class PositiveTests extends TestBase {

    @Test
    public void testContactStorage() {
        var randomUUID1 = UUID.randomUUID();
        var randomUUID2 = UUID.randomUUID();

        testContactStore(randomUUID1 + "John aldon", "Smith", "john.smith@email.com", 201);
        testContactStore("John", randomUUID2 + "Smith", "john.smith@email.com", 201);

        testContactStore(randomUUID1 + "John aldon", "Smith", "john.smith@email.com", 200);
        testContactStore("John", randomUUID2 + "Smith", "john.smith@email.com", 200);
    }


    @Test
    public void testParallelRequests() {
        ForkJoinPool.commonPool().submit(() ->
                Stream.iterate(0, i -> i + 1)
                        .limit(20)
                        .parallel()
                        .forEach(x -> testRepeatedMethod(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
        ).join();
    }

    public void testRepeatedMethod(UUID randomUUID1, UUID randomUUID2, UUID randomUUID3, UUID randomUUID4) {

        testContactStore(randomUUID1 + "John", "Smith", "john.smith@email.com", true);
        testContactStore("John", randomUUID2 + "Smith", "john.smith@email.com", true);
        testContactStore("John" + randomUUID3, "Smith", "john.smith@email.com", true);
        testContactStore("John", "Smith" + randomUUID4, "john.smith@email.com", true);

        testContactStore(randomUUID1 + "John", "Smith", "john.smith@email.com", true);
        testContactStore("John", randomUUID2 + "Smith", "john.smith@email.com", true);
        testContactStore("John" + randomUUID3, "Smith", "john.smith@email.com", true);
        testContactStore("John", "Smith" + randomUUID4, "john.smith@email.com", true);
    }
}
