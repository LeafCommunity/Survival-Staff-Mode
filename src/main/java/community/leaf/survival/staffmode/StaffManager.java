/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface StaffManager
{
	Stream<StaffMember> streamOnlineStaffMembers();
	
	Optional<StaffMember> member(UUID uuid);
	
	Optional<StaffMember> member(Player player);
	
	default StaffMember onlineStaffMember(Player player)
	{
		return member(player).orElseThrow(() ->
			new IllegalArgumentException("Player is not a staff member: " + player.getName())
		);
	}
}
