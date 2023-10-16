package de.dafuqs.paginatedadvancements.mixin;

import net.minecraft.advancement.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.*;

@Mixin(AdvancementWidget.class)
public interface AdvancementWidgetAccessor {
	
	@Accessor(value = "x")
	int getX();
	
	@Accessor(value = "y")
	int getY();
	
	@Accessor(value = "advancement")
	PlacedAdvancement getAdvancement();
	
	@Accessor(value = "display")
	AdvancementDisplay getDisplay();
	
	@Accessor(value = "progress")
	AdvancementProgress getProgress();
	
	@Accessor(value = "children")
	List<AdvancementWidget> getChildren();
	
	@Accessor(value = "width")
	int getWidth();
	
	@Accessor(value = "description")
	List<OrderedText> getDescription();
	
	@Accessor(value = "tab")
	AdvancementTab getTab();
	
	@Accessor(value = "title")
	OrderedText getTitle();
	
	@Invoker(value = "wrapDescription")
	List<StringVisitable> invokeWrapDescription(Text text, int width);
	
}