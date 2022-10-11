package fr.jarven.transportbukkit.utils;

import org.bukkit.Location;

import java.util.Map;

public class LocationRollable extends Location {
	private float roll;

	public LocationRollable(Location location) {
		this(location, 0);
	}

	public LocationRollable(Location location, float roll) {
		super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		this.roll = roll;
	}

	public LocationRollable(LocationRollable location) {
		super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		this.roll = location.getRoll();
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o instanceof LocationRollable) {
			LocationRollable other = (LocationRollable) o;
			return super.equals(other) && this.roll == other.roll;
		} else if (o instanceof Location) {
			Location other = (Location) o;
			return super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();

		hash = 79 * hash + Float.floatToIntBits(getPitch());
		hash = 79 * hash + Float.floatToIntBits(getYaw());
		hash = 79 * hash + Float.floatToIntBits(roll);

		return hash;
	}

	public LocationRollable add(MovementsVector vector) {
		super.add(vector.getX(), vector.getY(), vector.getZ());
		this.setYaw(this.getYaw() + vector.getYaw());
		this.setPitch(this.getPitch() + vector.getPitch());
		this.roll += vector.getRoll();
		return this;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = super.serialize();
		data.put("roll", this.roll);
		return data;
	}

	public static LocationRollable deserialize(Map<String, Object> args) {
		Location location = Location.deserialize(args);
		double roll = (double) args.getOrDefault("roll", 0.0);
		return new LocationRollable(location, (float) roll);
	}
}