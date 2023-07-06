package fr.jarven.transportbukkit.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.BasePartTemplate;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Seat;
import fr.jarven.transportbukkit.vehicles.Vehicle;
import net.md_5.bungee.api.ChatMessageType;

public class PlayerListener implements Listener {
	private Map<UUID, BukkitTask> cancelExitTasks = new HashMap<>();

	public boolean isVehiclePartEntity(Entity entity) {
		return entity.getScoreboardTags().contains(BasePartTemplate.ENTITY_TAG);
	}

	public Optional<Seat> getSeatByPassenger(Entity entity) {
		return TransportPlugin.getVehicleManager().getSeatByPassenger(entity);
	}

	public Optional<Seat> getSeatFromEntity(Vehicle vehicle, Entity entity) {
		return vehicle.getSeats().stream().filter(seat -> entity.getUniqueId().equals(seat.getEntityUUID())).findFirst();
	}

	public Optional<Vehicle> getVehicleFromEntity(Entity entity) {
		if (!isVehiclePartEntity(entity)) {
			return Optional.empty();
		}
		Optional<Vehicle> vehicle = TransportPlugin.getVehicleManager().getVehicleByEntity(entity);
		if (vehicle.isPresent()) {
			return Optional.of(vehicle.get());
		}
		return Optional.empty();
	}

	public void sendVehicleLockedMessage(Entity entity, Vehicle vehicle) {
		if (entity instanceof Player) {
			Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.getName()).send((Player) entity, ChatMessageType.ACTION_BAR);
		}
	}

	public void cancelLeaveEvent(Entity entity, Seat seat) {
		sendVehicleLockedMessage(entity, seat.getVehicle());
		if (cancelExitTasks.containsKey(entity.getUniqueId())) {
			return;
		}
		cancelExitTasks.put(entity.getUniqueId(), Bukkit.getScheduler().runTaskLater(TransportPlugin.getInstance(), () -> {
			seat.addPassenger(entity);
			Bukkit.getScheduler().scheduleSyncDelayedTask(TransportPlugin.getInstance(), () -> sendVehicleLockedMessage(entity, seat.getVehicle()), 1L);
			cancelExitTasks.remove(entity.getUniqueId());
		}, 4L));
	}

	public void cancelLeaveEventIfLocked(Cancellable event, Entity entity, boolean cancel) {
		if (event.isCancelled()) return;
		// Use the list of players instead of the vehicle's passengers because of --force-lock
		Seat seat = TransportPlugin.getVehicleManager().getSeatByPassengerPlayer().getOrDefault(entity.getUniqueId(), null);
		if (seat != null) {
			if (seat.getVehicle().isLocked()) {
				cancelLeaveEvent(entity, seat);
				event.setCancelled(cancel);
			} else {
				seat.removePassenger(entity);
			}
		}
	}

	public void enterVehicle(Vehicle vehicle, Player player, Optional<Seat> seatClicked) {
		Optional<Seat> currentSeat = getSeatByPassenger(player);

		// If the player is not in a seat
		// Or he has clicked on a seat
		if (!currentSeat.isPresent() || seatClicked.isPresent()) {
			if (vehicle.isLocked()) {
				sendVehicleLockedMessage(player, vehicle);
				return;
			}
			if (vehicle.isFull()) {
				Resources.VEHICLE_FULL.replace("%vehicle%", vehicle.getName()).send(player, ChatMessageType.ACTION_BAR);
				return;
			}

			if (seatClicked.isPresent()) {
				vehicle.addPassenger(player, seatClicked.get());
			} else {
				vehicle.addPassenger(player);
			}
		}
	}

	public double getDistanceSquaredIfInFront(Location eyeLocation, Entity target) {
		Vector direction = eyeLocation.getDirection();
		RayTraceResult res = target.getBoundingBox().rayTrace(eyeLocation.toVector(), direction, 6.0D);
		if (res == null) {
			return -1;
		}
		return res.getHitPosition().distanceSquared(eyeLocation.toVector());
	}

	/**
	 * Get the entity where the player is looking at
	 */
	public Optional<Seat> getNearestSeatInFrontOfPlayer(Vehicle vehicle, Player player) {
		Seat nearestSeat = null;
		double nearestDistance = Double.MAX_VALUE;

		for (Entity target : player.getNearbyEntities(6.0D, 6.0D, 6.0D)) {
			if (target instanceof ArmorStand) {
				double distance = getDistanceSquaredIfInFront(player.getEyeLocation(), target);
				if (distance != -1) {
					Optional<Seat> seat = getSeatFromEntity(vehicle, target);
					if (seat.isPresent()) {
						if (distance < nearestDistance) {
							nearestSeat = seat.get();
							nearestDistance = distance;
						}
					}
				}
			}
		}
		return Optional.ofNullable(nearestSeat);
	}

	public void enterVehicle(Vehicle vehicle, Player player, Entity entityClicked) {
		Optional<Seat> seatClicked = getSeatFromEntity(vehicle, entityClicked);
		if (!seatClicked.isPresent()) {
			seatClicked = getNearestSeatInFrontOfPlayer(vehicle, player);
		}
		enterVehicle(vehicle, player, seatClicked);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return; // No off-hand
		Entity entityClicked = event.getRightClicked();
		Optional<Vehicle> vehicle = getVehicleFromEntity(event.getRightClicked());
		Player player = event.getPlayer();
		if (vehicle.isPresent()) {
			event.setCancelled(true); // Prevent from taking the item
			enterVehicle(vehicle.get(), player, entityClicked);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getLocation(), 0.5, 0.5, 0.5);
			for (Entity entity : entities) {
				Optional<Vehicle> vehicle = getVehicleFromEntity(entity);
				if (vehicle.isPresent()) {
					event.setCancelled(true);
					enterVehicle(vehicle.get(), event.getPlayer(), entity);
					break;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerLeaveVehicle(EntityDismountEvent event) {
		cancelLeaveEventIfLocked(event, event.getDismounted(), true);
	}

	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		cancelLeaveEventIfLocked(event, event.getPlayer(), false);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		TransportPlugin.getVehicleManager().onPlayerJoin(player);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (TransportPlugin.getVehicleManager().getPlayersPassengers().contains(player.getUniqueId())) {
			cancelLeaveEventIfLocked(event, player, false);
		}
	}

	@EventHandler
	public void onEntityHurt(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (isVehiclePartEntity(entity)) {
			event.setCancelled(true);
		}
	}
}
