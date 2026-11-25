package com.example;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CalendarController {
    private static final DateTimeFormatter DATABASE_DATE = DateTimeFormatter.ofPattern("MM-dd-uuuu", Locale.ROOT);

    private final UserTable userTable;
    private final TaskInfoTable taskInfoTable;
    private final TaskLogTable taskLogTable;
    private final NoteTable noteTable;

    public CalendarController(UserTable userTable, DatabaseSetup databaseSetup) {
        this.userTable = userTable;
        this.taskInfoTable = new TaskInfoTable(databaseSetup);
        this.taskLogTable = new TaskLogTable(databaseSetup);
        this.noteTable = new NoteTable(databaseSetup);
    }

    @SuppressWarnings("null")
    @GetMapping("/api/calendar/month")
    public CalendarMonth getMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpSession session) {
        LocalDate today = LocalDate.now();
        int requestedYear = year == null ? today.getYear() : year;
        int requestedMonth = month == null ? today.getMonthValue() : month;
        int userId = getUserId(session);

        final YearMonth selectedMonth;
        try {
            selectedMonth = YearMonth.of(requestedYear, requestedMonth);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid calendar month.", exception);
        }

        try {
            List<Integer> dueDays = new ArrayList<>();
            for (TaskModel task : taskInfoTable.getTasksByUserId(userId)) {
                LocalDate endDate = parseStoredDate(task.getEndDate());
                if (YearMonth.from(endDate).equals(selectedMonth) && !dueDays.contains(endDate.getDayOfMonth())) {
                    dueDays.add(endDate.getDayOfMonth());
                }
            }
            dueDays.sort(Integer::compare);

            return new CalendarMonth(
                    selectedMonth.getYear(),
                    selectedMonth.getMonthValue(),
                    selectedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    selectedMonth.lengthOfMonth(),
                    selectedMonth.atDay(1).getDayOfWeek().getValue() % 7,
                    new CalendarDate(today.getYear(), today.getMonthValue(), today.getDayOfMonth()),
                    dueDays);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    @GetMapping("/api/calendar/day")
    public CalendarDay getDay(@RequestParam String date, HttpSession session) {
        LocalDate selectedDate = parseDate(date);
        int userId = getUserId(session);

        try {
            Map<Integer, Boolean> completionByTaskId = new HashMap<>();
            for (TaskLogModel log : taskLogTable.getTaskLogsByDateAndUserId(userId, databaseDate(selectedDate))) {
                completionByTaskId.put(log.getCorrespondingTaskId(), log.getTaskCompletionStatus());
            }

            List<CalendarTask> tasks = new ArrayList<>();
            for (TaskModel task : taskInfoTable.getTasksByUserId(userId)) {
                LocalDate startDate = parseStoredDate(task.getStartDate());
                LocalDate endDate = parseStoredDate(task.getEndDate());
                if (!selectedDate.isBefore(startDate) && !selectedDate.isAfter(endDate)) {
                    int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());
                    tasks.add(new CalendarTask(
                            taskId,
                            task.getTaskName(),
                            task.getTaskGoalAmount(),
                            task.getTaskUnit(),
                            task.getTimeOfDay(),
                            completionByTaskId.getOrDefault(taskId, false),
                            selectedDate.equals(endDate)));
                }
            }

            NoteModel note = noteTable.getSingleNote(userId, databaseDate(selectedDate));
            return new CalendarDay(date, tasks, note == null ? "" : note.getNoteText());
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    @PostMapping("/api/calendar/tasks")
    public CalendarTask createTask(@RequestBody CreateTaskRequest request, HttpSession session) {
        int userId = getUserId(session);
        TaskDetails details = parseTaskDetails(request);

        try {
            if (taskInfoTable.getSingleTask(userId, details.taskName()) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A task with this name already exists.");
            }

            TaskModel task = new TaskModel(
                    userId,
                    details.taskName(),
                    details.goal(),
                    details.unit(),
                    details.timeOfDay(),
                    databaseDate(details.startDate()),
                    databaseDate(details.endDate()));
            taskInfoTable.addTask(task);
            int taskId = taskInfoTable.getTaskId(userId, details.taskName());
            return new CalendarTask(
                    taskId,
                    details.taskName(),
                    details.goal(),
                    details.unit(),
                    details.timeOfDay(),
                    false,
                    details.startDate().equals(details.endDate()));
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    @PutMapping("/api/calendar/tasks/{taskId}")
    public void updateTask(
            @PathVariable int taskId,
            @RequestBody CreateTaskRequest request,
            HttpSession session) {
        int userId = getUserId(session);
        TaskDetails details = parseTaskDetails(request);

        try {
            TaskModel existingTask = findTaskById(userId, taskId);
            if (existingTask == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
            }
            TaskModel taskWithSameName = taskInfoTable.getSingleTask(userId, details.taskName());
            if (taskWithSameName != null && !existingTask.getTaskName().equals(details.taskName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A task with this name already exists.");
            }

            TaskModel updatedTask = new TaskModel(
                    userId,
                    details.taskName(),
                    details.goal(),
                    details.unit(),
                    details.timeOfDay(),
                    databaseDate(details.startDate()),
                    databaseDate(details.endDate()));
            if (taskInfoTable.updateTaskDetails(userId, taskId, updatedTask) == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    @DeleteMapping("/api/calendar/tasks/{taskId}")
    public void deleteTask(@PathVariable int taskId, HttpSession session) {
        int userId = getUserId(session);
        try {
            TaskModel task = findTaskById(userId, taskId);
            if (task == null || taskInfoTable.deleteTask(userId, task.getTaskName()) == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    @PutMapping("/api/calendar/tasks/{taskId}/completion")
    public void updateCompletion(
            @PathVariable int taskId,
            @RequestParam String date,
            @RequestBody CompletionRequest request,
            HttpSession session) {
        if (request == null || request.completed() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion status is required.");
        }
        LocalDate selectedDate = parseDate(date);
        int userId = getUserId(session);

        try {
            TaskModel matchingTask = findTaskById(userId, taskId);
            if (matchingTask == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
            }

            LocalDate startDate = parseStoredDate(matchingTask.getStartDate());
            LocalDate endDate = parseStoredDate(matchingTask.getEndDate());
            if (selectedDate.isBefore(startDate) || selectedDate.isAfter(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is not scheduled for this date.");
            }

            taskLogTable.addLog(new TaskLogModel(userId, taskId, databaseDate(selectedDate), request.completed()));
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    @PutMapping("/api/calendar/notes")
    public void saveNote(@RequestBody SaveNoteRequest request, HttpSession session) {
        if (request == null || request.date() == null || request.text() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note date and text are required.");
        }
        LocalDate date = parseDate(request.date());
        int userId = getUserId(session);

        try {
            noteTable.addNote(new NoteModel(userId, request.text(), databaseDate(date)));
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    private int getUserId(HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to use the calendar.");
        }

        try {
            int userId = userTable.getUserId(username);
            if (userId < 0) {
                session.invalidate();
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to use the calendar.");
            }
            return userId;
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    private TaskModel findTaskById(int userId, int taskId) throws SQLException {
        for (TaskModel task : taskInfoTable.getTasksByUserId(userId)) {
            if (taskInfoTable.getTaskId(userId, task.getTaskName()) == taskId) {
                return task;
            }
        }
        return null;
    }

    private static TaskDetails parseTaskDetails(CreateTaskRequest request) {
        if (request == null || request.taskName() == null || request.taskName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task name is required.");
        }

        LocalDate startDate;
        LocalDate endDate;
        if (request.date() != null && request.startDate() == null && request.endDate() == null) {
            startDate = parseDate(request.date());
            endDate = startDate;
        } else {
            if (request.startDate() == null || request.endDate() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task start and end dates are required.");
            }
            startDate = parseDate(request.startDate());
            endDate = parseDate(request.endDate());
        }
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task end date must be on or after its start date.");
        }

        int goal = 1;
        String unit = "time";
        if (request.duration() != null && !request.duration().isBlank()) {
            String[] durationParts = request.duration().trim().split("\\s+");
            if (durationParts.length != 2 || !List.of("min", "hr").contains(durationParts[1])) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be a number followed by min or hr.");
            }
            try {
                goal = Integer.parseInt(durationParts[0]);
            } catch (NumberFormatException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be a positive number.", exception);
            }
            if (goal <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be a positive number.");
            }
            unit = durationParts[1];
        }

        String timeOfDay = request.timeOfDay() == null || request.timeOfDay().isBlank()
                ? "all day"
                : request.timeOfDay().trim().toLowerCase(Locale.ROOT);
        if (!List.of("all day", "morning", "evening").contains(timeOfDay)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time of day must be All day, Morning, or Evening.");
        }

        return new TaskDetails(request.taskName().trim(), goal, unit, timeOfDay, startDate, endDate);
    }

    private static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date must use YYYY-MM-DD format.", exception);
        }
    }

    private static LocalDate parseStoredDate(String date) {
        try {
            return LocalDate.parse(date, DATABASE_DATE);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "A stored task has an invalid date.", exception);
        }
    }

    private static String databaseDate(LocalDate date) {
        return DATABASE_DATE.format(date);
    }

    private static ResponseStatusException databaseError(SQLException exception) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to access calendar data.", exception);
    }

    public record CalendarMonth(
            int year,
            int month,
            String monthName,
            int daysInMonth,
            int firstDayOffset,
            CalendarDate today,
            List<Integer> dueDays) {
    }

    public record CalendarDate(int year, int month, int day) {
    }

    public record CalendarDay(String date, List<CalendarTask> tasks, String note) {
    }

    public record CalendarTask(
            int taskId,
            String taskName,
            int goal,
            String unit,
            String timeOfDay,
            boolean completed,
            boolean finalDay) {
    }

    public record CreateTaskRequest(
            String taskName,
            String date,
            String startDate,
            String endDate,
            String duration,
            String timeOfDay) {
        public CreateTaskRequest(String taskName, String date) {
            this(taskName, date, null, null, null, null);
        }
    }

    public record CompletionRequest(Boolean completed) {
    }

    public record SaveNoteRequest(String date, String text) {
    }

    private record TaskDetails(
            String taskName,
            int goal,
            String unit,
            String timeOfDay,
            LocalDate startDate,
            LocalDate endDate) {
    }
}
