package ru.pushkin.mmb.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class TimeProvider {

    public LocalDate nowDate() {
        return LocalDate.now();
    }

    public LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }

}
