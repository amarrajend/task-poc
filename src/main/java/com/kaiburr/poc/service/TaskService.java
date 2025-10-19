// service/TaskService.java
package com.kaiburr.poc.service;


import com.kaiburr.poc.model.Task;
import com.kaiburr.poc.model.TaskExecution;
import com.kaiburr.poc.repo.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class TaskService {

    private final TaskRepository repo;
    private final CommandValidator validator;

    @Autowired
    private KubernetesService kubernetesService;

    @Value("${KUBERNETES_NAMESPACE:default}")
    private String namespace;

    public TaskExecution addTaskExecution(String taskId, String command) {
        System.out.println("Adding TaskExecution for Task ID: " + taskId + " with command: " + command);
        Optional<Task> optionalTask = repo.findById(taskId);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();

            // Create Kubernetes pod to execute command
            String executionResult = kubernetesService.createCommandPod(command, namespace);
            Date start = Date.from(Instant.now());
            Date end = Date.from(Instant.now());


            TaskExecution execution = new TaskExecution();
            execution.setStartTime(start);
            execution.setEndTime(end); // Simulate end time
            execution.setOutput(executionResult);

            task.getTaskExecutions().add(execution);
            repo.save(task);

            return execution;
        }
        throw new RuntimeException("Task not found with id: " + taskId);
    }

    public TaskService(TaskRepository repo, CommandValidator validator) {
        this.repo = repo;
        this.validator = validator;
    }

    // PUT (upsert)
    public Task save(Task t) {
        validator.validateOrThrow(t.getCommand());
        return repo.save(t);
    }

    // GET all
    public List<Task> all() { return repo.findAll(); }

    // GET by id
    public Task byId(String id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    // DELETE
    public void delete(String id) { repo.deleteById(id); }

    // FIND by name contains (404 if none handled in controller)
    public List<Task> searchByName(String q) { return repo.findByNameContainingIgnoreCase(q); }

    // Execute the stored command for a task and append a TaskExecution
    public TaskExecution run(String taskId) throws Exception {
        Task task = byId(taskId);

        // validate again in case command changed outside endpoint
        validator.validateOrThrow(task.getCommand());
        List<String> cmd = validator.tokenize(task.getCommand());

        Date start = Date.from(Instant.now());
        String output = runProcess(cmd);
        Date end = Date.from(Instant.now());

        TaskExecution exec = new TaskExecution();
        exec.setStartTime(start);
        exec.setEndTime(end);
        exec.setOutput(output);

        task.getTaskExecutions().add(exec);
        repo.save(task);
        return exec;
    }

    private String runProcess(List<String> cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // merge stderr into stdout
        Process p = pb.start();

        // Time limit + output limit for safety
        boolean finished = p.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new RuntimeException("Command timed out");
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int max = 10_000; // cap output length
            while ((line = br.readLine()) != null) {
                if (sb.length() + line.length() + 1 > max) { break; }
                sb.append(line).append('\n');
            }
        }
        return sb.toString().trim();
    }
}
