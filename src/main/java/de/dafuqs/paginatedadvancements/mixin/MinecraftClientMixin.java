package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.client.PaginatedAdvancementScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    
    /**
     * Redirect all calls to the vanilla advancement screen to out custom one
     * Other screens that extend AdvancementScreen will not be touched
     */
    @ModifyVariable(method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), argsOnly = true)
    private Screen openScreen(Screen screen) {
        if (screen != null && AdvancementsScreen.class == screen.getClass()) {
            return new PaginatedAdvancementScreen(player.networkHandler.getAdvancementHandler());
        } else {
            return screen;
        }
    }
}
