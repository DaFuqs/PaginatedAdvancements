package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.frames.*;
import net.minecraft.advancement.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Advancement.class)
public abstract class AdvancementMixin {
    
    /**
     * Redirect all calls to the vanilla advancement screen to out custom one
     * Other screens that extend AdvancementScreen will not be touched
     */
    @ModifyVariable(method = "<init>", at = @At("STORE"))
    private Formatting paginatedAdvancements$customFormat(Formatting formatting, Identifier id) {
        FrameWrapper frameWrapper = AdvancementFrameDataLoader.get(id);
        if (frameWrapper instanceof FrameWrapper.PaginatedFrameWrapper) {
			return frameWrapper.getTitleFormat();
		}
        return formatting;
    }
    
}
