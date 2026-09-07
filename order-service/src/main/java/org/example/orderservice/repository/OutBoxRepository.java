package org.example.orderservice.repository;

import org.example.orderservice.model.OutBoxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutBoxRepository extends JpaRepository<OutBoxMessage,Long> {
    List<OutBoxMessage> findBySentFalseOrderByIdAsc();
}
