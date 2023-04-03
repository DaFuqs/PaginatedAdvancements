package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.client.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.client.network.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	
	@Shadow
	@Nullable
	public ClientPlayerEntity player;
	
	/**
	 * Redirect all calls to the vanilla advancement screen to out custom one
	 * Other screens that extend AdvancementScreen will not be touched
	 */
	@ModifyVariable(method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), argsOnly = true)
	private Screen paginatedAdvancements$modifyAdvancementsScreen(Screen screen) {
		if (screen != null && AdvancementsScreen.class == screen.getClass()) {
			return new PaginatedAdvancementScreen(player.networkHandler.getAdvancementHandler());
		} else {
			return screen;
		}
	}
}
