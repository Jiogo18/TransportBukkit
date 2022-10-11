package fr.jarven.transportbukkit.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;

public class MovementsVector extends Vector {
	private float yaw;
	private float pitch;
	private float roll;

	public MovementsVector() {
		this(0, 0, 0, 0, 0, 0);
	}

	public MovementsVector(float forwardBackward, float leftRight, float upDown) {
		this(forwardBackward, leftRight, upDown, 0, 0, 0);
	}

	public MovementsVector(double forwardBackward, double leftRight, double upDown, float yaw, float pitch, float roll) {
		super(forwardBackward, leftRight, upDown);
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
	}

	public MovementsVector(MovementsVector offset) {
		super(offset.getX(), offset.getY(), offset.getZ());
		this.yaw = offset.yaw;
		this.pitch = offset.pitch;
		this.roll = offset.roll;
	}

	private double applyMinMax(double value, double min, double max) {
		if (value > max) {
			return max;
		} else if (value < min) {
			return min;
		} else {
			return value;
		}
	}

	public void applyMaximum(MovementsConstraints maxSpeed) {
		this.setX(applyMinMax(this.getX(), -maxSpeed.getBackward(), maxSpeed.getForward()));
		this.setY(applyMinMax(this.getY(), -maxSpeed.getLeft(), maxSpeed.getRight()));
		this.setZ(applyMinMax(this.getZ(), -maxSpeed.getDown(), maxSpeed.getUp()));
		yaw = (float) applyMinMax(yaw, -maxSpeed.getYawLeft(), maxSpeed.getYawRight());
		pitch = (float) applyMinMax(pitch, -maxSpeed.getPitchUp(), maxSpeed.getPitchDown());
		roll = (float) applyMinMax(roll, -maxSpeed.getRollLeft(), maxSpeed.getRollRight());
	}

	public double getForwardBackward() {
		return this.getX();
	}

	public double getLeftRight() {
		return this.getY();
	}

	public double getUpDown() {
		return this.getZ();
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public float getRoll() {
		return roll;
	}

	public double getDistanceWithOrigin() {
		return this.distance(new Vector());
	}

	private static double getDouble(Object object) {
		if (object instanceof Integer) {
			return (Integer) object;
		} else if (object instanceof Double) {
			return (Double) object;
		} else {
			return 0;
		}
	}

	public static MovementsVector fromConfig(Object object) {
		if (object instanceof ConfigurationSection) {
			ConfigurationSection section = (ConfigurationSection) object;
			double x = section.getDouble("x", 0);
			double y = section.getDouble("y", 0);
			double z = section.getDouble("z", 0);
			float relativeYaw = (float) section.getDouble("yaw", 0);
			float relativePitch = (float) section.getDouble("pitch", 0);
			float relativeRoll = (float) section.getDouble("roll", 0);
			return new MovementsVector(x, y, z, relativeYaw, relativePitch, relativeRoll);
		} else if (object instanceof LinkedHashMap) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) object;
			double x = getDouble(map.get("x"));
			double y = getDouble(map.get("y"));
			double z = getDouble(map.get("z"));
			float relativeYaw = (float) getDouble(map.get("yaw"));
			float relativePitch = (float) getDouble(map.get("pitch"));
			float relativeRoll = (float) getDouble(map.get("roll"));
			return new MovementsVector(x, y, z, relativeYaw, relativePitch, relativeRoll);
		} else {
			throw new IllegalArgumentException("MovementsVector can't be created from " + object.getClass().getName());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o instanceof MovementsVector) {
			MovementsVector other = (MovementsVector) o;
			return super.equals(other) && this.yaw == other.yaw && this.pitch == other.pitch && this.roll == other.roll;
		} else if (o instanceof Vector) {
			Vector other = (Vector) o;
			return super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();

		hash = 79 * hash + Float.floatToIntBits(this.yaw);
		hash = 79 * hash + Float.floatToIntBits(this.pitch);
		hash = 79 * hash + Float.floatToIntBits(this.roll);

		return hash;
	}
}
