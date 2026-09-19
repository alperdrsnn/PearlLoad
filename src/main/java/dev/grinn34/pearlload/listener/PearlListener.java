package dev.grinn34.pearlload.listener;

import dev.grinn34.pearlload.config.PearlLoadSettings;
import dev.grinn34.pearlload.pearl.PearlTracker;
import org.powernukkitx.Player;
import org.powernukkitx.entity.projectile.EntityEnderPearl;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityDespawnEvent;
import org.powernukkitx.event.entity.EntitySpawnEvent;
import org.powernukkitx.event.entity.ProjectileLaunchEvent;
import org.powernukkitx.event.level.LevelUnloadEvent;
import org.powernukkitx.event.player.PlayerDeathEvent;
import org.powernukkitx.event.player.PlayerJoinEvent;
import org.powernukkitx.event.player.PlayerQuitEvent;
import org.powernukkitx.plugin.Plugin;

public final class PearlListener implements Listener {

    private final Plugin plugin;
    private final PearlTracker tracker;
    private final PearlLoadSettings settings;

    public PearlListener(Plugin plugin, PearlTracker tracker, PearlLoadSettings settings) {
        this.plugin = plugin;
        this.tracker = tracker;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof EntityEnderPearl pearl) {
            tracker.track(pearl);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EntityEnderPearl pearl) {
            tracker.track(pearl);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDespawn(EntityDespawnEvent event) {
        if (event.getEntity() instanceof EntityEnderPearl pearl) {
            tracker.drop(pearl.getId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        tracker.parkPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            if (player.isOnline()) {
                tracker.restorePlayer(player);
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (settings.vanishOnDeath()) {
            tracker.vanishPlayer(event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevelUnload(LevelUnloadEvent event) {
        tracker.dropLevel(event.getLevel());
    }
}
