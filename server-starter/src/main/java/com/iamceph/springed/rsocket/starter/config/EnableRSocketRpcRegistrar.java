package com.iamceph.springed.rsocket.starter.config;

import com.iamceph.springed.rsocket.starter.service.RSocketService;
import lombok.val;
import org.jetbrains.annotations.NotNull;
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

/**
 * If someone will be searching for this black magic fuckery, I searched for this way too long.
 * HERE YOU GO: https://stackoverflow.com/questions/61971497/spring-custom-enable-annotation-meta-annotated-with-componentscan
 */
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
            return;
        }

        val annotationAttributes = new AnnotationAttributes(attributes);
        val provider = new ClassPathScanningCandidateComponentProvider(false, environment);

        provider.addIncludeFilter(new AnnotationTypeFilter(RSocketService.class, true));

        val basePackages = getBasePackages((StandardAnnotationMetadata) metadata, annotationAttributes);
        basePackages.forEach(basePackage ->
                provider.findCandidateComponents(basePackage)
                        .forEach(beanDefinition -> {
                            val beanClassName = BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, registry);
                            if (!registry.containsBeanDefinition(beanClassName)) {
                                registry.registerBeanDefinition(beanClassName, beanDefinition);
                            }
                        }));
    }

    private static Set<String> getBasePackages(StandardAnnotationMetadata metadata, AnnotationAttributes attributes) {
        val basePackages = attributes.getStringArray("basePackages");
        val packagesToScan = new LinkedHashSet<>(Arrays.asList(basePackages));

        if (packagesToScan.isEmpty()) {
            // If value attribute is not set, fallback to the package of the annotated class
            return Collections.singleton(metadata.getIntrospectedClass().getPackage().getName());
        }

        return packagesToScan;
    }
}

