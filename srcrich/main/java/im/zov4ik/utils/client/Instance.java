package im.zov4ik.utils.client;

import lombok.experimental.UtilityClass;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.features.module.Module;
import im.zov4ik.zov4ik;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@UtilityClass
public class Instance {
    private final ConcurrentMap<Class<? extends Module>, Module> instanceModules = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AbstractDraggable>, AbstractDraggable> instanceDraggables = new ConcurrentHashMap<>();

    public <T extends Module> T get(Class<T> clazz) {
        return clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> zov4ik.getInstance().getModuleProvider().get(instance)));
    }

    public <T extends Module> T get(String module) {
        return zov4ik.getInstance().getModuleProvider().get(module);
    }

    public <T extends AbstractDraggable> T getDraggable(Class<T> clazz) {
        return clazz.cast(instanceDraggables.computeIfAbsent(clazz, instance -> zov4ik.getInstance().getDraggableRepository().get(instance)));
    }

    public <T extends AbstractDraggable> T getDraggable(String draggable) {
        return zov4ik.getInstance().getDraggableRepository().get(draggable);
    }
}
