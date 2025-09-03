package com.phenikaa.communicationservice.factory;

import com.phenikaa.communicationservice.service.composer.NotificationComposer;
import com.phenikaa.communicationservice.service.decorator.NotificationDecorator;

public record NotificationToolkit(
        NotificationComposer composer,
        NotificationDecorator decoratorChain
) {}


