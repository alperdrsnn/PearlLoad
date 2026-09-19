package dev.grinn34.pearlload;

import dev.grinn34.pearlload.config.PearlLoadSettings;
import dev.grinn34.pearlload.entity.PearlEntityBinder;
import dev.grinn34.pearlload.listener.PearlListener;
import dev.grinn34.pearlload.pearl.ParkedPearlStore;
import dev.grinn34.pearlload.pearl.PearlTracker;
import org.powernukkitx.plugin.PluginBase;

import java.io.File;

public final class PearlLoadPlugin extends PluginBase {

    private PearlTracker tracker;

    @Override
    public void onLoad() {
        PearlEntityBinder.install(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PearlLoadSettings settings = PearlLoadSettings.from(getConfig());
        ParkedPearlStore store = new ParkedPearlStore(new File(getDataFolder(), "parked.yml"));
        tracker = new PearlTracker(settings, store);
        getServer().getPluginManager().registerEvents(new PearlListener(this, tracker, settings), this);
        getServer().getScheduler().scheduleRepeatingTask(this, tracker::tick, 1);
        tracker.adoptLoaded(getServer());
    }

    @Override
    public void onDisable() {
        if (tracker != null) {
            tracker.parkAll();
        }
    }
}
