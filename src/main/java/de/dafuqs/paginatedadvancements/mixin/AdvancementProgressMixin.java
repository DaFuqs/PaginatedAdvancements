package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.accessors.AdvancementProgressAccessor;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.criterion.CriterionProgress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(AdvancementProgress.class)
public abstract class AdvancementProgressMixin implements AdvancementProgressAccessor {
    
    @Shadow @Final private Map<String, CriterionProgress> criteriaProgresses;
    
    @Shadow public abstract String toString();
    
    @Shadow private String[][] requirements;
    
    public CriterionProgress getCriterion(String criterion) {
        return criteriaProgresses.get(criterion);
    }
    
    public String[][] getRequirements() {
        return requirements;
    }
    
}