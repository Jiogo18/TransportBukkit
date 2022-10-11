package fr.jarven.transportbukkit.templates;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.MovementsVector;

public abstract class BasePartTemplate {
	protected MovementsVector offset;
	public static final String ENTITY_TAG = "TransportBukkit_Entity";

	public MovementsVector getOffset() {
		return offset;
	}

	public LocationRollable getLocationIfEntity(LocationRollable location, double neckHeigh) {
		// Change the offset depending on the rotation of the vehicle
		MovementsVector offsetRel = new MovementsVector(getOffset());
		offsetRel.setY(offsetRel.getY() + neckHeigh);
		offsetRel.rotateAroundX(Math.toRadians(location.getPitch()));
		offsetRel.rotateAroundZ(Math.toRadians(location.getRoll()));
		offsetRel.rotateAroundY(-Math.toRadians(location.getYaw()));
		offsetRel.setY(offsetRel.getY() - neckHeigh);
		location.add(offsetRel);
		return location;
	}

	public LocationRollable getLocation(LocationRollable location) {
		// Change the offset depending on the rotation of the vehicle
		MovementsVector offsetRel = new MovementsVector(getOffset());
		offsetRel.rotateAroundX(Math.toRadians(location.getPitch()));
		offsetRel.rotateAroundZ(Math.toRadians(location.getRoll()));
		offsetRel.rotateAroundY(-Math.toRadians(location.getYaw()));
		location.add(offsetRel);
		return location;
	}

	protected void update(BasePartTemplate other) {
		this.offset = other.offset;
	}

	public ArmorStand spawnArmorStand(Location location) {
		ArmorStand entity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		entity.setInvulnerable(true);
		entity.setGravity(false);
		entity.setVisible(false);
		entity.setBasePlate(false);
		entity.addScoreboardTag("TransportBukkit_Entity");
		return entity;
	}
}