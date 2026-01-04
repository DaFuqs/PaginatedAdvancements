package de.dafuqs.paginatedadvancements.config;

import com.electronwill.nightconfig.core.*;
import net.minecraft.client.*;
import net.minecraft.resources.*;
import net.neoforged.neoforge.common.*;
import org.apache.commons.lang3.tuple.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class PaginatedAdvancementsConfig {
	
	public static final PaginatedAdvancementsConfig CONFIG;
	public static final ModConfigSpec CONFIG_SPEC;
	
	public ModConfigSpec.BooleanValue PinningEnabled;
	public ModConfigSpec.BooleanValue ShowAdvancementIDInDebugTooltip;
	public ModConfigSpec.EnumValue<@NotNull DebugInfoSetting> ShowDebugInfo;
	public ModConfigSpec.ConfigValue<Integer> MaxCriterionEntries;
	public ModConfigSpec.BooleanValue FadeOutBackgroundOnAdvancementHover;
	public ModConfigSpec.BooleanValue SaveLastSelectedTab;
	public ModConfigSpec.ConfigValue<List<String>> PinnedTabs;
	public ModConfigSpec.ConfigValue<String> LastSelectedTab;
	
	public ModConfigSpec.ConfigValue<Integer> SpacingBetweenHorizontalTabs;
	public ModConfigSpec.ConfigValue<Integer> SpacingBetweenPinnedTabs;
	
	public static void saveSelectedTab(Identifier tabIdentifier) {
		if (CONFIG.SaveLastSelectedTab.getAsBoolean()) {
			CONFIG.LastSelectedTab.set(tabIdentifier.toString());
			CONFIG_SPEC.save();
		}
	}
	
	public static void pinTab(Identifier tabIdentifier) {
		String identifierString = tabIdentifier.toString();
		if (!CONFIG.PinnedTabs.get().contains(identifierString)) {
			CONFIG.PinnedTabs.get().add(identifierString);
			CONFIG_SPEC.save();
		}
	}
	
	public static void unpinTab(Identifier tabIdentifier) {
		String identifierString = tabIdentifier.toString();
		if (CONFIG.PinnedTabs.get().contains(identifierString)) {
			CONFIG.PinnedTabs.get().remove(identifierString);
			CONFIG_SPEC.save();
		}
	}
	
	public static boolean isPinned(Identifier tabIdentifier) {
		return CONFIG.PinningEnabled.get() && CONFIG.PinnedTabs.get().contains(tabIdentifier.toString());
	}
	
	public static boolean hasPins() {
		return CONFIG.PinningEnabled.get() && !CONFIG.PinnedTabs.get().isEmpty();
	}
	
	public static List<String> getPinnedTabs() {
		return CONFIG.PinnedTabs.get();
	}
	
	public static int getPinIndex(Identifier tabIdentifier) {
		return CONFIG.PinnedTabs.get().indexOf(tabIdentifier.toString());
	}
	
	public enum DebugInfoSetting {
		ALWAYS,
		DEBUG_TOOLTIPS_ENABLED,
		NEVER
	}
	
	static {
		Pair<PaginatedAdvancementsConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(PaginatedAdvancementsConfig::new);
		CONFIG = pair.getLeft();
		CONFIG_SPEC = pair.getRight();
	}
	
	private PaginatedAdvancementsConfig(ModConfigSpec.Builder builder) {
		PinningEnabled = builder.define("pinning_enabled", true);
		ShowAdvancementIDInDebugTooltip = builder.define("show_advancement_id_in_debug_tooltip", true);
		ShowDebugInfo = builder.defineEnum("show_debug_info", DebugInfoSetting.DEBUG_TOOLTIPS_ENABLED, EnumGetMethod.ORDINAL_OR_NAME);
		MaxCriterionEntries = builder.define("max_criterion_entries", 2);
		FadeOutBackgroundOnAdvancementHover = builder.define("fade_out_on_advancement_hover", true);
		SaveLastSelectedTab = builder.define("save_last_selected_tab", true);
		PinnedTabs = (ModConfigSpec.ConfigValue<List<String>>) (Object) builder.defineList("pinned_tabs", List.of(), () -> "mymod:myid", s -> true);
		LastSelectedTab = builder.define("last_selected_tab", "");
		SpacingBetweenHorizontalTabs = builder.define("spacing_between_horizontal_tabs", 4);
		SpacingBetweenPinnedTabs = builder.define("spacing_between_pinned_tabs", 2);
	}
	
	public boolean shouldShowAdvancementDebug(Minecraft client) {
		switch (ShowDebugInfo.get()) {
			case DebugInfoSetting.ALWAYS -> {
				return true;
			}
			case DebugInfoSetting.DEBUG_TOOLTIPS_ENABLED -> {
				return client.options.advancedItemTooltips;
			}
			default -> {
				return false;
			}
		}
	}
	
}
