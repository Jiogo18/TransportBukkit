package fr.jarven.transportbukkit.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.Collection;
import java.util.Optional;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.BasePartTemplate;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Seat;
import fr.jarven.transportbukkit.vehicles.Vehicle;
import net.md_5.bungee.api.ChatMessageType;

public class PlayerListener implements Listener {
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

	public void enterVehicle(Vehicle vehicle, Player player, Optional<Seat> seatClicked) {
		Optional<Seat> currentSeat = getSeatByPassenger(player);

		// If the player is not in a seat
		// Or he has clicked on a seat
		if (!currentSeat.isPresent() || seatClicked.isPresent()) {
			if (vehicle.isLocked()) {
				Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.getName()).send(player, ChatMessageType.ACTION_BAR);
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

	public void enterVehicle(Vehicle vehicle, Player player, Entity entityClicked) {
		Optional<Seat> seatClicked = getSeatFromEntity(vehicle, entityClicked);
		enterVehicle(vehicle, player, seatClicked);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
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
	public void onPlayerLeaveVehicle(VehicleExitEvent event) {
		Optional<Vehicle> vehicle = getVehicleFromEntity(event.getVehicle());
		if (vehicle.isPresent()) {
			if (vehicle.get().isLocked()) {
				event.setCancelled(true);
				if (event.getExited() instanceof Player) {
					Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.get().getName()).send((Player) event.getExited(), ChatMessageType.ACTION_BAR);
				}
			}
		}
	}
}
