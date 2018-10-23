package main;

import java.time.LocalTime;

public class Passenger {
	public final String name;
	public final int tripNumber;
	public final int x_origin;
	public final int y_origin;
	public final int x_destination;
	public final int y_destination;
	public final LocalTime earliestPickup;
	public final LocalTime latestDropOff;
	public Passenger(String name, int tripNumber, int x_origin, int y_origin, int x_destination, int y_destination,
			LocalTime earliestPickup, LocalTime latestDropOff) {
		this.name = name;
		this.tripNumber = tripNumber;
		this.x_origin = x_origin;
		this.y_origin = y_origin;
		this.x_destination = x_destination;
		this.y_destination = y_destination;
		this.earliestPickup = earliestPickup;
		this.latestDropOff = latestDropOff;
	}
}
