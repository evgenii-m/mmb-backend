package ru.pushkin.mmb.api.output;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class Utils {
    public static <T> ResponseEntity<T> handleExceptions(Supplier<ResponseEntity<T>> supplier)  {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Exception: {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
