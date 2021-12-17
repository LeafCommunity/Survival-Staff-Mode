/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.events;

import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Objects;

public abstract class AbstractStaffModeToggleEvent extends Event
{
	private final StaffMember member;
	private final SnapshotContext context;
	
	public AbstractStaffModeToggleEvent(StaffMember member, SnapshotContext context)
	{
		this.member = Objects.requireNonNull(member, "member");
		this.context = Objects.requireNonNull(context, "context");
		
		if (!member.uuid().equals(context.player().getUniqueId()))
		{
			throw new IllegalArgumentException("Context doesn't match staff member (player has different UUID)");
		}
	}
	
	public StaffMember member() { return member; }
	
	public SnapshotContext context() { return context; }
	
	public Player player() { return context.player(); }
	
	public Mode mode() { return context.mode(); }
}
