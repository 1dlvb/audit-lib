package com.onedlvb.messagereceiver.repository;

import com.onedlvb.messagereceiver.model.KafkaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface KafkaMessageRepository extends JpaRepository<KafkaMessage, UUID> {
}
