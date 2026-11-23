package com.example;


/**
 * TaskModel - Holds a task's information so it can be passed to and from the database classes.
 * Holds: correspondingUserId, taskName, taskGoalAmount, taskUnit, timeOfDay, startDate, endDate.
 * Immutable: values are set in the constructor and can only be read with getters.
 */
public class TaskModel {

    private int correspondingUserId;
    private String taskName;
    private int taskGoalAmount;
    private String taskUnit;
    private String timeOfDay;
    private String startDate;
    private String endDate;

    public TaskModel(int correspondingUserId, String taskName, int taskGoalAmount, String taskUnit, String timeOfDay, String startDate, String endDate){
        this.correspondingUserId = correspondingUserId;
        this.taskName = taskName; 
        this.taskGoalAmount = taskGoalAmount;
        this.taskUnit = taskUnit;
        this.timeOfDay = timeOfDay; // Options are: morning, all day, evening
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
