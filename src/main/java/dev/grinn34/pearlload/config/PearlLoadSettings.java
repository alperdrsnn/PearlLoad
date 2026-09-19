package dev.grinn34.pearlload.config;

import org.powernukkitx.utils.Config;

public record PearlLoadSettings(int ticketRadius, boolean generateChunks, boolean vanishOnDeath) {

    public static final int DEFAULT_RADIUS = 1;
    public static final int MAX_RADIUS = 2;

    public static PearlLoadSettings from(Config config) {
        int radius = config.getInt("ticket-radius", DEFAULT_RADIUS);
        return new PearlLoadSettings(
                Math.clamp(radius, 0, MAX_RADIUS),
                config.getBoolean("generate-chunks", true),
                config.getBoolean("vanish-on-death", true)
        );
    }
}
