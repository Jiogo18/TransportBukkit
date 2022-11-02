package fr.jarven.transportbukkit.templates;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class SeatProperties extends BasePartTemplate implements ConfigurationSerializable {
	private int seatIndex;
	private SeatType type;

	enum SeatType {
		DRIVER,
		PASSENGER,
		OTHER
	}

	private SeatProperties() {
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("offset", offset);
		map.put("type", type);
		return map;
	}

	public static SeatProperties deserialize(Map<String, Object> map) {
		SeatProperties seatProperties = new SeatProperties();
		seatProperties.offset = MovementsVector.fromConfig(map.get("offset"));
		seatProperties.type = SeatType.valueOf((String) map.getOrDefault("type", "OTHER"));
		return seatProperties;
	}

	public static SeatProperties fromConfig(Object object) {
		if (object instanceof SeatProperties) {
			return (SeatProperties) object;
		} else if (object instanceof LinkedHashMap) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) object;
			return SeatProperties.deserialize(map);
		} else {
			throw new IllegalArgumentException("Invalid seat template");
		}
	}

	public void update(SeatProperties template) {
		this.seatIndex = template.seatIndex;
		this.offset = template.offset;
		this.type = template.type;
	}

	protected void setSeatIndex(int seatIndex) {
		this.seatIndex = seatIndex;
	}

	public int getSeatIndex() {
		return seatIndex;
	}

	public Entity spawnEntity(LocationRollable location, String vehicleName) {
		ArmorStand entity = super.spawnArmorStand(location, vehicleName);
		entity.setSmall(true);
		entity.addScoreboardTag("TransportBukkit_Seat");
		entity.addScoreboardTag("Seat_" + seatIndex);
		entity.setCustomName(vehicleName + " Seat " + seatIndex);
		return entity;
	}
}
