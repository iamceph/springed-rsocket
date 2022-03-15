package com.iamceph.springed.rsocket.starter.util;

import com.iamceph.resulter.core.DataResultable;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class ReflectUtil {

    /**
     * Gets names from the service.
     * This is generally used for getting methods/routes from the generated Service.
     *
     * @param service the RSocket generated service
     * @param name    name of the field
     * @return list of methods/routes
     */
    public List<String> getFields(Object service, String name, Logger log) {
        return Arrays.stream(service.getClass().getFields())
                .filter(next -> next.getName().contains(name))
                .map(next -> {
                    try {
                        return (String) next.get(null);
                    } catch (IllegalAccessException e) {
                        log.warn("Cannot get {} from service {}!", name, service.getClass().getSimpleName(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public <T> DataResultable<T> getField(Object service, String fieldName, Class<T> resultClass) {
        try {
            val field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            val toReturn = field.get(service);
            return DataResultable.failIfNull(resultClass.cast(toReturn));
        } catch (Exception e) {
            return DataResultable.fail(e);
        }
    }
}
