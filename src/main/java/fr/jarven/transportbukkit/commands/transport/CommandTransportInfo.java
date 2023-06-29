package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
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
			.then(literal("vehicle_template").then(vehicleTemplateArgument().executes(CommandTransportInfo::infoVehicleTemplate)))
			.then(literal("part_template").then(partTemplateArgument().executes(CommandTransportInfo::infoPartTemplate)))
			.then(literal("vehicle").then(vehicleArgument().executes(CommandTransportInfo::infoVehicle)))
			.then(literal("part").then(vehicleArgument().then(vehiclePartArgument().executes(CommandTransportInfo::infoPart))))
			.then(literal("player").then(new PlayerArgument("player_name").executes(CommandTransportInfo::infoPlayer)));
	}

	private static int infoVehicleTemplate(CommandSender sender, CommandArguments args) {
		VehicleTemplate template = getVehicleTemplate(args);
		Resources.TEMPLATE_VEHICLE_INFO
			.replace("%template_vehicle%", template.getName())
			.replace("%maxspeed%", toReadbleString(template.getMaxSpeed()))
			.replace("%maxacceleration%", toReadbleString(template.getMaxAcceleration()))
			.replace("%seats%", String.valueOf(template.getSeats().size()))
			.replace("%parts%", String.join(", ", template.getParts().stream().map(PartTemplate::getName).collect(Collectors.toList())))
			.send(sender);
		return 1;
	}

	private static int infoPartTemplate(CommandSender sender, CommandArguments args) {
		PartTemplate template = getPartTemplate(args);
		Resources.TEMPLATE_PART_INFO
			.replace("%template_part%", template.getName())
			.replace("%offset%", toReadbleString(template.getOffset()))
			.replace("%type%", template.getType().name())
			.send(sender);
		return 1;
	}

	private static int infoVehicle(CommandSender sender, CommandArguments args) {
		Vehicle vehicle = getVehicle(args);
		Resources.VEHICLE_INFO
			.replace("%vehicle%", vehicle.getName())
			.replace("%template%", vehicle.getTemplate().getName())
			.replace("%location%", toReadbleString(vehicle.getLocation()))
			.replace("%destination%", toReadbleString(vehicle.getDestination()))
			.replace("%speed%", toReadbleString(vehicle.getVelocity()))
			.replace("%fullspeed%", toReadbleString(vehicle.getAllVelocity()))
			.replace("%acceleration%", toReadbleString(vehicle.getAcceleration()))
			.replace("%fullacceleration%", toReadbleString(vehicle.getAllAcceleration()))
			.replace("%seats%", String.valueOf(vehicle.getSeats().size()))
			.replace("%passengers%", String.valueOf(vehicle.getPassengers().size()))
			.replace("%last_saved%", toReadbleString(new Date(vehicle.getSaveTimestamp()), sender))
			.send(sender);
		return 1;
	}

	private static int infoPart(CommandSender sender, CommandArguments args) {
		VehiclePart part = getVehiclePart(args);

		Resources.PART_INFO
			.replace("%part%", part.getTemplate().getName())
			.replace("%vehicle%", part.getVehicle().getName())
			.replace("%location%", toReadbleString(part.getLocation()))
			.replace("%type%", part.getTemplate().getType().name())
			.send(sender);
		return 1;
	}

	private static int infoPlayer(CommandSender sender, CommandArguments args) {
		Player player = (Player) args.getOptional("player_name").orElseThrow(NullPointerException::new);
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
