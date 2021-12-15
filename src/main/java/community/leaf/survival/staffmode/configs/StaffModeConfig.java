/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.configs;

import com.github.zafarkhaja.semver.Version;
import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import community.leaf.configvalues.bukkit.DefaultYamlValue;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.data.YamlDataFile;
import community.leaf.survival.staffmode.StaffModePlugin;
import community.leaf.survival.staffmode.util.Versions;

import java.util.List;

public class StaffModeConfig extends YamlDataFile
{
	public static final YamlValue<Version> VERSION =
		YamlValue.of("config-version", Versions.YAML).maybe();
	
	public static final DefaultYamlValue<Boolean> METRICS_ENABLED =
		YamlValue.ofBoolean("plugin.metrics").defaults(true);
	
	@AggregatedResult
	public static final List<YamlValue<?>> VALUES = Aggregates.list(StaffModeConfig.class, YamlValue.type());
	
	public StaffModeConfig(StaffModePlugin plugin)
	{
		super(plugin.directory(), "staff-mode.config.yml");
		
		reloadsWith(() ->
		{
			if (isInvalid()) { return; }
			
			Version existing = get(VERSION).orElse(Versions.ZERO);
			boolean isOutdated = existing.lessThan(plugin.version());
			
			if (isOutdated) { set(VERSION, plugin.version()); }
			
			headerFromResource("staff-mode.config.header.txt");
			defaultValues(VALUES);
			
			if (isUpdated()) { backupThenSave(plugin.backups(), "v" + existing); }
		});
	}
}
