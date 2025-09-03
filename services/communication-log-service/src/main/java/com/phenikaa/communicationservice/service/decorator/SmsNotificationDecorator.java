//package com.phenikaa.communicationservice.service.decorator;
//
//import com.phenikaa.communicationservice.dto.request.NotificationRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class SmsNotificationDecorator extends BaseNotificationDecorator {
//
//    private final SmsService smsService;
//
//    public SmsNotificationDecorator(NotificationDecorator wrapped, SmsService smsService) {
//        super(wrapped);
//        this.smsService = smsService;
//    }
//
//    @Override
//    public void sendNotification(NotificationRequest request) {
//        // Gửi SMS
//        sendSmsNotification(request);
//
//        // Gọi wrapped service
//        super.sendNotification(request);
//    }
//
//    private void sendSmsNotification(NotificationRequest request) {
//        try {
//            String phoneNumber = getReceiverPhone(request.getReceiverId());
//            if (phoneNumber != null) {
//                smsService.sendSms(phoneNumber, request.getMessage());
//                log.info("SMS notification sent to: {}", phoneNumber);
//            }
//        } catch (Exception e) {
//            log.error("Error sending SMS notification: {}", e.getMessage());
//        }
//    }
//
//    private String getReceiverPhone(Integer receiverId) {
//        // Lấy số điện thoại từ profile service
//        return null; // Mock
//    }
//}