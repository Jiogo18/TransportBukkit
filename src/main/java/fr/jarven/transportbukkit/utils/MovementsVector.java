package fr.jarven.transportbukkit.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;

import fr.jarven.transportbukkit.TransportPlugin;

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

	private static double round(double value) {
		if (Math.abs(value) < 0.000001) {
			return 0;
		} else {
			return (float) value;
		}
	}

	private static float roundAngle(float value) {
		if (Math.abs(value) < 0.0001) {
			return 0;
		} else {
			return (float) value;
		}
	}

	public void applyMaximum(MovementsConstraints maxSpeed) {
		this.setX(applyMinMax(this.getX(), -maxSpeed.getRight(), maxSpeed.getLeft()));
		this.setY(applyMinMax(this.getY(), -maxSpeed.getDown(), maxSpeed.getUp()));
		this.setZ(applyMinMax(this.getZ(), -maxSpeed.getBackward(), maxSpeed.getForward()));
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

	public void setForwardBackward(double forwardBackward) {
		this.setX(forwardBackward);
	}

	public void setLeftRight(double leftRight) {
		this.setY(leftRight);
	}

	public void setUpDown(double upDown) {
		this.setZ(upDown);
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

	public double getDistanceWithOrigin() {
		return this.distance(new Vector());
	}

	public double getDistanceWithOriginSquared() {
		return this.distanceSquared(new Vector());
	}

	public float getRotationDistanceWithOrigin() {
		return (float) Math.sqrt(Math.pow(yaw, 2) + Math.pow(pitch, 2) + Math.pow(roll, 2));
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
		} else if (object == null) {
			return new MovementsVector();
		} else {
			TransportPlugin.LOGGER.warning("MovementsVector can't be created from " + object.getClass().getName());
			return new MovementsVector();
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

	public MovementsVector add(MovementsVector other) {
		super.add(other);
		this.yaw += other.yaw;
		this.pitch += other.pitch;
		this.roll += other.roll;
		return this;
	}

	public MovementsVector substract(MovementsVector other) {
		super.subtract(other);
		this.yaw -= other.yaw;
		this.pitch -= other.pitch;
		this.roll -= other.roll;
		return this;
	}

	public MovementsVector multiply(MovementsVector other) {
		super.multiply(other);
		this.yaw *= other.yaw;
		this.pitch *= other.pitch;
		this.roll *= other.roll;
		return this;
	}

	public MovementsVector divide(MovementsVector other) {
		super.divide(other);
		// if x is nan then
		if (getX() == Double.NaN) setX(0);
		if (getY() == Double.NaN) setY(0);
		if (getZ() == Double.NaN) setZ(0);
		if (this.yaw != 0) this.yaw /= other.yaw;
		if (this.pitch != 0) this.pitch /= other.pitch;
		if (this.roll != 0) this.roll /= other.roll;
		return this;
	}

	@Override
	public MovementsVector multiply(float number) {
		super.multiply(number);
		this.yaw *= number;
		this.pitch *= number;
		this.roll *= number;
		return this;
	}

	private double divideByNonZeroAbs(double value, double divider) {
		if (divider == 0) {
			return 0;
		} else {
			return Math.abs(value / divider);
		}
	}

	private double max(double... ds) {
		double max = 0;
		for (double d : ds) {
			max = Math.max(max, d);
		}
		return max;
	}

	private double maxAbs(double... ds) {
		double max = 0;
		for (double d : ds) {
			double abs = Math.abs(d);
			if (abs > max && abs != Double.POSITIVE_INFINITY) {
				max = abs;
			}
		}
		return max;
	}

	public double biggestFactor(MovementsConstraints constraints) {
		return max(
			divideByNonZeroAbs(this.getForwardBackward(), Math.min(Math.abs(constraints.getForward()), Math.abs(constraints.getBackward()))),
			divideByNonZeroAbs(this.getLeftRight(), Math.min(Math.abs(constraints.getRight()), Math.abs(constraints.getLeft()))),
			divideByNonZeroAbs(this.getUpDown(), Math.min(Math.abs(constraints.getUp()), Math.abs(constraints.getDown()))),
			divideByNonZeroAbs(this.getYaw(), Math.min(Math.abs(constraints.getYawLeft()), Math.abs(constraints.getYawRight()))),
			divideByNonZeroAbs(this.getPitch(), Math.min(Math.abs(constraints.getPitchUp()), Math.abs(constraints.getPitchDown()))),
			divideByNonZeroAbs(this.getRoll(), Math.min(Math.abs(constraints.getRollLeft()), Math.abs(constraints.getRollRight()))));
	}

	public void update(MovementsVector other) {
		this.setX(other.getX());
		this.setY(other.getY());
		this.setZ(other.getZ());
		this.setYaw(other.getYaw());
		this.setPitch(other.getPitch());
		this.setRoll(other.getRoll());
	}

	public MovementsVector abs() {
		this.setX(Math.abs(this.getX()));
		this.setY(Math.abs(this.getY()));
		this.setZ(Math.abs(this.getZ()));
		this.setYaw(Math.abs(this.getYaw()));
		this.setPitch(Math.abs(this.getPitch()));
		this.setRoll(Math.abs(this.getRoll()));
		return this;
	}

	public double getMaxAbs() {
		return maxAbs(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch(), this.getRoll());
	}

	public MovementsVector rotateAsAbsolute(LocationRollable rotation) {
		rotateAroundX(Math.toRadians(rotation.getPitch()));
		rotateAroundZ(Math.toRadians(rotation.getRoll()));
		rotateAroundY(-Math.toRadians(rotation.getYaw()));
		return this;
	}

	public MovementsVector rotateAsRelative(LocationRollable rotation) {
		rotateAroundY(Math.toRadians(rotation.getYaw()));
		rotateAroundZ(-Math.toRadians(rotation.getRoll()));
		rotateAroundX(-Math.toRadians(rotation.getPitch()));
		return this;
	}

	public MovementsVector rotateAsAbsolute(MovementsVector rotation) {
		rotateAroundX(Math.toRadians(rotation.getPitch()));
		rotateAroundZ(Math.toRadians(rotation.getRoll()));
		rotateAroundY(-Math.toRadians(rotation.getYaw()));
		return this;
	}

	public MovementsVector rotateAsRelative(MovementsVector rotation) {
		rotateAroundY(Math.toRadians(rotation.getYaw()));
		rotateAroundZ(-Math.toRadians(rotation.getRoll()));
		rotateAroundX(-Math.toRadians(rotation.getPitch()));
		return this;
	}

	public void round() {
		this.setX(round(this.getX()));
		this.setY(round(this.getY()));
		this.setZ(round(this.getZ()));
		this.setYaw(roundAngle(this.getYaw()));
		this.setPitch(roundAngle(this.getPitch()));
		this.setRoll(roundAngle(this.getRoll()));
	}
}
