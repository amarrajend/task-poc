// web/TaskController.java

package com.kaiburr.poc.web;

import com.kaiburr.poc.model.Task;
import com.kaiburr.poc.model.TaskExecution;
import com.kaiburr.poc.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping("/Test")
    public String get() {
        System.out.println("Health check endpoint called.");
        return "Task Service is up and running!";
    }

    // GET tasks (all) OR by id with query param (?id=)
    @GetMapping
    public Object get(@RequestParam(value = "id", required = false) String id) {
        return (id == null ? service.all() : service.byId(id));
    }

    // PUT a task (upsert). Validates 'command' for safety.
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Task put(@Valid @RequestBody Task task) {
        return service.save(task);
    }

    // DELETE a task by id
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    // GET find tasks by name (contains). Return 404 if nothing found.
    @GetMapping("/search")
    public List<Task> search(@RequestParam("name") String name) {
        List<Task> found = service.searchByName(name);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("No tasks found containing name: " + name);
        }
        return found;
    }

    // PUT a TaskExecution (by task ID). Executes the shell command and persists execution.
    @PutMapping("/{id}/execute")
    @ResponseStatus(HttpStatus.OK)
    public TaskExecution execute(@PathVariable String id) throws Exception {
        return service.run(id);
    }

    @PostMapping("/{id}/executions")
    public ResponseEntity<TaskExecution> addTaskExecution(
            @PathVariable String id,
            @RequestBody CommandRequest commandRequest) {
        System.out.println("Received request to add TaskExecution for Task ID: " + id + " with command: " + commandRequest.getCommand());
        TaskExecution execution = service.addTaskExecution(id, commandRequest.getCommand());
        return ResponseEntity.ok(execution);
    }

    public static class CommandRequest {
        private String command;

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }
}
