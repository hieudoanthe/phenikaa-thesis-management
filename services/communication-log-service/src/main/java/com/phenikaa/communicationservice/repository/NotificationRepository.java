package com.phenikaa.communicationservice.repository;

import com.phenikaa.communicationservice.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
    Flux<Notification> findByReceiverId(Integer receiverId);
}
