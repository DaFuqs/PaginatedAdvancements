package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.frames.*;
import net.minecraft.advancement.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Advancement.class)
public abstract class AdvancementMixin {
	
	@ModifyVariable(method = "createNameFromDisplay(Lnet/minecraft/advancement/AdvancementDisplay;)Lnet/minecraft/text/Text;", at = @At("STORE"), ordinal = 0)
	private static Formatting paginatedAdvancements$customFormat(Formatting formatting, AdvancementDisplay display) {
		FrameWrapper frameWrapper = AdvancementFrameDataLoader.get(display.getBackground()); // TODO
		if (frameWrapper instanceof FrameWrapper.PaginatedFrameWrapper) {
			return frameWrapper.getTitleFormat();
		}
		return formatting;
	}
	
}
