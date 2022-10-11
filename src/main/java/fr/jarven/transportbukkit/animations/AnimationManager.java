package fr.jarven.transportbukkit.animations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.jarven.transportbukkit.vehicles.VehiclePart;

public class AnimationManager {
	final Map<VehiclePart, AnimatedPart> parts = new HashMap<>();

	public Collection<AnimatedPart> getAnimatedParts() {
		return parts.values();
	}

	public Optional<AnimatedPart> getAnimatedPart(VehiclePart part) {
		return Optional.ofNullable(parts.get(part));
	}

	public boolean animateVehiclePart(VehiclePart part) {
		if (!part.getTemplate().isAnimated()) return false;

		return parts.computeIfAbsent(part, (vehiclePart) -> {
			AnimatedPart animatedPart = new AnimatedPart(part);
			parts.put(part, animatedPart);
			animatedPart.restartAnimation();
			return animatedPart;
		}) != null;
	}

	public boolean updateAnimation(VehiclePart part) {
		return parts.computeIfPresent(part, (vehiclePart, animatedPart) -> {
			animatedPart.updateAnimation();
			return animatedPart;
		}) != null;
	}

	public void reload() {
		// TODO: restart all animations
	}
}
