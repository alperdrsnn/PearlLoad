package dev.grinn34.pearlload.pearl;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public final class ChunkPositions {

    private ChunkPositions() {
    }

    public static long key(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static int chunkOf(double block) {
        return (int) Math.floor(block) >> 4;
    }

    public static Set<Long> tickets(int chunkX, int chunkZ, int radius, int lookX, int lookZ) {
        Set<Long> keys = new LinkedHashSet<>();
        visit(chunkX, chunkZ, radius, lookX, lookZ, (x, z) -> keys.add(key(x, z)));
        return keys;
    }

    /**
     * Gravity/drag match {@code EntityEnderPearl} so we can load the landing
     * chunk before {@code move()} asks for it.
     */
    public static Set<Long> flightTickets(
            double x,
            double z,
            double motionX,
            double motionZ,
            int ticks,
            double drag,
            int radius
    ) {
        Set<Long> keys = new LinkedHashSet<>();
        int cx = chunkOf(x);
        int cz = chunkOf(z);
        addRadius(keys, cx, cz, radius);
        for (int i = 0; i < ticks; i++) {
            motionX *= 1 - drag;
            motionZ *= 1 - drag;
            x += motionX;
            z += motionZ;
            int nx = chunkOf(x);
            int nz = chunkOf(z);
            if (nx != cx || nz != cz) {
                addRadius(keys, nx, nz, radius);
                cx = nx;
                cz = nz;
            }
        }
        return keys;
    }

    public static void visit(int chunkX, int chunkZ, int radius, int lookX, int lookZ, BiConsumer<Integer, Integer> consumer) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius < 0");
        }
        for (int x = chunkX - radius; x <= chunkX + radius; x++) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                consumer.accept(x, z);
            }
        }
        if (Math.abs(lookX - chunkX) > radius || Math.abs(lookZ - chunkZ) > radius) {
            consumer.accept(lookX, lookZ);
        }
    }

    private static void addRadius(Set<Long> keys, int chunkX, int chunkZ, int radius) {
        visit(chunkX, chunkZ, radius, chunkX, chunkZ, (x, z) -> keys.add(key(x, z)));
    }
}
