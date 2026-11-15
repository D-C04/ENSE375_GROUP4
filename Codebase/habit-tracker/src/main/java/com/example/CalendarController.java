package com.example;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CalendarController {

    @GetMapping("/api/calendar/month")
    public CalendarMonth getMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate today = LocalDate.now();
        int requestedYear = year == null ? today.getYear() : year;
        int requestedMonth = month == null ? today.getMonthValue() : month;

        final YearMonth selectedMonth;
        try {
            selectedMonth = YearMonth.of(requestedYear, requestedMonth);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid calendar month.", exception);
        }

        return new CalendarMonth(
                selectedMonth.getYear(),
                selectedMonth.getMonthValue(),
                selectedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                selectedMonth.lengthOfMonth(),
                selectedMonth.atDay(1).getDayOfWeek().getValue() % 7,
                new CalendarDate(today.getYear(), today.getMonthValue(), today.getDayOfMonth()));
    }

    public record CalendarMonth(
            int year,
            int month,
            String monthName,
            int daysInMonth,
            int firstDayOffset,
            CalendarDate today) {
    }

    public record CalendarDate(int year, int month, int day) {
    }
}
