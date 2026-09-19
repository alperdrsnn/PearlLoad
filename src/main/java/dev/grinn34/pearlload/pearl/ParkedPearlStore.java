package dev.grinn34.pearlload.pearl;

import org.powernukkitx.utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java parks thrower-owned pearls on logout and writes them back on join.
 * This file is that parking lot across quit / stop / crash-after-quit.
 */
public final class ParkedPearlStore {

    private static final String KEY = "pearls";

    private final Config config;

    public ParkedPearlStore(File file) {
        this.config = new Config(file, Config.YAML);
    }

    public List<ParkedPearl> all() {
        List<ParkedPearl> pearls = new ArrayList<>();
        for (Map<?, ?> row : config.getMapList(KEY)) {
            ParkedPearl pearl = ParkedPearl.fromMap(row);
            if (pearl != null) {
                pearls.add(pearl);
            }
        }
        return pearls;
    }

    public List<ParkedPearl> of(String owner) {
        return all().stream().filter(pearl -> pearl.owner().equals(owner)).toList();
    }

    public void replaceOwner(String owner, List<ParkedPearl> pearls) {
        List<ParkedPearl> next = new ArrayList<>();
        for (ParkedPearl pearl : all()) {
            if (!pearl.owner().equals(owner)) {
                next.add(pearl);
            }
        }
        next.addAll(pearls);
        write(next);
    }

    public void addAll(List<ParkedPearl> pearls) {
        List<ParkedPearl> next = new ArrayList<>(all());
        next.addAll(pearls);
        write(next);
    }

    public void removeOwner(String owner) {
        replaceOwner(owner, List.of());
    }

    private void write(List<ParkedPearl> pearls) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ParkedPearl pearl : pearls) {
            rows.add(pearl.toMap());
        }
        config.set(KEY, rows);
        config.save();
    }
}
