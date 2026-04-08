package de.dafuqs.paginatedadvancements.mixin;

import net.minecraft.advancement.*;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.text.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.*;

@Mixin(AdvancementWidget.class)
public interface AdvancementWidgetAccessor {
	
	@Accessor(value = "x")
	int getX();
	
	@Accessor(value = "y")
	int getY();
	
	@Accessor(value = "advancementNode")
    AdvancementNode getAdvancementNode();
	
	@Accessor(value = "display")
    DisplayInfo getDisplay();
	
	@Accessor(value = "progress")
	@Nullable AdvancementProgress getProgress();
	
	@Accessor(value = "children")
	List<AdvancementWidget> getChildren();
	
	@Accessor(value = "width")
	int getWidth();
	
	@Accessor(value = "description")
	List<FormattedCharSequence> getDescription();
	
	@Accessor(value = "tab")
    AdvancementTab getTab();
	
	@Accessor(value = "titleLines")
	List<FormattedCharSequence> getTitleLines();
	
	@Invoker(value = "findOptimalLines")
	List<FormattedText> invokeFindOptimalLines(Component text, int width);
	
}