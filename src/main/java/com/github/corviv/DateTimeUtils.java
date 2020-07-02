package com.github.corviv;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners(LoggerListener.class)
public class DateTimeUtils {

    private static final Logger logger = LoggerFactory.getLogger("DateTimeUtils");

    public static String getFormatTimeNow() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String timeMinus(String time, int minutes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(time, formatter).minusMinutes(minutes).format(formatter);
    }

    public static String timePlus(String time, int minutes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(time, formatter).plusMinutes(minutes).format(formatter);
    }

    public static boolean timeInPeriod(String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        LocalTime timeNow = LocalTime.now();
        boolean inclusive = timeNow.isBefore(start) && timeNow.isAfter(end);
        if (inclusive) {

        }
        return inclusive;
    }

    public static String[] calculationTimeInterval(int startHours, int lengthHours) {
        LocalTime startTime = LocalTime.now().plusHours(startHours);
        LocalTime endTime = startTime.plusHours(lengthHours);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return new String[]{startTime.format(formatter), endTime.format(formatter)};
    }

    public static String[] calculationTimeInterval(int startHours, int lengthHours,
        int startMinutes, int lengthMinutes) {
        LocalTime startTime = LocalTime.now().plusHours(startHours).plusMinutes(startMinutes);
        LocalTime endTime = startTime.plusHours(lengthHours).plusMinutes(lengthMinutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return new String[]{startTime.format(formatter), endTime.format(formatter)};
    }

    public static int calculationDayOfWeekValue(int offset) {
        LocalDate date = LocalDate.now();
        LocalDate newDate = date.plusDays(offset);
        return newDate.getDayOfWeek().getValue();
    }

    public static boolean todayIsWeekend() {
        int date = LocalDate.now().getDayOfWeek().getValue();
        if (date == 6 || date == 7) {
            return true;
        } else {
            return false;
        }
    }

    public static String dayWeek(int number) {
        return dayWeek(number, true);
    }

    public static String dayWeek(int number, boolean shortName) {
        switch (number) {
            case 1:
                return shortName ? "Пн" : "Понедельник";
            case 2:
                return shortName ? "Вт" : "Вторник";
            case 3:
                return shortName ? "Ср" : "Среда";
            case 4:
                return shortName ? "Чт" : "Четверг";
            case 5:
                return shortName ? "Пт" : "Пятница";
            case 6:
                return shortName ? "Сб" : "Суббота";
            case 7:
                return shortName ? "Вс" : "Воскресенье";
            default:
                return "";
        }
    }

    public static String formatDateInUi() {
        LocalDate date = LocalDate.now();
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy" + " г.", new Locale("ru")));
    }

    public static String formatDateInCmd() {
        LocalDate date = LocalDate.now();
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public static boolean compareDates(String actualDate, LocalDateTime currentDate) {
        String newActualDate = null;
        if (actualDate.length() == 23) {
            newActualDate = actualDate.substring(0, actualDate.length() - 1);
        } else if (actualDate.length() == 21) {
            newActualDate = actualDate + "0";
        } else {
            newActualDate = actualDate;
        }
        LocalDateTime formattingActualDate = LocalDateTime
            .parse(newActualDate, DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss.SS"));
        logger.debug(String.valueOf(formattingActualDate));
        logger.debug(String.valueOf(currentDate));
        return currentDate.isBefore(formattingActualDate);
    }
}
