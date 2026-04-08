package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.client.PaginatedAdvancementScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	
	@Shadow
	@Nullable
	public LocalPlayer player;
	
	/**
	 * Redirect all calls to the vanilla advancement screen to out custom one
	 * Other screens that extend AdvancementScreen will not be touched
	 */
	@ModifyVariable(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), argsOnly = true)
	private Screen paginatedAdvancements$modifyAdvancementsScreen(Screen screen) {
		if (screen != null && AdvancementsScreen.class == screen.getClass()) {
			return new PaginatedAdvancementScreen(player.connection.getAdvancements());
		} else {
			return screen;
		}
	}
}
