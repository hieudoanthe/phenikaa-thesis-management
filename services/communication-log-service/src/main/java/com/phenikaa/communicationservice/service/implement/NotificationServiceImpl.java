package com.phenikaa.communicationservice.service.implement;

import com.phenikaa.communicationservice.broadcaster.NotificationPublisher;
import com.phenikaa.communicationservice.dto.request.NotificationRequest;
import com.phenikaa.communicationservice.entity.Notification;
import com.phenikaa.communicationservice.repository.NotificationRepository;
import com.phenikaa.communicationservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationBroadcaster;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public void sendNotification(NotificationRequest request) {
        createNotification(
            request.getSenderId(),
            request.getReceiverId(),
            request.getMessage()
        ).subscribe();
    }

    @Override
    public Mono<Notification> createNotification(Integer senderId, Integer receiverId, String message) {
        Notification noti = Notification.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .message(message)
                .read(false)
                .createdAt(Instant.now())
                .build();

        return notificationRepository.save(noti)
                .doOnSuccess(saved -> notificationBroadcaster.publish(receiverId, saved));
    }

    @Override
    public Mono<Long> markAllAsReadAndPublish(int receiverId) {
        Query query = new Query(
                Criteria.where("receiverId").is(receiverId)
                        .and("read").is(false)
        );

        return mongoTemplate.find(query, Notification.class)
                .collectList()
                .flatMap(notifications -> {
                    if (notifications.isEmpty()) {
                        return Mono.just(0L);
                    }

                    Update update = new Update().set("read", true);
                    return mongoTemplate.updateMulti(query, update, Notification.class)
                            .map(result -> {
                                notifications.forEach(noti -> {
                                    noti.setRead(true); // update lại local
                                    notificationBroadcaster.publish(receiverId, noti);
                                });
                                return (long) result.getModifiedCount();
                            });
                });
    }


    @Override
    public Mono<Notification> toggleReadAndPublish(int receiverId, String notificationId) {
        Query query = new Query(Criteria.where("receiverId").is(receiverId)
                .and("_id").is(notificationId));

        return mongoTemplate.findOne(query, Notification.class)
                .flatMap(existing -> {
                    boolean newValue = !existing.isRead();
                    Update update = new Update().set("read", newValue);

                    return mongoTemplate.findAndModify(query, update, Notification.class)
                            .doOnSuccess(updatedNoti -> {
                                if (updatedNoti != null) {
                                    notificationBroadcaster.publish(receiverId, updatedNoti);
                                }
                            });
                });
    }

}
