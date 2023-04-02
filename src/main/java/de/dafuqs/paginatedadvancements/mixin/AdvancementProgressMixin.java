package de.dafuqs.paginatedadvancements.mixin;

import de.dafuqs.paginatedadvancements.accessors.*;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.*;
import org.spongepowered.asm.mixin.*;

import java.util.*;

@Mixin(AdvancementProgress.class)
public abstract class AdvancementProgressMixin implements AdvancementProgressAccessor {
    
    @Shadow
    @Final
    Map<String, CriterionProgress> criteriaProgresses;
    
    @Shadow public abstract String toString();
    
    @Shadow private String[][] requirements;
    
    public CriterionProgress getCriterion(String criterion) {
        return criteriaProgresses.get(criterion);
    }
    
    public String[][] getRequirements() {
        return requirements;
    }
    
}