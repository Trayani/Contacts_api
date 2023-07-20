package cz.fio;

import org.junit.jupiter.api.Test;

public class NegativeTests extends TestBase {

    @Test
    public void testInvalidEmails() {
        testContactStore("John", "Smith", "non-sense-email", 400);
        testContactStore("John", "Smith", null, 400);
        testContactStore("John", "Smith", "", 400);
        testContactStore("John", "Smith", "paul.example.com", 400);
    }

    @Test
    public void testNamesInvalidEmails() {
        testContactStore("", "Smith", "paul@example.com", 400);
        testContactStore(null, "Smith", "paul@example.com", 400);

        testContactStore("John", "", "paul@example.com", 400);
        testContactStore("John", null, "paul@example.com", 400);
    }
}
