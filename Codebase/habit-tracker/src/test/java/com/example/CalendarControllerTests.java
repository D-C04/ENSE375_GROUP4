package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;

class CalendarControllerTests {
    @TempDir
    Path tempDir;

    private CalendarController controller;
    private HttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseSetup databaseSetup = new DatabaseSetup(tempDir, "calendar.db");
        databaseSetup.createTables();
        UserTable userTable = new UserTable(databaseSetup);
        userTable.addUser(new UserModel("calendar-user", "password-hash", "calendar@example.com"));
        controller = new CalendarController(userTable, databaseSetup);

        session = new MockHttpSession();
        session.setAttribute("user", "calendar-user");
    }

    @Test
    void calendarTasksCompletionAndNotesAreSavedForSelectedDay() {
        CalendarController.CalendarTask task = controller.createTask(
                new CalendarController.CreateTaskRequest("Read", "2026-02-03"), session);

        assertTrue(task.finalDay());
        assertFalse(task.completed());
        controller.updateCompletion(
                task.taskId(), "2026-02-03",
                new CalendarController.CompletionRequest(true), session);
        controller.saveNote(
                new CalendarController.SaveNoteRequest("2026-02-03", "A productive day."),
                session);

        CalendarController.CalendarDay day = controller.getDay("2026-02-03", session);
        assertEquals("2026-02-03", day.date());
        assertEquals("A productive day.", day.note());
        assertEquals(1, day.tasks().size());
        assertTrue(day.tasks().get(0).completed());
        assertTrue(day.tasks().get(0).finalDay());
    }

    @Test
    void dayTasksAreOnlyReturnedForTheirScheduledDate() {
        controller.createTask(new CalendarController.CreateTaskRequest("Walk", "2026-02-03"), session);

        assertTrue(controller.getDay("2026-02-04", session).tasks().isEmpty());
    }

    @Test
    void monthReportsTaskDueDaysForCalendarMarkers() {
        controller.createTask(new CalendarController.CreateTaskRequest(
                "Read", null, "2026-02-01", "2026-02-03", "30 min", "Morning"), session);
        controller.createTask(new CalendarController.CreateTaskRequest(
                "Walk", null, "2026-02-01", "2026-02-03", "20 min", "Evening"), session);
        controller.createTask(new CalendarController.CreateTaskRequest(
                "Stretch", null, "2026-02-01", "2026-02-07", "10 min", "All day"), session);

        assertEquals(List.of(3, 7), controller.getMonth(2026, 2, session).dueDays());
        assertTrue(controller.getMonth(2026, 3, session).dueDays().isEmpty());
    }

    @Test
    void dashboardHabitFieldsAreStoredAndShownAcrossItsDateRange() {
        CalendarController.CalendarTask task = controller.createTask(
                new CalendarController.CreateTaskRequest(
                        "Read", null, "2026-02-01", "2026-02-03", "30 min", "Morning"),
                session);

        CalendarController.CalendarDay middleDay = controller.getDay("2026-02-02", session);
        assertEquals(1, middleDay.tasks().size());
        assertEquals(30, middleDay.tasks().get(0).goal());
        assertEquals("min", middleDay.tasks().get(0).unit());
        assertEquals("morning", middleDay.tasks().get(0).timeOfDay());
        assertFalse(middleDay.tasks().get(0).finalDay());

        controller.updateCompletion(
                task.taskId(), "2026-02-02",
                new CalendarController.CompletionRequest(true), session);
        assertTrue(controller.getDay("2026-02-02", session).tasks().get(0).completed());

        controller.updateTask(
                task.taskId(),
                new CalendarController.CreateTaskRequest(
                        "Read updated", null, "2026-02-02", "2026-02-04", "45 min", "Evening"),
                session);
        CalendarController.CalendarTask updated = controller.getDay("2026-02-02", session).tasks().get(0);
        assertEquals("Read updated", updated.taskName());
        assertEquals(45, updated.goal());
        assertEquals("evening", updated.timeOfDay());

        controller.deleteTask(task.taskId(), session);
        assertTrue(controller.getDay("2026-02-02", session).tasks().isEmpty());
    }

    @Test
    void dayDataRequiresAnAuthenticatedSession() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getDay("2026-02-03", new MockHttpSession()));

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void invalidCalendarDateIsRejected() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getDay("2026-02-30", session));

        assertEquals(400, exception.getStatusCode().value());
    }
}
