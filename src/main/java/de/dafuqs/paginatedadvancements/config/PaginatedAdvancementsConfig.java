package de.dafuqs.paginatedadvancements.config;

import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.*;
import net.minecraft.client.*;

import java.util.*;

@Config(name = "PaginatedAdvancements")
public class PaginatedAdvancementsConfig implements ConfigData {
	
	public boolean PinningEnabled = true;
	public boolean ShowAdvancementIDInDebugTooltip = true;
	public DebugInfoSetting ShowDebugInfo = DebugInfoSetting.DEBUG_TOOLTIPS_ENABLED;
	public int MaxCriterionEntries = 2;
	public boolean FadeOutBackgroundOnAdvancementHover = true;
	public boolean SaveLastSelectedTab = true;
	public List<String> PinnedTabs = new ArrayList<>();
	public String LastSelectedTab = "";
	@ConfigEntry.Gui.Tooltip
	public int SpacingBetweenHorizontalTabs = 4;
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
