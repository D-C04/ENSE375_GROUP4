package com.example;


/**
 * TaskLogModel - Holds a task log's information so it can be passed to and from the database classes.
 * Holds: correspondingUserId, correspondingTaskId, taskLogDate, isTaskComplete.
 * Immutable: values are set in the constructor and can only be read with getters.
 */
public class TaskLogModel {

    private final int correspondingUserId;
    private final int correspondingTaskId;
    private final String taskLogDate;
    private final boolean isTaskComplete;

    public TaskLogModel(int correspondingUserId, int correspondingTaskId, String taskLogDate, boolean isTaskComplete){
        this.correspondingUserId = correspondingUserId;
        this.correspondingTaskId = correspondingTaskId;
        this.taskLogDate = taskLogDate;
        this.isTaskComplete = isTaskComplete;
    }

    public int getCorrespondingUserId(){
        return correspondingUserId;
    }

    public int getCorrespondingTaskId(){
        return correspondingTaskId;
    }

    public String getTaskLogDate(){
        return taskLogDate;
    }

    public boolean getTaskCompletionStatus(){
        return isTaskComplete;
    }

}
