package com.phenikaa.communicationservice.factory;

import com.phenikaa.communicationservice.service.composer.NotificationComposer;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;

public interface NotificationToolkitFactory {
    String supportsType();
    NotificationComposer createComposer();
    NotificationDecorator createDecorator();
}


