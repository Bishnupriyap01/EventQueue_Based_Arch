
package com.bishnupriya.event_driven.Repository;
import com.bishnupriya.event_driven.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

}
