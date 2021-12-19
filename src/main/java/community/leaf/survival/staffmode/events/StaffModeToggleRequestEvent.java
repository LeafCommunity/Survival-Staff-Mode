/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.events;

import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class StaffModeToggleRequestEvent extends AbstractStaffModeToggleEvent implements Cancellable
{
	public StaffModeToggleRequestEvent(StaffMember member, SnapshotContext context)
	{
		super(member, context);
	}
	
	// - - - - - - Cancellable Boilerplate - - - - - -
    
    private boolean cancelled = false;
    
    @Override
    public boolean isCancelled() { return cancelled; }
    
    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    
    // - - - - - - HandlerList Boilerplate - - - - - -
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    public static HandlerList getHandlerList() { return HANDLERS; }
    
    @Override
    public HandlerList getHandlers() { return HANDLERS; }
}
