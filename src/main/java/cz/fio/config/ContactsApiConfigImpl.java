package cz.fio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@org.springframework.context.annotation.Configuration
public class ContactsApiConfigImpl extends ContactsApiConfig {

    @Value("${contactStore.contactsFilePath}")
    private String contactsFilePath;

    @Override
    public String getContactsFilePath() {
        return contactsFilePath;
    }
}
