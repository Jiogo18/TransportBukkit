package fr.jarven.transportbukkit.templates;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.utils.MovementsVector;
import fr.jarven.transportbukkit.vehicles.VehiclePart;

public class AnimationTemplate {
	private AnimationType type;
	private ProportionnalType proportional;
	private double start;
	private double step;

	enum AnimationType {
		X,
		Y,
		Z,
		YAW,
		PITCH,
		ROLL
	}

	enum ProportionnalType {
		MOVE_ACCELERATION, // Faster when accelerating...
		MOVE_VELOCITY, // Faster when going faster...
		ROTATION_ACCELERATION, // Faster when accelerating...
		ROTATION_VELOCITY, // Faster when rotating faster...
		CONSTANT // Constant speed
	}

	protected static AnimationTemplate fromConfig(AnimationType type, ConfigurationSection config) {
		AnimationTemplate template = new AnimationTemplate();
		template.type = type;
		template.proportional = ProportionnalType.valueOf(config.getString("propotional", "CONSTANT"));
		template.start = config.getDouble("start", 0);
		template.step = config.getDouble("step", 1);
		return template;
	}

	protected static Set<AnimationTemplate> loadAnimations(ConfigurationSection config) {
		if (config == null) {
			return null;
		}
		Set<AnimationTemplate> animations = new HashSet<>();

		Set<String> animationKeys = config.getKeys(false);
		for (String animationKey : animationKeys) {
			try {
				ConfigurationSection animationSection = config.getConfigurationSection(animationKey);
				AnimationType animationType = AnimationType.valueOf(animationKey);
				AnimationTemplate animationTemplate = AnimationTemplate.fromConfig(animationType, animationSection);
				animations.add(animationTemplate);
			} catch (IllegalArgumentException e) {
				TransportPlugin.LOGGER.warning("Error loading animation template " + animationKey + " in vehicle part template");
				e.printStackTrace();
			}
		}

		return animations.isEmpty() ? null : animations;
	}

	private void applyAnimationSet(VehiclePart part, double value) {
		MovementsVector offsetAnimation = part.getOffsetAnimation();
		switch (type) {
			case X:
				offsetAnimation.setX(value);
				break;
			case Y:
				offsetAnimation.setY(value);
				break;
			case Z:
				offsetAnimation.setZ(value);
				break;
			case YAW:
				offsetAnimation.setYaw((float) value);
				break;
			case PITCH:
				offsetAnimation.setPitch((float) value);
				break;
			case ROLL:
				offsetAnimation.setRoll((float) value);
				break;
		}
	}

	private void applyAnimationAdd(VehiclePart part, double value) {
		MovementsVector offsetAnimation = part.getOffsetAnimation();
		switch (type) {
			case X:
				offsetAnimation.setX(value + offsetAnimation.getX());
				break;
			case Y:
				offsetAnimation.setY(value + offsetAnimation.getY());
				break;
			case Z:
				offsetAnimation.setZ(value + offsetAnimation.getZ());
				break;
			case YAW:
				offsetAnimation.setYaw((float) value + offsetAnimation.getYaw());
				break;
			case PITCH:
				offsetAnimation.setPitch((float) value + offsetAnimation.getPitch());
				break;
			case ROLL:
				offsetAnimation.setRoll((float) value + offsetAnimation.getRoll());
				break;
		}
	}

	public void reset(VehiclePart part) {
		applyAnimationSet(part, start);
	}

	public double apply(VehiclePart part) {
		double value;
		switch (proportional) {
			case MOVE_ACCELERATION:
				value = part.getVehicle().getAcceleration() * step;
				break;
			case MOVE_VELOCITY:
				value = part.getVehicle().getVelocity() * step;
				break;
			case ROTATION_ACCELERATION:
				value = part.getVehicle().getAllAcceleration().getRotationDistanceWithOrigin() * step;
				break;
			case ROTATION_VELOCITY:
				value = part.getVehicle().getAllVelocity().getRotationDistanceWithOrigin() * step;
				break;
			case CONSTANT:
				value = step;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + proportional);
		}
		if (value != 0) {
			applyAnimationAdd(part, value);
		}
		return value;
	}
}
