package cz.fio.dto;


import org.springframework.http.ResponseEntity;

public record ContactStorageResult(StorageResultType type, String message) {
    public ResponseEntity<ContactStorageResult> asResponseEntity() {
        return new ResponseEntity<>(this, type.responseStatus);
    }

}


