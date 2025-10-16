package com.phenikaa.communicationservice.repository;

import com.phenikaa.communicationservice.entity.GroupMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface GroupMessageRepository extends ReactiveMongoRepository<GroupMessage, String> {
    Flux<GroupMessage> findByGroupIdOrderByTimestampAsc(String groupId);
}


