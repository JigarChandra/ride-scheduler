package main;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Vehicle {
	private int vehicleNumber;
	private List<Schedule> schedules = new ArrayList<>();
	private final int VEHICLE_EVENT_CAPACITY = 6; // max 3 passengers and 2 events for each passenger
	private LocalTime lastDropOffTime;
	private int latestX;
	private int latestY;
	
	public Vehicle(int vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
		latestX = 0;
		latestY = 0;
	}

	public boolean hasNoSchedule() {
		return schedules.isEmpty();
	}

	public boolean hasFullSchedule() {
		return isFull(schedules.get(schedules.size() - 1));
	}

	public Schedules getSchedules() {
		return new Schedules(vehicleNumber, schedules);
	}

	public int getLatestX() {
		return latestX;
	}

	public int getLatestY() {
		return latestY;
	}

	public void addEvent(Event e) {
		if (schedules.isEmpty()) {
			schedules.add(new Schedule(new ArrayList<Event>()));
		}
		else if (isFull(schedules.get(schedules.size() - 1))) {
			schedules.add(new Schedule(new ArrayList<Event>()));
		}
		schedules.get(schedules.size() - 1).add(e);
		lastDropOffTime = e.getActualTime();
		latestX = e.x;
		latestY = e.y;
	}

	public void addEvent(int idx, Event e) {
		if (isFull(schedules.get(schedules.size() - 1))) {
			schedules.add(new Schedule(new ArrayList<Event>()));
		}
		schedules.get(schedules.size() - 1).add(idx, e);
		if (idx == schedules.get(schedules.size() - 1).size() - 1) {
			lastDropOffTime = e.getActualTime();
			latestX = e.x;
			latestY = e.y;
		} else {
			Schedule latestSchedule = schedules.get(schedules.size() - 1);
			List<Event> latestScheduleEvents = latestSchedule.getEvents();
			lastDropOffTime = latestScheduleEvents.get(latestScheduleEvents.size() - 1).getActualTime();
		}
	}
	
	public boolean isFull(Schedule schedule) {
		return schedule.size() == VEHICLE_EVENT_CAPACITY;
	}

	public LocalTime getLastDropOffTime() {
		return lastDropOffTime;
	}

	public Schedule getLatestSchedule() {
		return schedules.get(schedules.size() - 1);
	}

	public Schedule getSchedule(int idx) {
		return schedules.get(idx);
	}
	
	public int getScheduleCount() {
		return schedules.size();
	}
}
