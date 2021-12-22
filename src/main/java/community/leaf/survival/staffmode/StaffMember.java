/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import community.leaf.survival.staffmode.snapshots.GameplaySnapshot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface StaffMember
{
	UUID uuid();
	
	default OfflinePlayer offlinePlayer() { return Bukkit.getOfflinePlayer(uuid()); }
	
	default Optional<Player> player() { return Optional.ofNullable(Bukkit.getPlayer(uuid())); }
	
	default Player online()
	{
		return player().orElseThrow(() -> new IllegalStateException("Player is not online."));
	}
	
	Optional<Instant> sinceLastToggle();
	
	Optional<Mode> lastToggledMode();
	
	Optional<GameplaySnapshot> capture();
	
	Optional<GameplaySnapshot> snapshot(Mode mode);
	
	Mode mode();
	
	ToggleSwitch mode(Mode mode);
}
