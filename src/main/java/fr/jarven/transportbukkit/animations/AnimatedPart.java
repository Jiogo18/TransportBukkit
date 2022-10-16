package fr.jarven.transportbukkit.animations;

import java.util.Set;

import fr.jarven.transportbukkit.templates.AnimationTemplate;
import fr.jarven.transportbukkit.vehicles.VehiclePart;

public class AnimatedPart {
	final VehiclePart part;

	public AnimatedPart(VehiclePart part) {
		this.part = part;
	}

	public void restartAnimation() {
		Set<AnimationTemplate> animations = part.getTemplate().getAnimations();
		if (animations == null) return;

		for (AnimationTemplate animation : animations) {
			animation.reset(part);
		}

		part.updateFakeLocation();
	}

	protected void updateAnimation() {
		Set<AnimationTemplate> animations = part.getTemplate().getAnimations();
		if (animations == null) return;

		boolean hasChanged = false;
		for (AnimationTemplate animation : animations) {
			if (animation.apply(part) != 0) {
				hasChanged = true;
			}
		}

		if (hasChanged) {
			part.updateFakeLocation();
		}
	}
}
