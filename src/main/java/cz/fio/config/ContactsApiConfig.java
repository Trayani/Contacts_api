package cz.fio.config;

import org.springframework.beans.factory.annotation.Value;

public abstract class ContactsApiConfig {

    @Value("${contactStore.caseInsensitiveContactMatch}")
    private boolean caseInsensitiveContactMatch;

    @Value("${contactStore.normalizeNames}")
    private boolean normalizeNames;

    @Value("${contactStore.valdateEmail}")
    private boolean valdateEmail;

    public boolean isCaseInsensitiveContactMatch() {
        return caseInsensitiveContactMatch;
    }

    public boolean isNormalizeNames() {
        return normalizeNames;
    }

    public abstract String getContactsFilePath();

    public boolean isValdateEmail() {
        return valdateEmail;
    }
}
