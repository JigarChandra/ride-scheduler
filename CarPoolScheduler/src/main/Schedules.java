package main;

import java.util.List;

public class Schedules {
	private int vehicleNumber;
	private List<Schedule> schedules;
	public Schedules(int vehicleNumber, List<Schedule> schedules) {
		this.vehicleNumber = vehicleNumber;
		this.schedules = schedules;
	}
	public void print() {
		System.out.println("Itinerary for Vehicle number " + vehicleNumber);
		for (int i = 0; i < schedules.size(); i++) {
			System.out.println("Schedule " + i);
			schedules.get(i).print();
		}
		System.out.println();
	}
}
