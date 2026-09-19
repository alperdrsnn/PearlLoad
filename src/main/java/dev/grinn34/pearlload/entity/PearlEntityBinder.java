package dev.grinn34.pearlload.entity;

import org.powernukkitx.entity.EntityID;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.registry.EntityRegistry;
import org.powernukkitx.registry.RegisterException;
import org.powernukkitx.registry.Registries;

import java.lang.reflect.Field;
import java.util.Map;

public final class PearlEntityBinder {

    private PearlEntityBinder() {
    }

    public static void install(Plugin plugin) {
        try {
            Registries.ENTITY.registerOverrideEntity(plugin, EntityID.ENDER_PEARL, JavaEnderPearl.class);
            rebindClassMap(EntityID.ENDER_PEARL, JavaEnderPearl.class);
            plugin.getLogger().info("Ender pearl entity replaced (no 60s despawn).");
        } catch (RegisterException e) {
            plugin.getLogger().warning("Could not replace ender pearl entity: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void rebindClassMap(String id, Class<?> type) {
        try {
            Field field = EntityRegistry.class.getDeclaredField("CLASS");
            field.setAccessible(true);
            Map<String, Class<?>> classes = (Map<String, Class<?>>) field.get(null);
            classes.put(id, type);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
