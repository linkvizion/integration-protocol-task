
package org.snebesnyi;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@ApplicationScoped
@Slf4j
public class InMemoryKafka {
    private final Map<String, CopyOnWriteArrayList<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    public <T> void publish(String topic, T message) {
        var list = subscribers.get(topic);
        if (list != null) list.forEach(c -> {
            try {
                c.accept(message);
            } catch (Exception e) {
                log.error("", e);
            }
        });
    }

    public <T> void subscribe(String topic, Consumer<T> handler) {
        subscribers.computeIfAbsent(topic, t -> new CopyOnWriteArrayList<>()).add((Consumer<Object>) handler);
    }
}
