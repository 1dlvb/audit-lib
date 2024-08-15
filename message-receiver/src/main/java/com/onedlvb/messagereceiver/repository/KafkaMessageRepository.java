package com.onedlvb.messagereceiver.repository;

import com.onedlvb.messagereceiver.model.KafkaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * A repository for KafkaMessages
 * @see KafkaMessage
 * @author Matushkin Anton
 */
@Repository
public interface KafkaMessageRepository extends JpaRepository<KafkaMessage, UUID> {
}
