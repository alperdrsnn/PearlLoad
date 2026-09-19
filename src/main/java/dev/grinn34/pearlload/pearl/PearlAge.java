package dev.grinn34.pearlload.pearl;

import org.powernukkitx.entity.Entity;

import java.lang.reflect.Field;

/**
 * PNX {@code EntityEnderPearl} closes at age 1200. Java pearls do not expire.
 */
public final class PearlAge {

    private static final Field AGE = ageField();

    private PearlAge() {
    }

    public static void hold(Entity pearl) {
        if (AGE == null || pearl == null) {
            return;
        }
        try {
            AGE.setInt(pearl, Math.min(pearl.getAge(), 1));
        } catch (IllegalAccessException ignored) {
        }
    }

    private static Field ageField() {
        try {
            Field field = Entity.class.getDeclaredField("age");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
