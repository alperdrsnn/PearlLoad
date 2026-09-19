# PearlLoad

PowerNukkitX plugin. Ender pearls keep the chunk they are in loaded, the same way Java Edition tickets a pearl.

Without this, a Bedrock pearl that leaves the player's view distance is unloaded mid-flight and never lands.

## Requirements

- PowerNukkitX 3.0.4+
- Java 21

## Install

Drop `PearlLoad-1.2.0.jar` into `plugins/` and restart.

## Config

`plugins/PearlLoad/config.yml`

| Key | Default | Meaning |
|---|---|---|
| `ticket-radius` | `1` | `0` holds only the pearl's chunk. `1` holds a 3×3 so a chunk-border tick does not drop it. Capped at `2`. |
| `generate-chunks` | `true` | Generate terrain the pearl flies into (Java does this). |
| `vanish-on-death` | `true` | Java `enderPearlsVanishOnDeath`. |

Pearls are bound to the thrower: they unload when that player quits (or the server stops) and respawn when they join. Chunks are not held while the owner is offline. They do not expire after 60 seconds.

## Build

```
./gradlew.bat build
```

Jar: `build/libs/PearlLoad-1.2.0.jar`
