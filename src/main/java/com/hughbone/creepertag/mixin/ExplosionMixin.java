package com.hughbone.creepertag.mixin;

import com.hughbone.creepertag.CreeperAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Shadow @Final private DamageSource damageSource;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private ParticleEffect emitterParticle;
    @Shadow @Final private World world;

    @Unique private static boolean isTagger;

    // Make sure big explosion particles are present
    @Inject(method = "affectWorld", at=@At("HEAD"))
    private void affectWorld(boolean particles, CallbackInfo ci) {
        if (particles && isTagger) {
            ParticleEffect particleEffect;
            particleEffect = this.emitterParticle;
            this.world.addParticle(particleEffect, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            isTagger = false;
        }
    }

    // Make sure paintings / item entities don't get griefed
    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"))
    private List<Entity> collectBlocksAndDamageEntities(World world, Entity entity, Box box) {
        List<Entity> list = world.getOtherEntities(entity, box);

        if (damageSource.getSource() instanceof CreeperEntity creeper) {
            if (((CreeperAccessor) creeper).getIsTagger()) {
                isTagger = true;
                list.removeIf(e -> !(e instanceof LivingEntity));
                list.removeIf(e -> e instanceof ArmorStandEntity);
            }
        }

        return list;
    }

}
