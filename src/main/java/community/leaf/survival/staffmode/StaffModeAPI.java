/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import com.github.zafarkhaja.semver.Version;
import community.leaf.survival.staffmode.snapshots.SnapshotRegistry;

public interface StaffModeAPI
{
	Version version();
	
	SnapshotRegistry snapshots();
	
	StaffManager staff();
}
