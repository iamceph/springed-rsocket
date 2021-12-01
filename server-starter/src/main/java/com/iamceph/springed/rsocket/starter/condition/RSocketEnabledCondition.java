package com.iamceph.springed.rsocket.starter.condition;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RSocketEnabledCondition implements Condition {
    @Override
    public boolean matches(@NotNull ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        return context
                .getEnvironment()
                .getProperty("springed.rsocket.enabled", Boolean.class, false);
    }
}
