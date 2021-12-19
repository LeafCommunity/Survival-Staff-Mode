/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.events;

import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import org.bukkit.event.HandlerList;

public class StaffModeEnableEvent extends AbstractStaffModeToggleEvent
{
	public StaffModeEnableEvent(StaffMember member, SnapshotContext context)
	{
		super(member, context);
		
		if (context.mode() != Mode.STAFF)
		{
			throw new IllegalArgumentException("Mode must be " + Mode.STAFF);
		}
	}
	
	// - - - - - - HandlerList Boilerplate - - - - - -
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    public static HandlerList getHandlerList() { return HANDLERS; }
    
    @Override
    public HandlerList getHandlers() { return HANDLERS; }
}
