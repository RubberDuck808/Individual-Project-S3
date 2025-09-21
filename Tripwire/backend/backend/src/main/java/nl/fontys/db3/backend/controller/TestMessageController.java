package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.model.TestMessage;
import nl.fontys.db3.backend.repository.TestMessageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class TestMessageController {

    private final TestMessageRepository repository;

    public TestMessageController(TestMessageRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TestMessage> getAll() {
        return repository.findAll();
    }
}
