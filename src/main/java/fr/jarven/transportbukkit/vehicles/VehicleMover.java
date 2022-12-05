package fr.jarven.transportbukkit.vehicles;

import fr.jarven.transportbukkit.tasks.MovementTask;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.MovementsConstraints;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class VehicleMover {
	private final Vehicle vehicle;
	private LocationRollable destination;
	private float velocityAtEnd; // 0 = stop, 1 = full speed

	public VehicleMover(Vehicle vehicle, LocationRollable destination, float velocityAtEnd) {
		this.vehicle = vehicle;
		this.destination = destination;
		this.velocityAtEnd = velocityAtEnd;
	}

	public void setDestination(LocationRollable destination, float velocityAtEnd) {
		this.destination = destination;
		this.velocityAtEnd = velocityAtEnd;
		calculateMovement();
	}

	public LocationRollable getDestination() {
		return destination;
	}

	private void calculateMovement() {
		// Here, vectors are in the direction of the vehicle (not X, Y, Z but right, up, forward)
		// We convert to X, Y, Z when adding the velocity to the location
		// To calculate the distanceToDestiation we have to reverse the conversion (make it relative to the vehicle)

		// Calculate the distance and the direction to the destination
		MovementsVector currentLocation = vehicle.getLocation().toVector();
		MovementsVector destinationLocation = destination.toVector();
		MovementsVector distanceToDestination = new MovementsVector(destinationLocation).substract(currentLocation);
		distanceToDestination.rotateAsRelative(currentLocation);
		MovementsVector directionToDestination = new MovementsVector(
			Math.signum(distanceToDestination.getX()),
			Math.signum(distanceToDestination.getY()),
			Math.signum(distanceToDestination.getZ()),
			Math.signum(distanceToDestination.getYaw()),
			Math.signum(distanceToDestination.getPitch()),
			Math.signum(distanceToDestination.getRoll()));

		// The acceleration gain in the direction of the destination
		final MovementsConstraints maxAcceleration = vehicle.getTemplate().getMaxAcceleration();
		MovementsVector maxAccelerationDir = new MovementsVector(
			maxAcceleration.getOnX(directionToDestination.getX()) * directionToDestination.getX(),
			maxAcceleration.getOnY(directionToDestination.getY()) * directionToDestination.getY(),
			maxAcceleration.getOnZ(directionToDestination.getZ()) * directionToDestination.getZ(),
			maxAcceleration.getOnYaw(directionToDestination.getYaw()) * directionToDestination.getYaw(),
			maxAcceleration.getOnPitch(directionToDestination.getPitch()) * directionToDestination.getPitch(),
			maxAcceleration.getOnRoll(directionToDestination.getRoll()) * directionToDestination.getRoll());
		MovementsVector maxDecelerationDir = new MovementsVector(
			-maxAcceleration.getOnX(-directionToDestination.getX()) * directionToDestination.getX(),
			-maxAcceleration.getOnY(-directionToDestination.getY()) * directionToDestination.getY(),
			-maxAcceleration.getOnZ(-directionToDestination.getZ()) * directionToDestination.getZ(),
			-maxAcceleration.getOnYaw(-directionToDestination.getYaw()) * directionToDestination.getYaw(),
			-maxAcceleration.getOnPitch(-directionToDestination.getPitch()) * directionToDestination.getPitch(),
			-maxAcceleration.getOnRoll(-directionToDestination.getRoll()) * directionToDestination.getRoll());

		// Speed at different points of the movement
		MovementsConstraints maxSpeedConstraints = vehicle.getTemplate().getMaxSpeed();
		MovementsVector currentSpeed = new MovementsVector(vehicle.getAllVelocity()).rotateAsRelative(currentLocation);
		MovementsVector maxSpeed = new MovementsVector(
			maxSpeedConstraints.getOnX(directionToDestination.getX()),
			maxSpeedConstraints.getOnY(directionToDestination.getY()),
			maxSpeedConstraints.getOnZ(directionToDestination.getZ()),
			maxSpeedConstraints.getOnYaw(directionToDestination.getYaw()),
			maxSpeedConstraints.getOnPitch(directionToDestination.getPitch()),
			maxSpeedConstraints.getOnRoll(directionToDestination.getRoll()));

		// Now calc on x, y, z...
		double aX = calculateAcceleration(currentSpeed.getX(), maxSpeed.getX(), maxAccelerationDir.getX(), maxDecelerationDir.getX(), distanceToDestination.getX());
		double aY = calculateAcceleration(currentSpeed.getY(), maxSpeed.getY(), maxAccelerationDir.getY(), maxDecelerationDir.getY(), distanceToDestination.getY());
		double aZ = calculateAcceleration(currentSpeed.getZ(), maxSpeed.getZ(), maxAccelerationDir.getZ(), maxDecelerationDir.getZ(), distanceToDestination.getZ());
		float aYaw = calculateAcceleration(currentSpeed.getYaw(), maxSpeed.getYaw(), maxAccelerationDir.getYaw(), maxDecelerationDir.getYaw(), distanceToDestination.getYaw());
		float aPitch = calculateAcceleration(currentSpeed.getPitch(), maxSpeed.getPitch(), maxAccelerationDir.getPitch(), maxDecelerationDir.getPitch(), distanceToDestination.getPitch());
		float aRoll = calculateAcceleration(currentSpeed.getRoll(), maxSpeed.getRoll(), maxAccelerationDir.getRoll(), maxDecelerationDir.getRoll(), distanceToDestination.getRoll());

		MovementsVector acceleration = vehicle.getAllAcceleration();
		acceleration.setX(aX);
		acceleration.setY(aY);
		acceleration.setZ(aZ);
		acceleration.setYaw(aYaw);
		acceleration.setPitch(aPitch);
		acceleration.setRoll(aRoll);
	}

	private double calculateAcceleration(double currentSpeed, double maxSpeed, double maxAcceleration, double maxDeceleration, double distanceToDestination) {
		double speedAtEnd = maxSpeed * velocityAtEnd;
		double ticksToDestinationWithCurrentSpeed = distanceToDestination / currentSpeed;
		double ticksToReachMaxSpeed = (maxSpeed - Math.abs(currentSpeed)) / Math.abs(maxAcceleration);
		double ticksToSlowDownAtEnd = (Math.abs(currentSpeed) - speedAtEnd) / Math.abs(maxDeceleration) * 3;

		double acceleration;
		if (Math.signum(currentSpeed) != Math.signum(maxAcceleration)) {
			acceleration = maxAcceleration; // Going in the wrong direction
		} else if (ticksToDestinationWithCurrentSpeed <= ticksToSlowDownAtEnd) {
			acceleration = maxDeceleration; // We have to slow down
		} else if (ticksToDestinationWithCurrentSpeed > ticksToReachMaxSpeed) {
			acceleration = maxAcceleration; // We can accelerate
		} else {
			return 0;
		}

		double distance = Math.abs(distanceToDestination);
		if (distance < 1) { // Slow down
			acceleration *= distance;
		}

		return acceleration;
	}

	private float calculateAcceleration(float currentSpeed, float maxSpeed, float maxAcceleration, float maxDeceleration, float distanceToDestination) {
		float speedAtEnd = maxSpeed * velocityAtEnd;
		double ticksToDestinationWithCurrentSpeed = distanceToDestination / currentSpeed;
		double ticksToReachMaxSpeed = (maxSpeed - Math.abs(currentSpeed)) / Math.abs(maxAcceleration);
		double ticksToSlowDownAtEnd = (Math.abs(currentSpeed) - speedAtEnd) / Math.abs(maxDeceleration) * 2;

		float acceleration;
		if (Math.signum(currentSpeed) != Math.signum(maxAcceleration)) {
			acceleration = maxAcceleration;
		} else if (ticksToDestinationWithCurrentSpeed <= ticksToSlowDownAtEnd) {
			acceleration = maxDeceleration;
		} else if (ticksToReachMaxSpeed < ticksToDestinationWithCurrentSpeed) {
			acceleration = maxAcceleration;
		} else {
			return 0;
		}

		float distance = Math.abs(distanceToDestination);
		if (distance < 5) { // Slow down
			acceleration *= distance * 0.2f;
		}

		return acceleration;
	}

	private void move() {
		MovementsVector acceleration = vehicle.getAllAcceleration();
		MovementsVector absoluteVelocity = vehicle.getAllVelocity();
		LocationRollable location = new LocationRollable(vehicle.getLocation());
		MovementsVector relativeVelocity = new MovementsVector(absoluteVelocity).rotateAsRelative(location);
		relativeVelocity.add(acceleration);
		relativeVelocity.applyMaximum(vehicle.getTemplate().getMaxSpeed());
		absoluteVelocity.update(relativeVelocity.rotateAsAbsolute(location));
		absoluteVelocity.round();
		location.add(absoluteVelocity);
		vehicle.teleport(location);
	}

	public void stopMovement() {
		MovementsVector acceleration = vehicle.getAllAcceleration();
		MovementsVector velocity = vehicle.getAllVelocity();
		acceleration.setX(0);
		acceleration.setY(0);
		acceleration.setZ(0);
		acceleration.setYaw(0);
		acceleration.setPitch(0);
		acceleration.setRoll(0);
		velocity.setX(0);
		velocity.setY(0);
		velocity.setZ(0);
		velocity.setYaw(0);
		velocity.setPitch(0);
		velocity.setRoll(0);
	}

	private boolean isInTheRightWorld() {
		LocationRollable currentLocation = vehicle.getLocation();
		return currentLocation != null && currentLocation.getWorld().equals(destination.getWorld());
	}

	private boolean isArrived() {
		LocationRollable currentLocation = vehicle.getLocation();
		if (currentLocation == null) return false;
		double velocity = vehicle.getVelocity();
		float rotVelocity = vehicle.getAllVelocity().getRotationDistanceWithOrigin();
		if (velocity > 0.01 || rotVelocity > 0.1f) return false;
		return currentLocation.almostEquals(destination, 0.1, 1f);
	}

	private boolean canMove() {
		return isInTheRightWorld() && !isArrived();
	}

	/**
	 * If the vehicle can move to the destination
	 * If the vehicle is in another world, can't move
	 * If the vehicle is already at the destination and doesn't move, don't move
	 */
	public static boolean haveToMove(Vehicle vehicle, LocationRollable destination) {
		if (vehicle == null || destination == null) return false;
		LocationRollable currentLocation = vehicle.getLocation();
		if (currentLocation == null || !currentLocation.getWorld().equals(destination.getWorld())) return false;
		return !currentLocation.almostEquals(destination, 0.01, 0.1f);
	}

	/**
	 * If the vehicle can move to the destination
	 * If the vehicle is in another world, can't move
	 * If the vehicle is already at the destination, can move (slow down, reverse and stop...)
	 */
	public static boolean canMoveTo(Vehicle vehicle, LocationRollable destination) {
		if (vehicle == null || destination == null) return false;
		LocationRollable currentLocation = vehicle.getLocation();
		return currentLocation != null && currentLocation.getWorld().equals(destination.getWorld());
	}

	public void tickMovement() {
		if (!canMove()) {
			MovementTask.stopMovements(vehicle);
			return;
		}

		calculateMovement();
		move();
	}
}
