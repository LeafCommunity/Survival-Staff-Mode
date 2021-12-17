package community.leaf.survival.staffmode;

import com.github.zafarkhaja.semver.Version;
import community.leaf.survival.staffmode.snapshots.SnapshotRegistry;

public interface StaffModeAPI
{
	Version version();
	
	SnapshotRegistry snapshots();
	
	StaffManager staff();
}
