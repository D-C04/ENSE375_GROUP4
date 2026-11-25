package com.example;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StatisticsController {
    private static final DateTimeFormatter DATABASE_DATE = DateTimeFormatter.ofPattern("MM-dd-uuuu", Locale.ROOT);

    private final UserTable userTable;
    private final TaskInfoTable taskInfoTable;
    private final TaskLogTable taskLogTable;

    public StatisticsController(UserTable userTable, DatabaseSetup databaseSetup) {
        this.userTable = userTable;
        this.taskInfoTable = new TaskInfoTable(databaseSetup);
        this.taskLogTable = new TaskLogTable(databaseSetup);
    }

    @GetMapping("/api/statistics")
    public Statistics getStatistics(HttpSession session) {
        int userId = getUserId(session);
        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> scheduledTasksByDate = new HashMap<>();
        Map<LocalDate, Integer> completedTasksByDate = new HashMap<>();
        List<ActiveTask> activeTasks = new ArrayList<>();
        LocalDate earliestStartDate = null;

        try {
            for (TaskModel task : taskInfoTable.getTasksByUserId(userId)) {
                LocalDate startDate = parseStoredDate(task.getStartDate());
                LocalDate endDate = parseStoredDate(task.getEndDate());
                LocalDate lastScheduledDate = endDate.isAfter(today) ? today : endDate;
                if (startDate.isAfter(lastScheduledDate)) {
                    continue;
                }

                if (earliestStartDate == null || startDate.isBefore(earliestStartDate)) {
                    earliestStartDate = startDate;
                }

                int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());
                Set<LocalDate> completedDates = new HashSet<>();
                for (TaskLogModel log : taskLogTable.getAllLogsForTask(userId, taskId)) {
                    if (log.getTaskCompletionStatus()) {
                        LocalDate completionDate = parseStoredDate(log.getTaskLogDate());
                        if (!completionDate.isBefore(startDate) && !completionDate.isAfter(lastScheduledDate)) {
                            completedDates.add(completionDate);
                        }
                    }
                }

                for (LocalDate date = startDate; !date.isAfter(lastScheduledDate); date = date.plusDays(1)) {
                    scheduledTasksByDate.merge(date, Integer.valueOf(1),
                            (scheduledCount, increment) -> Integer.valueOf(
                                    (scheduledCount == null ? 0 : scheduledCount.intValue())
                                            + (increment == null ? 0 : increment.intValue())));
                    if (completedDates.contains(date)) {
                        completedTasksByDate.merge(date, Integer.valueOf(1),
                                (completedCount, increment) -> Integer.valueOf(
                                        (completedCount == null ? 0 : completedCount.intValue())
                                                + (increment == null ? 0 : increment.intValue())));
                    }
                }

                if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
                    long scheduledDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, today) + 1;
                    int frequency = (int) Math.round(completedDates.size() * 100.0 / scheduledDays);
                    activeTasks.add(new ActiveTask(task.getTaskName(), task.getTimeOfDay(), frequency));
                }
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }

        int streak = calculateStreak(today, earliestStartDate, scheduledTasksByDate, completedTasksByDate);
        int averageDailyCompletion = calculateAverageDailyCompletion(scheduledTasksByDate, completedTasksByDate);
        activeTasks.sort((first, second) -> {
            int timeComparison = first.timeOfDay().compareTo(second.timeOfDay());
            return timeComparison != 0
                    ? timeComparison
                    : first.taskName().compareTo(second.taskName());
        });
        return new Statistics(streak, averageDailyCompletion, activeTasks);
    }

    private int getUserId(HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to view statistics.");
        }

        try {
            int userId = userTable.getUserId(username);
            if (userId < 0) {
                session.invalidate();
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to view statistics.");
            }
            return userId;
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    private static int calculateStreak(
            LocalDate today,
            LocalDate earliestStartDate,
            Map<LocalDate, Integer> scheduledTasksByDate,
            Map<LocalDate, Integer> completedTasksByDate) {
        if (earliestStartDate == null) {
            return 0;
        }

        int streak = 0;
        for (LocalDate date = today; !date.isBefore(earliestStartDate); date = date.minusDays(1)) {
            int scheduledTasks = scheduledTasksByDate.getOrDefault(date, 0);
            if (scheduledTasks == 0) {
                continue;
            }
            if (completedTasksByDate.getOrDefault(date, 0) != scheduledTasks) {
                break;
            }
            streak++;
        }
        return streak;
    }

    private static int calculateAverageDailyCompletion(
            Map<LocalDate, Integer> scheduledTasksByDate,
            Map<LocalDate, Integer> completedTasksByDate) {
        if (scheduledTasksByDate.isEmpty()) {
            return 0;
        }

        double dailyCompletionTotal = 0;
        for (Map.Entry<LocalDate, Integer> entry : scheduledTasksByDate.entrySet()) {
            dailyCompletionTotal += completedTasksByDate.getOrDefault(entry.getKey(), 0) * 100.0 / entry.getValue();
        }
        return (int) Math.round(dailyCompletionTotal / scheduledTasksByDate.size());
    }

    private static LocalDate parseStoredDate(String date) {
        try {
            return LocalDate.parse(date, DATABASE_DATE);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "A stored task has an invalid date.", exception);
        }
    }

    private static ResponseStatusException databaseError(SQLException exception) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to access statistics data.", exception);
    }

    public record Statistics(int completedAllTasksStreak, int averageDailyCompletion, List<ActiveTask> activeTasks) {
    }

    public record ActiveTask(String taskName, String timeOfDay, int completionFrequency) {
    }
}
