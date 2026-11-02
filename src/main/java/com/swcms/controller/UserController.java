
package com.swcms.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.swcms.dto.WorkerResponse;
import com.swcms.model.User;
import com.swcms.repository.UserRepository;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/test")
    public String userTest(Authentication authentication) {
        return "Welcome USER: " + authentication.getName();
    }

    @GetMapping("/admin/workers")
    public List<WorkerResponse> getAllWorkers() {

        return userRepository.findByRole("WORKER")
                .stream()
                .map(worker -> new WorkerResponse(
                        worker.getUserId(),
                        worker.getName(),
                        worker.getEmail()
                ))
                .collect(Collectors.toList());
    }
}
