package com.hughbone.creepertag.mixin;

import com.hughbone.creepertag.CreeperAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class EntityTypeMixin<T extends Entity> {

    // Save player who used spawn egg
    @Inject(method = "spawnFromItemStack", at = @At("RETURN"))
    public void spawnFromItemStack(ServerWorld world, @Nullable ItemStack stack, @Nullable PlayerEntity player, BlockPos pos, SpawnReason spawnReason, boolean alignPosition, boolean invertY, CallbackInfoReturnable<T> cir) {
        if (player != null
            && cir.getReturnValue() instanceof CreeperEntity creeper
            && ((CreeperAccessor) creeper).getIsTagger()
        ) {
            ((CreeperAccessor) creeper).setSpawnSource(player.getNameForScoreboard());
        }
    }
}
