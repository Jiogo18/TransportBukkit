package fr.jarven.transportbukkit.utils;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/**
 * Speed and Acceleration constraints for a vehicle
 */
public class MovementsConstraints implements ConfigurationSerializable {
	private double forward;
	private double backward;
	private double left;
	private double right;
	private double up;
	private double down;
	private double yawLeft;
	private double yawRight;
	private double pitchUp;
	private double pitchDown;
	private double rollLeft;
	private double rollRight;

	private MovementsConstraints() {
	}

	public void update(MovementsConstraints maxSpeed) {
		this.forward = maxSpeed.forward;
		this.backward = maxSpeed.backward;
		this.left = maxSpeed.left;
		this.right = maxSpeed.right;
		this.up = maxSpeed.up;
		this.down = maxSpeed.down;
		this.yawLeft = maxSpeed.yawLeft;
		this.yawRight = maxSpeed.yawRight;
		this.pitchUp = maxSpeed.pitchUp;
		this.pitchDown = maxSpeed.pitchDown;
		this.rollLeft = maxSpeed.rollLeft;
		this.rollRight = maxSpeed.rollRight;
	}

	public double getForward() {
		return forward;
	}

	public double getBackward() {
		return backward;
	}

	public double getLeft() {
		return left;
	}

	public double getRight() {
		return right;
	}

	public double getUp() {
		return up;
	}

	public double getDown() {
		return down;
	}

	public double getYawLeft() {
		return yawLeft;
	}

	public double getYawRight() {
		return yawRight;
	}

	public double getPitchUp() {
		return pitchUp;
	}

	public double getPitchDown() {
		return pitchDown;
	}

	public double getRollLeft() {
		return rollLeft;
	}

	public double getRollRight() {
		return rollRight;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new java.util.HashMap<>();
		map.put("forward", forward);
		map.put("backward", backward);
		map.put("left", left);
		map.put("right", right);
		map.put("up", up);
		map.put("down", down);
		map.put("tawLeft", yawLeft);
		map.put("tawRight", yawRight);
		map.put("pitchUp", pitchUp);
		map.put("pitchDown", pitchDown);
		map.put("rollLeft", rollLeft);
		map.put("rollRight", rollRight);
		return map;
	}

	private static double getDouble(Object object) {
		if (object instanceof Integer) {
			return (Integer) object;
		}
		return (Double) object;
	}

	public static MovementsConstraints deserialize(Map<String, Object> map) {
		MovementsConstraints constraints = new MovementsConstraints();
		constraints.forward = getDouble(map.getOrDefault("forward", 0.0));
		constraints.backward = getDouble(map.getOrDefault("backward", 0.0));
		constraints.left = getDouble(map.getOrDefault("left", 0.0));
		constraints.right = getDouble(map.getOrDefault("right", 0.0));
		constraints.up = getDouble(map.getOrDefault("up", 0.0));
		constraints.down = getDouble(map.getOrDefault("down", 0.0));
		constraints.yawLeft = getDouble(map.getOrDefault("yawLeft", 0.0));
		constraints.yawRight = getDouble(map.getOrDefault("yawRight", 0.0));
		constraints.pitchUp = getDouble(map.getOrDefault("pitchUp", 0.0));
		constraints.pitchDown = getDouble(map.getOrDefault("pitchDown", 0.0));
		constraints.rollLeft = getDouble(map.getOrDefault("rollLeft", 0.0));
		constraints.rollRight = getDouble(map.getOrDefault("rollRight", 0.0));
		return constraints;
	}
}
