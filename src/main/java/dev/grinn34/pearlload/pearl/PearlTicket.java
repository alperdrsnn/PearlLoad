package dev.grinn34.pearlload.pearl;

import dev.grinn34.pearlload.config.PearlLoadSettings;
import org.powernukkitx.entity.projectile.EntityEnderPearl;
import org.powernukkitx.level.ChunkLoader;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.math.Vector3;

import java.util.HashSet;
import java.util.Set;

/**
 * PNX unloads a chunk when its last {@link ChunkLoader} leaves.
 * An ender pearl is not a loader, so we hold the ticket for it.
 */
final class PearlTicket implements ChunkLoader {

    private final EntityEnderPearl pearl;
    private final PearlLoadSettings settings;
    private final int loaderId;
    private final Set<Long> held = new HashSet<>();

    private String owner;
    private Level level;

    PearlTicket(EntityEnderPearl pearl, PearlLoadSettings settings, String owner) {
        this.pearl = pearl;
        this.settings = settings;
        this.owner = owner;
        this.loaderId = Level.generateChunkLoaderId(this);
    }

    EntityEnderPearl pearl() {
        return pearl;
    }

    String owner() {
        return owner;
    }

    void owner(String owner) {
        if (this.owner == null && owner != null) {
            this.owner = owner;
        }
    }

    boolean refresh() {
        if (!isLoaderActive()) {
            release();
            return false;
        }
        PearlAge.hold(pearl);
        Level current = pearl.getLevel();
        if (current == null) {
            release();
            return false;
        }
        if (level != null && level != current) {
            releaseFrom(level);
        }
        level = current;

        Vector3 motion = pearl.getMotion();
        Set<Long> wanted = ChunkPositions.flightTickets(
                pearl.getX(),
                pearl.getZ(),
                motion.getX(),
                motion.getZ(),
                32,
                0.01,
                settings.ticketRadius()
        );

        for (Long key : held) {
            if (!wanted.contains(key)) {
                unregister(level, key);
            }
        }
        for (Long key : wanted) {
            if (held.add(key)) {
                register(level, key);
            }
        }
        held.retainAll(wanted);
        pearl.scheduleUpdate();
        return true;
    }

    boolean holds(Level world) {
        return level == world;
    }

    void release() {
        if (level != null) {
            releaseFrom(level);
        }
        level = null;
        held.clear();
    }

    private void releaseFrom(Level world) {
        for (Long key : held) {
            unregister(world, key);
        }
    }

    private void register(Level world, long key) {
        int x = (int) (key >> 32);
        int z = (int) key;
        world.registerChunkLoader(this, x, z, false);
        world.cancelUnloadChunkRequest(x, z);
        world.loadChunk(x, z, settings.generateChunks());
    }

    private void unregister(Level world, long key) {
        world.unregisterChunkLoader(this, (int) (key >> 32), (int) key);
    }

    @Override
    public int getLoaderId() {
        return loaderId;
    }

    @Override
    public boolean isLoaderActive() {
        return !pearl.closed;
    }

    @Override
    public Position getPosition() {
        return pearl.getPosition();
    }

    @Override
    public double getX() {
        return pearl.getX();
    }

    @Override
    public double getZ() {
        return pearl.getZ();
    }

    @Override
    public Level getLevel() {
        return pearl.getLevel();
    }

    @Override
    public void onChunkChanged(IChunk chunk) {
    }

    @Override
    public void onChunkLoaded(IChunk chunk) {
    }

    @Override
    public void onChunkUnloaded(IChunk chunk) {
    }
}
