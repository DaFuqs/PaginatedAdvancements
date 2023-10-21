package de.dafuqs.paginatedadvancements.config;

import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;
import net.minecraft.client.*;

import java.util.*;

@Config(name = "PaginatedAdvancements")
public class PaginatedAdvancementsConfig implements ConfigData {
	
	@Comment("Enable pinning of advancement tabs")
	public boolean PinningEnabled = true;
	
	@Comment("Show advancement ID in debug tooltip")
	public boolean ShowAdvancementIDInDebugTooltip = true;
	
	@Comment("Show Debug Info on advancement hover (possible values: 'always', 'never', 'debug_tooltips_enabled')")
	public DebugInfoSetting ShowDebugInfo = DebugInfoSetting.DEBUG_TOOLTIPS_ENABLED;
	
	@Comment("Max displayed advancement criterion entries when not pressing shift")
	public int MaxCriterionEntries = 2;
	
	@Comment("Fade the background slightly gray when hovering over an advancement")
	public boolean FadeOutBackgroundOnAdvancementHover = true;
	
	@Comment("Save and restore the last selected tab")
	public boolean SaveLastSelectedTab = true;
	
	@Comment("List of pinned Tabs (root advancement identifier)")
	public List<String> PinnedTabs = new ArrayList<>();
	
	@Comment("The identifier of the last opened tab")
	public String LastSelectedTab = "";
	
	@Comment("Spacing between horizontal tabs (vanilla default: 4)")
	public int SpacingBetweenHorizontalTabs = 4;
	
	@Comment("Spacing between pinned tabs")
	public int SpacingBetweenPinnedTabs = 2;
	
	public enum DebugInfoSetting {
		ALWAYS,
		DEBUG_TOOLTIPS_ENABLED,
		NEVER
	}
	
	@Override
	public void validatePostLoad() {
	
	}
	
	public boolean shouldShowAdvancementDebug(MinecraftClient client) {
		switch (ShowDebugInfo) {
			case ALWAYS -> {
				return true;
			}
			case DEBUG_TOOLTIPS_ENABLED -> {
				return client.options.advancedItemTooltips;
			}
			default -> {
				return false;
			}
		}
	}
	
}
