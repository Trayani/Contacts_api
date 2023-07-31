package cz.fio.contacts;

import cz.fio.config.ContactsApiConfig;
import cz.fio.dto.ContactStorageResult;
import cz.fio.dto.StorageResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;


@Service
public class StoreContactService {

    @Autowired
    private ContactsApiConfig config;
    private Logger logger = LoggerFactory.getLogger("StoreContactService");
    private final Object fileLock = new Object();
    private static String CSV_DELIMITER = ",";
    public static String NEW_LINE = System.lineSeparator();


    public ContactStorageResult findOrStoreContact(String firstName, String lastName, String email) {
        String contactCsvRow = firstName + CSV_DELIMITER + lastName + CSV_DELIMITER + email;
        return findOrStoreContact(contactCsvRow);
    }


    /**
     * Finds existing contacts record or stores a new one if it already exists
     *
     * @param contactCsvRow a full contact record in CSV-line format
     */
    public ContactStorageResult findOrStoreContact(String contactCsvRow) {
        return findOrStoreContact(contactCsvRow, true);
    }


    /**
     * Finds existing contacts record or stores a new one if it already exists
     *
     * @param contactCsvRow          a full contact record in CSV-line format
     * @param retryOnNonExistingFile if <b>true</b> and in case of @FileNotFound exception, a the contacts file will be created anew
     *                               and the contact record will be stored to it
     */
    private ContactStorageResult findOrStoreContact(String contactCsvRow, boolean retryOnNonExistingFile) {
        RandomAccessFile file = null;
        FileChannel channel = null;
        FileLock lock = null;
        try {
            synchronized (fileLock) {
                file = new RandomAccessFile(config.getContactsFilePath(), "rw");
                channel = file.getChannel();
                lock = channel.lock();
                return findOrStoreContactInternal(contactCsvRow, file);
            }
        } catch (Throwable e) {
            if (e instanceof FileNotFoundException && retryOnNonExistingFile) {
                try {
                    return createNewContactsFileAndStore(contactCsvRow);
                } catch (Throwable ignore) {
                }
                logger.warn("Could not create a new file");
            }
            logger.error("Error during contact record lookup", e);
            return new ContactStorageResult(StorageResultType.INTERNAL_ERROR, "Error during contact record lookup");
        } finally {
            closeQuietly(lock);
            closeQuietly(channel);
            closeQuietly(file);
        }
    }

    /** attempts to close a resource;
     *  - if the resource value is null, nothing happens
     *  - if the an error occurs, it is ignore (TODO: monitoring of closing errors ) */
    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (Throwable ignored) {
        }
    }


    /**
     * Creates a new contacts CSV file amd proceeds to store new record to it
     *
     * @param contactCsvRow a full contact record in CSV-line format
     */
    private ContactStorageResult createNewContactsFileAndStore(String contactCsvRow) throws IOException {
        logger.warn("Contacts file not found. Creating new one...");
        java.io.File targetFile = new java.io.File(config.getContactsFilePath());
        java.io.File parent = targetFile.getParentFile();
        if (!(parent != null && !parent.exists() && !parent.mkdirs())) {
            logger.warn("New contacts file created");
            return findOrStoreContact(contactCsvRow, false);
        }
        throw new IOException("Could not create contacts file");
    }


    /**
     * Scans the contacts file. If not found, record will be stored
     *
     * @param contactCsvRow a full contact record in CSV-line format
     * @param file          an object representing the contacts file within the file system
     */
    private ContactStorageResult findOrStoreContactInternal(String contactCsvRow, RandomAccessFile file) throws IOException {
        var lineIndex = 0;

        String nextLine;
        while ((nextLine = file.readLine()) != null) {
            if (compareContactLine(nextLine, contactCsvRow)) {
                logger.info("Contact information record found at " + lineIndex);
                return new ContactStorageResult(StorageResultType.ALREADY_EXISTS, "Contact information record already exsits");
            }
            ++lineIndex;
        }
        return storeNewContact(contactCsvRow, file);
    }


    /**
     * Evaluates contact line match. Con be configured for case-insensitive match
     */
    private boolean compareContactLine(String exstingRecordLine, String lookupCsvLine) {
        if (config.isCaseInsensitiveContactMatch())
            return exstingRecordLine.equalsIgnoreCase(lookupCsvLine);
        return exstingRecordLine.equals(lookupCsvLine);
    }


    /**
     * Stores new record to the contacts file
     */
    private ContactStorageResult storeNewContact(String expectedCsvRow, RandomAccessFile file) {
        try {
            var newLineExists = true;
            if (file.length() > 0) {
                file.seek(file.length() - 1);
                byte readByte = file.readByte();
                newLineExists = readByte == 0xA;
            } else
                file.seek(0);

            file.writeBytes(newLineExists ? expectedCsvRow + NEW_LINE : NEW_LINE + expectedCsvRow + NEW_LINE);
            logger.info("New contact information accepted");
            return new ContactStorageResult(StorageResultType.CREATED, "New contact information accepted");
        } catch (IOException e) {
            logger.error("Error during contact record insertion", e);
            return new ContactStorageResult(StorageResultType.INTERNAL_ERROR, "Error during contact record insertion");
        }
    }
}
