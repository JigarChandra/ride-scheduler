package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SchedulerMain {
	public static void main(String[] args) throws IOException {
		InputStream stream = ClassLoader.getSystemResourceAsStream("file.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		List<Passenger> passengers = new ArrayList<>();
		try {
		    int passengerTrips = Integer.parseInt(br.readLine());

		    for (int i = 0; i < passengerTrips; i++) {
		        String line = br.readLine();
		        passengers.add(createPassenger(line));
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    br.close();
		}
		List<Schedules> schedules =ScheduleGenerator.getSchedules(passengers);
		schedules.stream().forEach(Schedules::print);
	}

	private static Passenger createPassenger(String line) {
		String[] info = line.split("\\t");
		String name = info[0];
		int tripNumber = Integer.parseInt(info[1]);
		String[] pickupTimeStr = info[2].split(":");
		String[] dropOffTimeStr = info[3].split(":");
		LocalTime pickUpTime = LocalTime.of(Integer.parseInt(pickupTimeStr[0]), Integer.parseInt(pickupTimeStr[1]));
		LocalTime dropOffTime = LocalTime.of(Integer.parseInt(dropOffTimeStr[0]), Integer.parseInt(dropOffTimeStr[1]));
		int x_origin = Integer.parseInt(info[4]);
		int x_destination = Integer.parseInt(info[6]);
		int y_origin = Integer.parseInt(info[5]);
		int y_destination = Integer.parseInt(info[7]);
		return new Passenger(name, tripNumber, x_origin, y_origin, x_destination, y_destination, pickUpTime, dropOffTime);
	}
}
