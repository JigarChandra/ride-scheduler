package main;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleGenerator {
	public static List<Schedules> getSchedules(List<Passenger> passengers) {
		List<Vehicle> vehicles = new ArrayList<>();
		vehicles.add(new Vehicle(0));
		sortPassengersBasedOnEarliestPickupTime(passengers);
		int currVehicleNumber = 0;

		while (! passengers.isEmpty()) {
			Vehicle currVehicle = vehicles.get(currVehicleNumber); 
			if (currVehicle.hasNoSchedule()) {
				Passenger p = passengers.get(0);
				Event pickup = new Event(p.tripNumber, p.name, "pickup",
						p.x_origin, p.y_origin, p.earliestPickup, p.earliestPickup, 0, Integer.MAX_VALUE);
				
				int timeToReach = DistanceUtil.calculateTravelTime(p.x_origin, p.y_origin, p.x_destination, p.y_destination);
				LocalTime actualDropOff = p.earliestPickup.plusMinutes(timeToReach);
				int maxDelay = (int) Duration.between(actualDropOff, p.latestDropOff).toMinutes();
				Event dropoff = new Event(p.tripNumber, p.name, "dropoff",
						p.x_destination, p.y_destination, actualDropOff, p.latestDropOff, timeToReach, maxDelay);
				currVehicle.addEvent(pickup);
				currVehicle.addEvent(dropoff);
				passengers.remove(0);
			} else if (currVehicle.hasFullSchedule()) {
				List<Event> optimalTrips = findMostOptimalTripForNewSchedule(currVehicle.getLastDropOffTime(),
						currVehicle.getLatestX(), currVehicle.getLatestY(), passengers);
				if (optimalTrips != null) {
					currVehicle.addEvent(optimalTrips.get(0)); // pickup
					currVehicle.addEvent(optimalTrips.get(1)); // dropoff
				} else {
					if (++currVehicleNumber == vehicles.size()) {
						vehicles.add(new Vehicle(currVehicleNumber));
						currVehicleNumber = 0;
					}
				}
			} else {
				ScheduleResult result = insertMostOptimalTripForCurrentSchedule(passengers.get(0), vehicles, currVehicleNumber);
				if (result != null && result.wasSuccessful) {
					vehicles.get(currVehicleNumber).addEvent(result.pickupInsertionIdx, result.pickup);
					vehicles.get(currVehicleNumber).addEvent(result.dropoffInsertionIdx, result.dropoff);
					passengers.remove(0);	
				} else {
					if (++currVehicleNumber == vehicles.size()) {
						vehicles.add(new Vehicle(currVehicleNumber));
						currVehicleNumber = 0;
					}
				}
			}
		}
		removeUnusedVehicles(vehicles);
		return vehicles.stream().map(Vehicle::getSchedules).collect(Collectors.toList());
	}

	private static void sortPassengersBasedOnEarliestPickupTime(List<Passenger> passengers) {
		Collections.sort(passengers, new Comparator<Passenger>()
				{
					public int compare(Passenger p1, Passenger p2) {
						return p1.earliestPickup.compareTo(p2.earliestPickup);
					}
		});
	}

	private static List<Event> findMostOptimalTripForNewSchedule(LocalTime vehicleStartTime, int vehicleX, int vehicleY, List<Passenger> passengers) {
		int currOptimalPassengerIdx = -1;
		int currBestBufferTime = -1;
		LocalTime potentialPickupTime = null;
		LocalTime potentialDropOffTime = null;
		int timeFromPreviousStop = -1;
		
		for (int i = 0; i < passengers.size(); i++) {
			Passenger p = passengers.get(i);
			int travelTimeToOrigin = DistanceUtil.calculateTravelTime(vehicleX, vehicleY, p.x_origin, p.y_origin);
			LocalTime startTime = vehicleStartTime.plusMinutes(travelTimeToOrigin);
			int travelTimeToDestination = DistanceUtil.calculateTravelTime(p.x_origin, p.y_origin, p.x_destination, p.y_destination);
			LocalTime latestPickupTime = p.latestDropOff.minusMinutes(travelTimeToDestination); 
			if (!latestPickupTime.isBefore(startTime)) {
				int diffInMinutes = LocalTimeUtil.minus(latestPickupTime, startTime);
				if (startTime.isBefore(p.earliestPickup)) {
					currOptimalPassengerIdx = i;
					currBestBufferTime = diffInMinutes;
					potentialPickupTime = p.earliestPickup;
					potentialDropOffTime = potentialPickupTime.plusMinutes(travelTimeToDestination);
					timeFromPreviousStop = travelTimeToDestination;
					break;
				}
				if (diffInMinutes > currBestBufferTime) {
					currOptimalPassengerIdx = i;
					currBestBufferTime = diffInMinutes;
					potentialPickupTime = startTime;
					potentialDropOffTime = potentialDropOffTime.plusMinutes(travelTimeToDestination);
					timeFromPreviousStop = travelTimeToDestination;
				}
			}
		}
		if (currOptimalPassengerIdx != -1) {
			Passenger p = passengers.get(currOptimalPassengerIdx);
			Event pickup = new Event(p.tripNumber, p.name, "pickup",
					p.x_origin, p.y_origin, potentialPickupTime, p.earliestPickup, 0, Integer.MAX_VALUE);
			int maxDelay = LocalTimeUtil.minus(p.latestDropOff, potentialDropOffTime);
			Event dropoff = new Event(p.tripNumber, p.name, "dropoff",
					p.x_destination, p.y_destination, potentialDropOffTime,
					p.latestDropOff, timeFromPreviousStop, maxDelay);
			passengers.remove(p);
			return Arrays.asList(pickup, dropoff);
		}
		return null;
	}
	
	private static ScheduleResult insertMostOptimalTripForCurrentSchedule(Passenger passenger, List<Vehicle> vehicles, int currVehicleNumber) {
		return Scheduler.tryInsertInCurrentVehicleSchedule(vehicles.get(currVehicleNumber), passenger);
	}

	private static void removeUnusedVehicles(List<Vehicle> vehicles) {
		for (int i = vehicles.size() -1; i >= 0; i--) {
			if (vehicles.get(i).hasNoSchedule()) {
				vehicles.remove(i);
			} else {
				break;
			}
		}
	}
}
