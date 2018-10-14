package nl.knokko.golems;

import org.bukkit.Location;
import org.bukkit.entity.Golem;

public class GolemFreeTask extends GolemTask {

	public GolemFreeTask() {
		super(GolemPriority.NONE, GolemState.FREE);
	}

	@Override
	public Location getTaskLocation(Golem golem) {
		return null;
	}

	@Override
	public GolemAction getNextAction(Golem golem) {
		return null;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof GolemFreeTask;
	}

	@Override
	public String getSaveString() {
		return "free";
	}

}
