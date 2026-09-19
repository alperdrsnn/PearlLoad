package dev.grinn34.pearlload.entity;

import dev.grinn34.pearlload.pearl.PearlAge;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.projectile.EntityEnderPearl;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.nbt.tag.CompoundTag;

public final class JavaEnderPearl extends EntityEnderPearl {

    public JavaEnderPearl(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public JavaEnderPearl(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        PearlAge.hold(this);
        return super.onUpdate(currentTick);
    }
}
