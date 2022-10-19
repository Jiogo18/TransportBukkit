package fr.jarven.transportbukkit.templates;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.MovementsVector;

public abstract class BasePartTemplate {
	protected MovementsVector offset;
	public static final String ENTITY_TAG = "TransportBukkit_Entity";

	private static final Method[] teleportMethods = ((Supplier<Method[]>) () -> {
		try {
			Method getHandle = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftEntity").getDeclaredMethod("getHandle");
			return new Method[] {
				getHandle, getHandle.getReturnType().getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class)};
		} catch (Exception ex) {
			return null;
		}
	}).get();

	public MovementsVector getOffset() {
		return offset;
	}

	public LocationRollable getLocationIfEntity(LocationRollable location, MovementsVector animationOffset, double neckHeigh) {
		// Change the offset depending on the rotation of the vehicle
		MovementsVector offsetRel = new MovementsVector(getOffset());
		offsetRel.setY(offsetRel.getY() + neckHeigh);
		offsetRel.rotateAsAbsolute(location);
		offsetRel.setY(offsetRel.getY() - neckHeigh);
		if (animationOffset != null) {
			location.setPitch(0); // Not ready yet
			location.add(animationOffset);
		}
		location.add(offsetRel);
		return location;
	}

	public LocationRollable getLocation(LocationRollable location) {
		// Change the offset depending on the rotation of the vehicle
		MovementsVector offsetRel = new MovementsVector(getOffset());
		offsetRel.rotateAsAbsolute(location);
		location.add(offsetRel);
		return location;
	}

	protected void update(BasePartTemplate other) {
		this.offset = other.offset;
	}

	public Entity spawnEntity(Location location, EntityType type) {
		Entity entity = location.getWorld().spawnEntity(location, type);
		entity.setInvulnerable(true);
		entity.setGravity(false);
		entity.addScoreboardTag("TransportBukkit_Entity");
		return entity;
	}

	public ArmorStand spawnArmorStand(Location location) {
		ArmorStand entity = (ArmorStand) spawnEntity(location, EntityType.ARMOR_STAND);
		entity.setVisible(false);
		entity.setBasePlate(false);
		return entity;
	}

	public void teleport(Entity entity, Location location) {
		try {
			teleportMethods[1].invoke(teleportMethods[0].invoke(entity), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		} catch (Exception ex) {
			entity.teleport(location);
		}
	}
}
