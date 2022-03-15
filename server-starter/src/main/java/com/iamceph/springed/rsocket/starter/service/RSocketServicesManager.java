package com.iamceph.springed.rsocket.starter.service;

import com.iamceph.springed.rsocket.starter.condition.RSocketEnabledCondition;
import com.iamceph.springed.rsocket.starter.config.RSocketStarterConfig;
import com.iamceph.springed.rsocket.starter.util.ReflectUtil;
import io.rsocket.rpc.AbstractRSocketService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SingletonSupplier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@Conditional(RSocketEnabledCondition.class)
public class RSocketServicesManager implements ApplicationContextAware, InitializingBean {
    private ApplicationContext context;

    private Supplier<List<ServiceWrapper>> availableServices;

    private Supplier<List<AbstractRSocketService>> availableRsocketServices;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public void afterPropertiesSet() {
        final var config = context.getBean(RSocketStarterConfig.class);

        this.availableServices = SingletonSupplier.of(context.getBeansWithAnnotation(RSocketService.class)
                .values()
                .stream()
                .map(service -> buildWrapper(service, config))
                .peek(next -> log.info("Registered RSocket service[{}].", next.getName()))
                .collect(Collectors.toList()));
    }

    public List<ServiceWrapper> getServices() {
        return availableServices.get();
    }

    private ServiceWrapper buildWrapper(Object service, RSocketStarterConfig config) {
        try {
            final var klass = service.getClass();
            final var serviceField = klass.getField("SERVICE");

            final var serviceAnnotation = service.getClass().getAnnotation(RSocketService.class);
            if (serviceAnnotation.customName().isEmpty()
                    || !config.getModifyServiceNames()) {
                return new ServiceWrapper((String) serviceField.get(null), service);
            }

            serviceField.setAccessible(true);

            final var modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(serviceField, serviceField.getModifiers() & ~Modifier.FINAL);

            serviceField.set(null, serviceAnnotation.customName());

            return new ServiceWrapper((String) serviceField.get(null), service);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceWrapper(service.getClass().getSimpleName(), service);
        }
    }

    public final static class ServiceWrapper {
        @Getter
        private final String name;
        @Getter
        private final Object service;

        private final Supplier<List<String>> routes;
        private final Supplier<List<String>> methods;

        public ServiceWrapper(String name, Object service) {
            this.name = name;
            this.service = service;

            this.routes = SingletonSupplier.of(ReflectUtil.getFields(service, "ROUTE"));
            this.methods = SingletonSupplier.of(ReflectUtil.getFields(service, "METHOD"));
        }

        private List<String> getRoutes() {
            return routes.get();
        }

        private List<String> getMethods() {
            return methods.get();
        }
    }

}
