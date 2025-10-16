package com.phenikaa.communicationservice.repository;

import com.phenikaa.communicationservice.entity.Group;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface GroupRepository extends ReactiveMongoRepository<Group, String> {
    Flux<Group> findByMemberIdsContaining(String userId);
}


