package de.dafuqs.paginatedadvancements.accessors;

import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.util.Identifier;

public interface AdvancementWidgetAccessor {
    
    AdvancementProgress getProgress();
    Identifier getAdvancementID();
    
}