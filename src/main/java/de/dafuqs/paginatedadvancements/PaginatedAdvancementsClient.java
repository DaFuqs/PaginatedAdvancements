package de.dafuqs.paginatedadvancements;

import de.dafuqs.paginatedadvancements.config.PaginatedAdvancementsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.ConfigManager;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

import java.util.List;

public class PaginatedAdvancementsClient implements ClientModInitializer {
	
	public static ConfigManager<PaginatedAdvancementsConfig> CONFIG_MANAGER;
	public static PaginatedAdvancementsConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		ConfigHolder<PaginatedAdvancementsConfig> configHolder = AutoConfig.register(PaginatedAdvancementsConfig.class, JanksonConfigSerializer::new);
		CONFIG_MANAGER = ((ConfigManager<PaginatedAdvancementsConfig>) configHolder);
		CONFIG = AutoConfig.getConfigHolder(PaginatedAdvancementsConfig.class).getConfig();
	}
	
	private static void updateConfigFile() {
		//CONFIG.initialSpawnPointActive = initialSpawnActive;
		//CONFIG.save();
	}
	
	public static void saveSelectedTab(Identifier tabIdentifier) {
		if(CONFIG.SaveLastSelectedTab) {
			CONFIG.LastSelectedTab = tabIdentifier.toString();
			CONFIG_MANAGER.save();
		}
	}
	
	public static void pinTab(Identifier tabIdentifier) {
		String identifierString = tabIdentifier.toString();
		if(!CONFIG.PinnedTabs.contains(identifierString)) {
			CONFIG.PinnedTabs.add(identifierString);
			CONFIG_MANAGER.save();
		}
	}
	
	public static void unpinTab(Identifier tabIdentifier) {
		String identifierString = tabIdentifier.toString();
		if(CONFIG.PinnedTabs.contains(identifierString)) {
			CONFIG.PinnedTabs.remove(identifierString);
			CONFIG_MANAGER.save();
		}
	}
	
	public static boolean isPinned(Identifier tabIdentifier) {
		return CONFIG.PinnedTabs.contains(tabIdentifier.toString());
	}
	
	public static boolean hasPins() {
		return !CONFIG.PinnedTabs.isEmpty();
	}
	
	public static List<String> getPinnedTabs() {
		return CONFIG.PinnedTabs;
	}
	
	public static int getPinIndex(Identifier tabIdentifier) {
		return CONFIG.PinnedTabs.indexOf(tabIdentifier.toString());
	}
	
}
