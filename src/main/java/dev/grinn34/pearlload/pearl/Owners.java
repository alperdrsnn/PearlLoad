package dev.grinn34.pearlload.pearl;

import org.powernukkitx.Player;

final class Owners {

    private Owners() {
    }

    static String id(Player player) {
        String xuid = player.getXUID();
        if (xuid != null && !xuid.isBlank()) {
            return "xuid:" + xuid;
        }
        return "name:" + player.getName();
    }
}
