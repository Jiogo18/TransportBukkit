package fr.jarven.transportbukkit.animations;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.vehicles.VehiclePart;

public class AnimationManager {
	final Map<VehiclePart, AnimatedPart> parts = new HashMap<>();
	private int animationDelay = 1;
	private BukkitTask task;

	public Collection<AnimatedPart> getAnimatedParts() {
		return parts.values();
	}

	public Optional<AnimatedPart> getAnimatedPart(VehiclePart part) {
		return Optional.ofNullable(parts.get(part));
	}

	public boolean animateVehiclePart(VehiclePart part) {
		if (!part.getTemplate().isAnimated()) return false;

		return parts.computeIfAbsent(part, vehiclePart -> {
			AnimatedPart animatedPart = new AnimatedPart(part);
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
		for (AnimatedPart animatedPart : parts.values()) {
			animatedPart.restartAnimation();
		}
		startTask();
	}

	public void onDisable() {
		for (AnimatedPart animatedPart : parts.values()) {
			animatedPart.restartAnimation();
		}
		stopTask();
	}

	private void startTask() {
		if (task == null || task.isCancelled()) {
			task = Bukkit.getScheduler().runTaskTimer(TransportPlugin.getInstance(), this::onTick, animationDelay, animationDelay);
		}
	}

	private void stopTask() {
		if (task != null && !task.isCancelled()) {
			task.cancel();
		}
		task = null;
	}

	private void onTick() {
		for (AnimatedPart animatedPart : parts.values()) {
			animatedPart.updateAnimation();
		}
	}
}
