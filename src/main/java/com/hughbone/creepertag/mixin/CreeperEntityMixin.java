package com.hughbone.creepertag.mixin;

import com.hughbone.creepertag.CreeperAccessor;
import com.hughbone.creepertag.MyUtil;
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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity implements CreeperAccessor {

    @Shadow protected abstract void explode();
    @Unique private boolean isTagger = false;
    @Unique private String spawnSource = "?";

    protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean getIsTagger() {
        return isTagger;
    }

    @Override
    public String getSpawnSource() {
        return spawnSource;
    }

    @Override
    public void setSpawnSource(String sourceName) {
        this.spawnSource = sourceName;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readnbt(NbtCompound nbt, CallbackInfo ci) {
        isTagger = nbt.contains("creeper_tag");
    }

    // Drop egg if attacked while holding diamond
    @Inject(method = "getHurtSound", at = @At("HEAD"))
    private void injected(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        if (isTagger) return;

        if (source.getSource() instanceof ServerPlayerEntity player) {
            ItemStack handStack = player.getMainHandStack();
            if (handStack.getItem().equals(Items.DIAMOND)) {
                handStack.decrement(1);
                this.explode();

                // Spawn egg nbt
                NbtCompound itemNbt = new NbtCompound();
                NbtCompound nbtTags = new NbtCompound();
                nbtTags.putBoolean("creeper_tag", true);
                itemNbt.put("EntityTag", nbtTags);

                // Drop egg
                ItemStack spawnEgg = new ItemStack(RegistryEntry.of(Items.CREEPER_SPAWN_EGG), 1, Optional.of(itemNbt));
                spawnEgg.setCustomName(Text.of(MyUtil.getRainbowText("Tag!")));

                this.dropStack(spawnEgg);
            }
        }
    }

    // Reduce blast radius by 20%
    @ModifyVariable(method = "explode", at = @At("STORE"), ordinal = 0)
    private float blastRadius(float f) {
        return 0.8f;
    }

    // Prevent mobGriefing
    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"), index = 5)
    private World.ExplosionSourceType explode(World.ExplosionSourceType explosionSourceType) {
        return isTagger ? World.ExplosionSourceType.NONE
                : explosionSourceType;
    }

}
