package com.example.vdkfamilyserver.Repositories.Event;

import com.example.vdkfamilyserver.Models.Event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}