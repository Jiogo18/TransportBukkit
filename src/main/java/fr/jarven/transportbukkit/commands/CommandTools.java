package fr.jarven.transportbukkit.commands;

import org.bukkit.Location;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.transportbukkit.commands.arguments.PartArgument;
import fr.jarven.transportbukkit.commands.arguments.PartTemplateArgument;
import fr.jarven.transportbukkit.commands.arguments.VehicleArgument;
import fr.jarven.transportbukkit.commands.arguments.VehicleTemplateArgument;
import fr.jarven.transportbukkit.utils.MovementsConstraints;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class CommandTools {
	protected CommandTools() {}

	public static LiteralArgument literal(String name) {
		return new LiteralArgument(name);
	}

	public static PartTemplateArgument partTemplateArgument(String name) {
		return new PartTemplateArgument(name);
	}

	public static VehicleTemplateArgument vehicleTemplateArgument(String name) {
		return new VehicleTemplateArgument(name);
	}

	public static VehicleArgument vehicleArgument(String name) {
		return new VehicleArgument(name);
	}

	public static PartArgument partArgument(String name) {
		return new PartArgument(name);
	}

	public static double round(double value, int decimals) {
		double factor = Math.pow(10, decimals);
		return Math.round(value * factor) / factor;
	}

	public static String toReadbleString(Location location) {
		if (location == null) return "null";
		return String.format("(%s ; %s ; %s) in %s",
			round(location.getX(), 3),
			round(location.getY(), 3),
			round(location.getZ(), 3),
			location.getWorld().getName());
	}

	public static String toReadbleString(MovementsVector vector) {
		if (vector == null) return "null";
		return String.format("(%s,%s,%s ; %s,%s,%s)",
			vector.getForwardBackward(),
			vector.getLeftRight(),
			vector.getUpDown(),
			vector.getYaw(),
			vector.getPitch(),
			vector.getRoll());
	}

	public static String toReadbleString(MovementsConstraints vector) {
		if (vector == null) return "null";
		return String.format("(%s/%s ; %s/%s ; %s/%s)",
			vector.getForward(),
			-vector.getBackward(),
			vector.getLeft(),
			-vector.getRight(),
			vector.getUp(),
			-vector.getDown());
	}
}
