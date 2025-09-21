package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.model.TestMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestMessageRepository extends JpaRepository<TestMessage, Long> {}
