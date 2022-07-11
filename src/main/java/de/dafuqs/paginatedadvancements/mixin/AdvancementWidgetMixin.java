package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.accessors.AdvancementWidgetAccessor;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancementWidget.class)
public class AdvancementWidgetMixin implements AdvancementWidgetAccessor {
    
    @Shadow
    private AdvancementProgress progress;
    
    @Shadow @Final private Advancement advancement;
    
    public AdvancementProgress getProgress() {
        return this.progress;
    }
    
    public Identifier getAdvancementID() {
        return advancement.getId();
    }
    
}