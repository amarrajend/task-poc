package com.kaiburr.poc.model;

// model/Task.java

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document("tasks")
public class Task {
    @Id
    private String id;

    @NotBlank private String name;
    @NotBlank private String owner;
    @NotBlank private String command;

    @Valid
    private List<TaskExecution> taskExecutions = new ArrayList<>();

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public List<TaskExecution> getTaskExecutions() { return taskExecutions; }
    public void setTaskExecutions(List<TaskExecution> taskExecutions) { this.taskExecutions = taskExecutions; }
}
