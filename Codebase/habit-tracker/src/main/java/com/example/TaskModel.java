package com.example;


/**
 * TaskModel - Holds a task's information so it can be passed to and from the database classes.
 * Holds: correspondingUserId, taskName, taskGoalAmount, taskUnit, timeOfDay, startDate, endDate.
 * Immutable: values are set in the constructor and can only be read with getters.
 * 
 * Expected values for timeOfDay are: morning, all day, evening.
 */
public class TaskModel {

    private final int correspondingUserId;
    private final String taskName;
    private final int taskGoalAmount;
    private final String taskUnit;
    private final String timeOfDay;
    private final String startDate;
    private final String endDate;

    public TaskModel(int correspondingUserId, String taskName, int taskGoalAmount, String taskUnit, String timeOfDay, String startDate, String endDate){
        this.correspondingUserId = correspondingUserId;
        this.taskName = taskName; 
        this.taskGoalAmount = taskGoalAmount;
        this.taskUnit = taskUnit;
        this.timeOfDay = timeOfDay; 
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getCorrespondingUserId(){
        return correspondingUserId;
    }

    public String getTaskName(){
        return taskName;
    }

    public int getTaskGoalAmount(){
        return taskGoalAmount;
    }

    public String getTaskUnit(){
        return taskUnit;
    }

    public String getTimeOfDay(){
        return timeOfDay;
    }

    public String getStartDate(){
        return startDate;
    }

    public String getEndDate(){
        return endDate;
    }

}
