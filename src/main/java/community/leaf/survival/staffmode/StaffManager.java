/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface StaffManager
{
	Optional<StaffMember> member(UUID uuid);
	
	Optional<StaffMember> member(Player player);
	
	default Stream<StaffMember> streamOnlineStaffMembers()
	{
		return Bukkit.getOnlinePlayers().stream()
			.filter(Permissions.STAFF_MEMBER::allows)
			.flatMap(player -> member(player).stream());
	}
	
	default StaffMember onlineStaffMember(Player player)
	{
		return member(player).orElseThrow(() ->
			new IllegalArgumentException("Player is not a staff member: " + player.getName())
		);
	}
	
	default boolean isInStaffMode(Player player)
	{
		return Permissions.STAFF_MEMBER.allows(player) && onlineStaffMember(player).mode() == Mode.STAFF;
	}
}
