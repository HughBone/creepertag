package com.hughbone.creepertag.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity {

    @Shadow protected abstract void explode();
    @Unique private boolean isTagger;

    protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void readnbt(NbtCompound nbt, CallbackInfo ci) {
        isTagger = nbt.contains("creeper_tag");
    }

    @Unique
    private Text getRainbowText(String name) {
        StringBuilder rainbowName = new StringBuilder();
        Formatting[] colors = {
                Formatting.RED, Formatting.GOLD, Formatting.YELLOW, Formatting.GREEN,
                Formatting.AQUA, Formatting.BLUE, Formatting.LIGHT_PURPLE
        };
        for (int i = 0; i < name.length(); i++) {
            Formatting color = colors[i % colors.length];
            rainbowName.append(color).append(name.charAt(i));
        }
        return Text.literal(rainbowName.toString());
    }

    @Inject(method = "getHurtSound", at = @At("HEAD"))
    private void injected(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        if (isTagger) return;

        // Remove diamond, explode creeper, summon spawn egg
        if (source.getSource() instanceof ServerPlayerEntity) {
            ServerPlayerEntity attacker = (ServerPlayerEntity) source.getSource();
            ItemStack handStack = attacker.getMainHandStack();
            if (handStack.getItem().equals(Items.DIAMOND)) {
                // Remove diamond + explode
                int slot = attacker.getInventory().getSlotWithStack(handStack);
                attacker.getInventory().removeStack(slot, 1);
                this.explode();

                // Spawn egg nbt
                NbtCompound itemNbt = new NbtCompound();
                NbtCompound nbtTags = new NbtCompound();
                nbtTags.putBoolean("creeper_tag", true);
                itemNbt.put("EntityTag", nbtTags);

                // Drop egg
                ItemStack spawnEgg = new ItemStack(RegistryEntry.of(Items.CREEPER_SPAWN_EGG), 1, Optional.of(itemNbt));
                spawnEgg.setCustomName(Text.of(getRainbowText("Tag!")));
                this.dropStack(spawnEgg);
            }
        }
    }

    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"), index = 5)
    private World.ExplosionSourceType injected(World.ExplosionSourceType explosionSourceType) {
        return isTagger ? World.ExplosionSourceType.NONE
                : explosionSourceType;
    }

}
