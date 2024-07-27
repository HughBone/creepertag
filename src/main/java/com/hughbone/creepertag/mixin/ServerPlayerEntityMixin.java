package com.hughbone.creepertag.mixin;

import com.hughbone.creepertag.CreeperAccessor;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow public abstract ServerWorld getServerWorld();

    // Check if player tagged sucessfully
    @Inject(method = "damage", at = @At("HEAD"))
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof CreeperEntity creeper
            && ((CreeperAccessor) creeper).getIsTagger()
        ) {
            String sourcePlayerName = ((CreeperAccessor) creeper).getSpawnSource();
            String destPlayerName = ((ServerPlayerEntity)(Object)this).getNameForScoreboard();

            if (!sourcePlayerName.equals(destPlayerName)) {
                this.getServerWorld().getPlayers().forEach(player -> {
                    Text CTMessage = Text.literal(sourcePlayerName)
                            .append(Text.literal(" Creeper Tagged ").formatted(Formatting.GREEN))
                            .append(destPlayerName + " !!");
                    player.sendMessage(CTMessage);
                });

                ((ServerPlayerEntity)(Object)this).addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 3 * 20));
            }
        }
    }

}
