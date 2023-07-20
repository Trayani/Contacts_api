package cz.fio.api;

import cz.fio.config.ContactsApiConfig;
import cz.fio.contacts.StoreContactService;
import cz.fio.dto.ContactStorageResult;
import cz.fio.dto.StorageResultType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController()
@RequestMapping("/contacts")
@Validated
public class StoreContactController {

    @Autowired
    private ContactsApiConfig config;
    @Autowired
    private StoreContactService storeContactSerivce;
    private Logger logger = LoggerFactory.getLogger("StoreContactController");
    private Pattern multiSpaceRegex = Pattern.compile("\\s+");


    @PostMapping("storeContact")
    public ResponseEntity<ContactStorageResult> storeContactIfNotExists(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email
    ) {
        firstName = firstName.trim();
        lastName = lastName.trim();
        email = email.trim();


        if (firstName.isBlank() || lastName.isBlank() || email.isBlank())
            return new ContactStorageResult(
                    StorageResultType.BAD_REQUEST,
                    "'firstName', 'lastName' nor 'email' parameters cannot be blank."
            ).asResponseEntity();

        if (config.isValdateEmail() && !EmailValidator.getInstance().isValid(email))
            return new ContactStorageResult(StorageResultType.BAD_REQUEST, "'email' parameter did not have a valid value.")
                    .asResponseEntity();

        if (config.isNormalizeNames()) {
            firstName = formatName(firstName);
            lastName = formatName(lastName);
        }

        ContactStorageResult result = storeContactSerivce.findOrStoreContact(firstName.trim(), lastName.trim(), email.trim());
        return new ResponseEntity<>(result, result.type().responseStatus);
    }

    /** Upper-cases the first letters of the input while lower-cases the rest  */
    private String formatName(String input) {
        if (!input.contains(" "))
            return StringUtils.capitalize(input);

        var nameSegments = multiSpaceRegex.split(input.toLowerCase().trim());
        return Arrays
                .stream(nameSegments)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }
}
