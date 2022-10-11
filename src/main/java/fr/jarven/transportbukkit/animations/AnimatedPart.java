package fr.jarven.transportbukkit.animations;

import fr.jarven.transportbukkit.vehicles.VehiclePart;

public class AnimatedPart {
	final VehiclePart part;
	int tick = 0;

	public AnimatedPart(VehiclePart part) {
		this.part = part;
	}

	public void restartAnimation() {
		// Reset all modified properties, tp to default state...
		tick = 0;
	}

	protected void updateAnimation() {
		tick++;
	}

	protected void stopAnimation() {
		// TODO : stop animation
	}
}
