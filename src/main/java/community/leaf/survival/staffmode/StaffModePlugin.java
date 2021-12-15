/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import community.leaf.eventful.bukkit.BukkitEventSource;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class StaffModePlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource
{
	public static final int BSTATS = 0; // TODO: get bstats ID
	
	@Override
	public Plugin plugin() { return this; }
}
