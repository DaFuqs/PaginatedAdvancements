package de.dafuqs.paginatedadvancements.mixin;

import net.minecraft.advancement.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(AdvancementProgress.class)
public interface AdvancementProgressAccessor {
	
	@Accessor(value = "requirements")
	String[][] getRequirements();
	
}