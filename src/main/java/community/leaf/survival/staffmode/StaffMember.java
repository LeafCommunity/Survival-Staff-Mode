/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface StaffMember
{
	UUID uuid();
	
	default OfflinePlayer offlinePlayer() { return Bukkit.getOfflinePlayer(uuid()); }
	
	default Optional<Player> player() { return Optional.ofNullable(Bukkit.getPlayer(uuid())); }
	
	Mode mode();
	
	void mode(Mode mode);
}