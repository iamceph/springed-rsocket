package com.iamceph.springed.rsocket.starter.util;

import lombok.experimental.UtilityClass;

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
    public List<String> getFields(Object service, String name) {
        return Arrays.stream(service.getClass().getFields())
                .filter(next -> next.getName().contains(name))
                .map(next -> {
                    try {
                        return (String) next.get(null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
