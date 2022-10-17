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
	private float yawLeft;
	private float yawRight;
	private float pitchUp;
	private float pitchDown;
	private float rollLeft;
	private float rollRight;

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

	public float getYawLeft() {
		return yawLeft;
	}

	public float getYawRight() {
		return yawRight;
	}

	public float getPitchUp() {
		return pitchUp;
	}

	public float getPitchDown() {
		return pitchDown;
	}

	public float getRollLeft() {
		return rollLeft;
	}

	public float getRollRight() {
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
		} else if (object instanceof Float) {
			return (Float) object;
		}
		return (Double) object;
	}

	private static float getFloat(Object object) {
		if (object instanceof Integer) {
			return (Integer) object;
		} else if (object instanceof Double) {
			return ((Double) object).floatValue();
		}
		return (Float) object;
	}

	public static MovementsConstraints deserialize(Map<String, Object> map) {
		MovementsConstraints constraints = new MovementsConstraints();
		constraints.forward = getDouble(map.getOrDefault("forward", 0.0));
		constraints.backward = getDouble(map.getOrDefault("backward", 0.0));
		constraints.left = getDouble(map.getOrDefault("left", 0.0));
		constraints.right = getDouble(map.getOrDefault("right", 0.0));
		constraints.up = getDouble(map.getOrDefault("up", 0.0));
		constraints.down = getDouble(map.getOrDefault("down", 0.0));
		constraints.yawLeft = getFloat(map.getOrDefault("yawLeft", 0.0));
		constraints.yawRight = getFloat(map.getOrDefault("yawRight", 0.0));
		constraints.pitchUp = getFloat(map.getOrDefault("pitchUp", 0.0));
		constraints.pitchDown = getFloat(map.getOrDefault("pitchDown", 0.0));
		constraints.rollLeft = getFloat(map.getOrDefault("rollLeft", 0.0));
		constraints.rollRight = getFloat(map.getOrDefault("rollRight", 0.0));
		return constraints;
	}

	public double getOnX(double sens) {
		if (sens == 0) return 0;
		if (sens > 0) return right;
		return left;
	}

	public double getOnY(double sens) {
		if (sens == 0) return 0;
		if (sens > 0) return up;
		return down;
	}

	public double getOnZ(double sens) {
		if (sens == 0) return 0;
		if (sens > 0) return forward;
		return backward;
	}

	public float getOnYaw(double sens) {
		if (sens == 0) return 0;
		if (sens > 0) return yawRight;
		return yawLeft;
	}

	public float getOnPitch(double sens) {
		if (sens == 0) return 0;
		if (sens > 0) return pitchUp;
		return pitchDown;
	}

	public float getOnRoll(double sens) {
		if (sens == 0) return 0;
		if (sens > 0) return rollRight;
		return rollLeft;
	}
}
