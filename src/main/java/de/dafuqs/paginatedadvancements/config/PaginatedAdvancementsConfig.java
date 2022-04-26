package de.dafuqs.paginatedadvancements.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

@Config(name = "PaginatedAdvancements")
public class PaginatedAdvancementsConfig implements ConfigData {
	
	@Comment("Fade the background slightly gray when hovering over an advancement")
	public boolean FadeOutBackgroundOnAdvancementHover = true;
	
	@Comment("List of pinned Tabs (root advancement identifier)")
	public List<String> PinnedTabs = new ArrayList<>();
	
	@Comment("Save and restore the last selected tab")
	public boolean SaveLastSelectedTab = true;
	
	@Comment("The identifier of the last opened tab")
	public String LastSelectedTab = "";
	
	@Comment("Spacing between horizontal tabs (vanilla default: 4)")
	public int SpacingBetweenHorizontalTabs = 4;
	
	@Comment("Spacing between pinned tabs")
	public int SpacingBetweenPinnedTabs = 2;
	
	@Override
	public void validatePostLoad() {

	}
	
}
