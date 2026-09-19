package dev.grinn34.pearlload.pearl;

import dev.grinn34.pearlload.config.PearlLoadSettings;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.projectile.EntityEnderPearl;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PearlTracker {

    private static final String PEARL_ID = "minecraft:ender_pearl";

    private final PearlLoadSettings settings;
    private final ParkedPearlStore store;
    private final Map<Long, PearlTicket> tickets = new ConcurrentHashMap<>();

    public PearlTracker(PearlLoadSettings settings, ParkedPearlStore store) {
        this.settings = settings;
        this.store = store;
    }

    public void track(EntityEnderPearl pearl) {
        track(pearl, ownerOf(pearl));
    }

    public void track(EntityEnderPearl pearl, String owner) {
        pearl.setPersistent(true);
        tickets.compute(pearl.getId(), (id, existing) -> {
            PearlTicket ticket = existing != null ? existing : new PearlTicket(pearl, settings, owner);
            ticket.owner(owner);
            ticket.refresh();
            return ticket;
        });
    }

    public void drop(long entityId) {
        PearlTicket ticket = tickets.remove(entityId);
        if (ticket != null) {
            ticket.release();
        }
    }

    public void dropLevel(Level level) {
        Map<String, List<ParkedPearl>> parked = new ConcurrentHashMap<>();
        Iterator<Map.Entry<Long, PearlTicket>> it = tickets.entrySet().iterator();
        while (it.hasNext()) {
            PearlTicket ticket = it.next().getValue();
            if (!ticket.holds(level)) {
                continue;
            }
            ParkedPearl shot = snapshot(ticket);
            if (shot != null) {
                parked.computeIfAbsent(shot.owner(), key -> new ArrayList<>()).add(shot);
            }
            closeQuietly(ticket.pearl());
            ticket.release();
            it.remove();
        }
        parked.values().forEach(store::addAll);
    }

    public void tick() {
        Iterator<Map.Entry<Long, PearlTicket>> it = tickets.entrySet().iterator();
        while (it.hasNext()) {
            if (!it.next().getValue().refresh()) {
                it.remove();
            }
        }
    }

    public void adoptLoaded(Server server) {
        for (Level level : server.getLevels().values()) {
            for (Entity entity : level.getEntities()) {
                if (entity instanceof EntityEnderPearl pearl) {
                    track(pearl);
                }
            }
        }
    }

    public void parkPlayer(Player player) {
        String owner = Owners.id(player);
        store.replaceOwner(owner, snapshotOwned(owner, player));
        closeOwned(owner, player);
    }

    public void parkAll() {
        Map<String, List<ParkedPearl>> byOwner = new ConcurrentHashMap<>();
        for (PearlTicket ticket : tickets.values()) {
            ParkedPearl shot = snapshot(ticket);
            if (shot == null) {
                continue;
            }
            byOwner.computeIfAbsent(shot.owner(), key -> new ArrayList<>()).add(shot);
            closeQuietly(ticket.pearl());
        }
        byOwner.forEach(store::replaceOwner);
        releaseAll();
    }

    public int restorePlayer(Player player) {
        String owner = Owners.id(player);
        List<ParkedPearl> parked = store.of(owner);
        if (parked.isEmpty()) {
            return 0;
        }
        List<ParkedPearl> leftover = new ArrayList<>();
        int spawned = 0;
        for (ParkedPearl shot : parked) {
            EntityEnderPearl pearl = spawn(shot, player);
            if (pearl == null) {
                leftover.add(shot);
                continue;
            }
            track(pearl, owner);
            spawned++;
        }
        store.replaceOwner(owner, leftover);
        return spawned;
    }

    public void vanishPlayer(Player player) {
        String owner = Owners.id(player);
        closeOwned(owner, player);
        store.removeOwner(owner);
    }

    public void releaseAll() {
        for (PearlTicket ticket : tickets.values()) {
            ticket.release();
        }
        tickets.clear();
    }

    private List<ParkedPearl> snapshotOwned(String owner, Player player) {
        List<ParkedPearl> shots = new ArrayList<>();
        for (PearlTicket ticket : tickets.values()) {
            if (!ownedBy(ticket, owner, player)) {
                continue;
            }
            ParkedPearl shot = snapshot(ticket);
            if (shot != null) {
                shots.add(shot);
            }
        }
        return shots;
    }

    private void closeOwned(String owner, Player player) {
        Iterator<Map.Entry<Long, PearlTicket>> it = tickets.entrySet().iterator();
        while (it.hasNext()) {
            PearlTicket ticket = it.next().getValue();
            if (!ownedBy(ticket, owner, player)) {
                continue;
            }
            closeQuietly(ticket.pearl());
            ticket.release();
            it.remove();
        }
    }

    private boolean ownedBy(PearlTicket ticket, String owner, Player player) {
        if (owner.equals(ticket.owner())) {
            return true;
        }
        return ticket.pearl().shootingEntity == player;
    }

    private ParkedPearl snapshot(PearlTicket ticket) {
        EntityEnderPearl pearl = ticket.pearl();
        if (pearl.closed) {
            return null;
        }
        Level level = pearl.getLevel();
        if (level == null) {
            return null;
        }
        String owner = ticket.owner();
        if (owner == null && pearl.shootingEntity instanceof Player player) {
            owner = Owners.id(player);
        }
        if (owner == null) {
            return null;
        }
        Vector3 motion = pearl.getMotion();
        return new ParkedPearl(
                owner,
                level.getFolderName(),
                pearl.getX(),
                pearl.getY(),
                pearl.getZ(),
                motion.getX(),
                motion.getY(),
                motion.getZ(),
                (float) pearl.getYaw(),
                (float) pearl.getPitch(),
                pearl.getAge()
        );
    }

    private EntityEnderPearl spawn(ParkedPearl shot, Player owner) {
        Level level = levelByFolder(owner.getServer(), shot.levelFolder());
        if (level == null) {
            return null;
        }
        int cx = ChunkPositions.chunkOf(shot.x());
        int cz = ChunkPositions.chunkOf(shot.z());
        level.loadChunk(cx, cz, settings.generateChunks());
        IChunk chunk = level.getChunk(cx, cz, true);
        if (chunk == null) {
            return null;
        }
        Vector3 pos = new Vector3(shot.x(), shot.y(), shot.z());
        Vector3 motion = new Vector3(shot.motionX(), shot.motionY(), shot.motionZ());
        CompoundTag nbt = Entity.getDefaultNBT(pos, motion, shot.yaw(), shot.pitch());
        nbt.putShort("Age", (short) Math.clamp(shot.age(), 0, Short.MAX_VALUE));
        Entity created = Entity.createEntity(PEARL_ID, chunk, nbt, owner);
        if (!(created instanceof EntityEnderPearl pearl)) {
            if (created != null) {
                created.close();
            }
            return null;
        }
        pearl.setPersistent(true);
        pearl.spawnToAll();
        return pearl;
    }

    private static Level levelByFolder(Server server, String folder) {
        Level named = server.getLevelByName(folder);
        if (named != null) {
            return named;
        }
        for (Level level : server.getLevels().values()) {
            if (folder.equals(level.getFolderName())) {
                return level;
            }
        }
        return null;
    }

    private static String ownerOf(EntityEnderPearl pearl) {
        if (pearl.shootingEntity instanceof Player player) {
            return Owners.id(player);
        }
        return null;
    }

    private static void closeQuietly(EntityEnderPearl pearl) {
        if (!pearl.closed) {
            pearl.close();
        }
    }
}
