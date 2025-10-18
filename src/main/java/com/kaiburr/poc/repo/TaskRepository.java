// repo/TaskRepository.java
package com.kaiburr.poc.repo;


import com.kaiburr.poc.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByNameContainingIgnoreCase(String name);
}

