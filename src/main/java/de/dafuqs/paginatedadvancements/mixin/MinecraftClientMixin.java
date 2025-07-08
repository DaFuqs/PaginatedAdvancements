package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.client.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.advancements.*;
import net.minecraft.client.player.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
	
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
