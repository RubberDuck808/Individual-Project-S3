package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByUser_UsernameOrderByIdDesc(String username);
}
