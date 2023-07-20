package cz.fio;

import cz.fio.config.ContactsApiConfig;
import org.springframework.context.annotation.Profile;

@Profile("test")
@org.springframework.context.annotation.Configuration
public class TestContactsApiConfig extends ContactsApiConfig {

    @Override
    public String getContactsFilePath() {
        return ".\\test_data\\contacts.csv";
    }
}
