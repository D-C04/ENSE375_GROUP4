package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;

class StatisticsControllerTests {
    private static final DateTimeFormatter DATABASE_DATE =
            DateTimeFormatter.ofPattern("MM-dd-uuuu", Locale.ROOT);

    @TempDir
    Path tempDir;

    private DatabaseSetup databaseSetup;
    private StatisticsController controller;
    private int userId;
    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        databaseSetup = new DatabaseSetup(tempDir, "statistics.db");
        databaseSetup.createTables();
        UserTable userTable = new UserTable(databaseSetup);
        userTable.addUser(new UserModel("statistics-user", "password-hash", "statistics@example.com"));
        userId = userTable.getUserId("statistics-user");
        controller = new StatisticsController(userTable, databaseSetup);
        session = new MockHttpSession();
        session.setAttribute("user", "statistics-user");
    }

    @Test
    void calculatesDailyAverageStreakAndCurrentTaskFrequencies() throws Exception {
        LocalDate today = LocalDate.now();
        TaskInfoTable taskInfoTable = new TaskInfoTable(databaseSetup);
        TaskLogTable taskLogTable = new TaskLogTable(databaseSetup);

        taskInfoTable.addTask(new TaskModel(
                userId, "Read", 1, "time", "morning",
                databaseDate(today.minusDays(2)), databaseDate(today)));
        taskInfoTable.addTask(new TaskModel(
                userId, "Walk", 1, "time", "all day",
                databaseDate(today.minusDays(1)), databaseDate(today)));
        int readId = taskInfoTable.getTaskId(userId, "Read");
        int walkId = taskInfoTable.getTaskId(userId, "Walk");

        taskLogTable.addLog(new TaskLogModel(userId, readId, databaseDate(today.minusDays(2)), false));
        taskLogTable.addLog(new TaskLogModel(userId, readId, databaseDate(today.minusDays(1)), true));
        taskLogTable.addLog(new TaskLogModel(userId, readId, databaseDate(today), true));
        taskLogTable.addLog(new TaskLogModel(userId, walkId, databaseDate(today.minusDays(1)), true));
        taskLogTable.addLog(new TaskLogModel(userId, walkId, databaseDate(today), true));

        StatisticsController.Statistics statistics = controller.getStatistics(session);

        assertEquals(2, statistics.completedAllTasksStreak());
        assertEquals(67, statistics.averageDailyCompletion());
        assertEquals(2, statistics.activeTasks().size());
        assertEquals("all day", statistics.activeTasks().get(0).timeOfDay());
        assertEquals(100, statistics.activeTasks().get(0).completionFrequency());
        assertEquals("morning", statistics.activeTasks().get(1).timeOfDay());
        assertEquals(67, statistics.activeTasks().get(1).completionFrequency());
    }

    @Test
    void statisticsRequireAnAuthenticatedSession() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getStatistics(new MockHttpSession()));

        assertEquals(401, exception.getStatusCode().value());
    }

    private static String databaseDate(LocalDate date) {
        return DATABASE_DATE.format(date);
    }
}
