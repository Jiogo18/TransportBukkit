package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.stream.Collectors;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.templates.VehicleTemplate;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Seat;
import fr.jarven.transportbukkit.vehicles.Vehicle;
import fr.jarven.transportbukkit.vehicles.VehiclePart;

public class CommandTransportInfo extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("info")
			.then(literal("vehicle_template").then(vehicleTemplateArgument("vehicle_template_name").executes(CommandTransportInfo::infoVehicleTemplate)))
			.then(literal("part_template").then(partTemplateArgument("part_template_name").executes(CommandTransportInfo::infoPartTemplate)))
			.then(literal("vehicle").then(vehicleArgument("vehicle_name").executes(CommandTransportInfo::infoVehicle)))
			.then(literal("part").then(vehicleArgument("vehicle_name").then(partArgument("part_name").executes(CommandTransportInfo::infoPart))))
			.then(literal("player").then(new PlayerArgument("player_name").executes(CommandTransportInfo::infoPlayer)));
	}

	private static int infoVehicleTemplate(CommandSender sender, Object[] args) {
		VehicleTemplate template = (VehicleTemplate) args[0];
		Resources.TEMPLATE_VEHICLE_INFO
			.replace("%template_vehicle%", template.getName())
			.replace("%maxspeed%", toReadbleString(template.getMaxSpeed()))
			.replace("%maxacceleration%", toReadbleString(template.getMaxAcceleration()))
			.replace("%seats%", String.valueOf(template.getSeats().size()))
			.replace("%parts%", String.join(", ", template.getParts().stream().map(PartTemplate::getName).collect(Collectors.toList())))
			.send(sender);
		return 1;
	}

	private static int infoPartTemplate(CommandSender sender, Object[] args) {
		PartTemplate template = (PartTemplate) args[0];
		Resources.TEMPLATE_PART_INFO
			.replace("%template_part%", template.getName())
			.replace("%offset%", toReadbleString(template.getOffset()))
			.replace("%type%", template.getType().name())
			.send(sender);
		return 1;
	}

	private static int infoVehicle(CommandSender sender, Object[] args) {
		Vehicle vehicle = (Vehicle) args[0];
		Resources.VEHICLE_INFO
			.replace("%vehicle%", vehicle.getName())
			.replace("%template%", vehicle.getTemplate().getName())
			.replace("%location%", toReadbleString(vehicle.getLocation()))
			.replace("%destination%", toReadbleString(vehicle.getDestination()))
			.replace("%speed%", String.valueOf(vehicle.getSpeed()))
			.replace("%fullspeed%", toReadbleString(vehicle.getAllSpeed()))
			.replace("%acceleration%", String.valueOf(vehicle.getAcceleration()))
			.replace("%fullacceleration%", toReadbleString(vehicle.getAllAcceleration()))
			.replace("%seats%", String.valueOf(vehicle.getSeats().size()))
			.replace("%passengers%", String.valueOf(vehicle.getPassengers().size()))
			.send(sender);
		return 1;
	}

	private static int infoPart(CommandSender sender, Object[] args) {
		VehiclePart part = (VehiclePart) args[1];

		Resources.PART_INFO
			.replace("%part%", part.getTemplate().getName())
			.replace("%vehicle%", part.getVehicle().getName())
			.replace("%location%", toReadbleString(part.getLocation()))
			.replace("%type%", part.getTemplate().getType().name())
			.send(sender);
		return 1;
	}

	private static int infoPlayer(CommandSender sender, Object[] args) {
		Player player = (Player) args[0];
		Optional<Seat> seat = TransportPlugin.getVehicleManager().getSeatByPassenger(player);
		String vehicleName = seat.map(Seat::getVehicle).map(Vehicle::getName).orElse("none");
		Resources.PLAYER_INFO
			.replace("%player%", player.getName())
			.replace("%location%", toReadbleString(player.getLocation()))
			.replace("%vehicle%", vehicleName)
			.send(sender);
		return 1;
	}
}
