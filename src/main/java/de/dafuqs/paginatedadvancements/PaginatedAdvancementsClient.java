package de.dafuqs.paginatedadvancements;

import de.dafuqs.paginatedadvancements.config.*;
import de.dafuqs.paginatedadvancements.frames.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.resource.*;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.util.*;

public class PaginatedAdvancementsClient implements ClientModInitializer {
	
	public static final Logger LOGGER = LoggerFactory.getLogger("PaginatedAdvancements");
	public static final String MOD_ID = "paginatedadvancements";
	
	public static ConfigManager<PaginatedAdvancementsConfig> CONFIG_MANAGER;
	public static PaginatedAdvancementsConfig CONFIG;
	
	@Contract(value = "_ -> new", pure = true)
	public static @NotNull Identifier locate(String name) {
		return new Identifier(MOD_ID, name);
	}
	
	@Override
	public void onInitializeClient() {
		ConfigHolder<PaginatedAdvancementsConfig> configHolder = AutoConfig.register(PaginatedAdvancementsConfig.class, JanksonConfigSerializer::new);
		CONFIG_MANAGER = ((ConfigManager<PaginatedAdvancementsConfig>) configHolder);
		CONFIG = AutoConfig.getConfigHolder(PaginatedAdvancementsConfig.class).getConfig();
		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(AdvancementFrameTypeDataLoader.INSTANCE);
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(AdvancementFrameDataLoader.INSTANCE);
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
