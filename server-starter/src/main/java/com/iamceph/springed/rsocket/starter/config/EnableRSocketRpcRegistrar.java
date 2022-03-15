package com.iamceph.springed.rsocket.starter.config;

import io.rsocket.rpc.annotations.internal.Generated;
import io.rsocket.rpc.annotations.internal.ResourceType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Class for registering {@link io.rsocket.rpc.AbstractRSocketService} services.
 * This is needed in order to actually use the service implementation and register it into the RSocker server.
 * <p>
 * I was searching a lot for this black magic fuckery, here is a credit for
 * an awesome thread that helped me. I hope it will help you too :)
 * <p>
 * LINK: https://stackoverflow.com/questions/61971497/spring-custom-enable-annotation-meta-annotated-with-componentscan
 */
@Slf4j
public class EnableRSocketRpcRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private static final BeanNameGenerator BEAN_NAME_GENERATOR = AnnotationBeanNameGenerator.INSTANCE;
    private Environment environment;

    @Override
    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(@NotNull AnnotationMetadata metadata,
                                        @NotNull BeanDefinitionRegistry registry) {
        val attributes = metadata.getAnnotationAttributes(EnableRSocketRpc.class.getCanonicalName());
        if (attributes == null) {
            log.debug("No attributes for EnableRSocketRpc found, doing nothing.");
            return;
        }

        val annotationAttributes = new AnnotationAttributes(attributes);
        val provider = new ClassPathScanningCandidateComponentProvider(false, environment);

        log.debug("Adding include filter for io.rsocket.rpc.annotations.internal.Generated annotation..");
        provider.addIncludeFilter(new AnnotationTypeFilter(Generated.class, true));

        getBasePackages(metadata, annotationAttributes)
                .stream()
                .flatMap(basePackage -> provider.findCandidateComponents(basePackage).stream())
                .filter(filterDefinitions())
                .forEach(beanDefinition -> {
                    val beanName = beanDefinition.getBeanClassName();
                    log.debug("Found RSocket Server bean: {}", beanName);

                    val beanClassName = BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, registry);
                    if (!registry.containsBeanDefinition(beanClassName)) {
                        log.debug("Registering RSocket server bean: {}", beanDefinition);
                        registry.registerBeanDefinition(beanClassName, beanDefinition);
                    }
                });
    }

    private Predicate<BeanDefinition> filterDefinitions() {
        return beanDefinition -> {
            val beanName = beanDefinition.getBeanClassName();
            val annotatedMetadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();

            if (annotatedMetadata.hasAnnotation("io.rsocket.rpc.annotations.internal.Generated")) {
                val annotation = annotatedMetadata.getAnnotations().get(Generated.class);
                if (!annotation.isPresent()) {
                    log.debug("Bean[{}] does not have [io.rsocket.rpc.annotations.internal.Generated] annotation, skipping.", beanName);
                    return false;
                }

                val resourceType = annotation.getValue("type", ResourceType.class);
                if (resourceType.isPresent()) {
                    val isService = resourceType.get() == ResourceType.SERVICE;
                    log.debug("Bean[{}] is a RSocket service: {}", beanName, isService);

                    return isService;
                }
                log.debug("Bean[{}] does not have [io.rsocket.rpc.annotations.internal.ResourceType] in Generated annotation, skipping.", beanName);
            }

            return false;
        };
    }

    private Set<String> getBasePackages(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        val basePackages = attributes.getStringArray("basePackages");
        val packagesToScan = new LinkedHashSet<>(Arrays.asList(basePackages));

        if (packagesToScan.isEmpty()) {
            val standardMetadata = (StandardAnnotationMetadata) metadata;
            log.debug("No basePackages are defined, using annotated class package.");
            return Collections.singleton(standardMetadata.getIntrospectedClass().getPackage().getName());
        }

        log.debug("Packages to scan: {}", packagesToScan);
        return packagesToScan;
    }
}

