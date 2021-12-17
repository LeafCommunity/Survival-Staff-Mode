/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import com.rezzedup.util.valuables.Adapter;

public enum Mode
{
	SURVIVAL,
	STAFF;
	
	private final Mode opposite;
	
	Mode()
	{
		Mode[] values = values();
		this.opposite = values[(ordinal() + 1) % values.length];
	}
	
	public Mode toggle() { return opposite; }
	
	private static final Adapter<String, Mode> ADAPTER = Adapter.ofString().intoEnum(Mode.class);
	
	public static Adapter<String, Mode> adapter() { return ADAPTER; }
}
