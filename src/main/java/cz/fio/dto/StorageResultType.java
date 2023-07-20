package cz.fio.dto;


import org.springframework.http.HttpStatus;

public enum StorageResultType {
    CREATED(HttpStatus.CREATED),
    ALREADY_EXISTS(HttpStatus.OK),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    public final HttpStatus responseStatus;

    StorageResultType(HttpStatus responseStatus) {
        this.responseStatus = responseStatus;
    }
}


