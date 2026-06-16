/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.event;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.Event;
import moscow.rockstar.systems.event.EventListener;

public class EventManager {
    private final ConcurrentHashMap<Type, CopyOnWriteArrayList<EventListener<?>>> listenerMap = new ConcurrentHashMap();
    private final Map<Class<?>, Field[]> declaredFieldsCache = new HashMap();
    private final Comparator<EventListener<?>> priorityOrder = Comparator.<EventListener<?>>comparingInt(EventListener::getPriority).reversed();
    private final BiConsumer<List<EventListener<?>>, Comparator<EventListener<?>>> sortCallback = List::sort;
    private final Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    public void subscribe(Object subscriber) {
        this.modifyEventListenerState(subscriber, (type, listener) -> {
            this.listenerMap.computeIfAbsent((Type)type, k -> new CopyOnWriteArrayList()).add(listener);
            this.sortCallback.accept((List)this.listenerMap.get(type), this.priorityOrder);
        });
    }

    public void unsubscribe(Object subscriber) {
        this.modifyEventListenerState(subscriber, (type, listener) -> {
            CopyOnWriteArrayList<EventListener<?>> listeners = this.listenerMap.get(type);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    this.listenerMap.remove(type);
                }
            }
        });
    }

    public <T extends Event> void triggerEvent(T event) {
        Class<?> eventType = event.getClass();
        List<EventListener<?>> listeners = this.listenerMap.get(eventType);
        if (listeners != null && !Rockstar.INSTANCE.isPanic()) {
            for (EventListener<?> listener : listeners) {
                try {
                    @SuppressWarnings("unchecked")
                    EventListener<T> castedListener = (EventListener<T>)listener;
                    castedListener.onEvent(event);
                }
                catch (Throwable t) {
                    this.errorHandler.accept(t);
                }
            }
        }
    }

    private void modifyEventListenerState(Object o, BiConsumer<Type, EventListener<?>> action) {
        for (Field field : this.getCachedDeclaredFields(o.getClass())) {
            EventListener<?> eventListener;
            if (field.getType() != EventListener.class || (eventListener = this.getEventListener(o, field)) == null) continue;
            Type eventType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            action.accept(eventType, eventListener);
        }
    }

    private Field[] getCachedDeclaredFields(Class<?> clazz) {
        return this.declaredFieldsCache.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private EventListener<?> getEventListener(Object o, Field field) {
        boolean accessible = field.canAccess(o);
        field.setAccessible(true);
        try {
            EventListener eventListener = (EventListener)field.get(o);
            return eventListener;
        }
        catch (IllegalAccessException e) {
            this.errorHandler.accept(e);
            EventListener<?> eventListener = null;
            return eventListener;
        }
        finally {
            field.setAccessible(accessible);
        }
    }
}
