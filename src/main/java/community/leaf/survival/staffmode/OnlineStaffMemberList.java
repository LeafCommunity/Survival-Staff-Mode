/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class OnlineStaffMemberList
{
	private final Set<Player> onlineStaffMembers = new HashSet<>();
	
	private final StaffModePlugin plugin;
	
	OnlineStaffMemberList(StaffModePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	public void join(Player player) { onlineStaffMembers.add(player); }
	
	public void quit(Player player) { onlineStaffMembers.remove(player); }
	
	public void refresh()
	{
		onlineStaffMembers.clear();
		plugin.getServer().getOnlinePlayers().stream()
			.filter(Permissions.STAFF_MEMBER::allows)
			.forEach(onlineStaffMembers::add);
	}
	
	public Stream<Player> streamOnlineStaff()
	{
		return onlineStaffMembers.stream();
	}
}
