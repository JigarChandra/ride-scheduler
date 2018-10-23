## Goal
With an unprecedented progress in the Ride-sharing industry by the likes of Uber and Lyft, this project
takes a step back and attempts to build a ride scheduler for the following simple scenario:

1. A car company has a detailed schedule of potential customer trips with info such as time after which he/she wants to depart,
max. arrival time, pickup and drop-off co-ordinates.
2. A car can have at max 3 passengers at anytime.
3. Traveling has been simplified to be in a linear motion with a constant speed of 60 kmph, and 5 distance units = 1 km.

The objective is to use minimum number of vehicles to service all ride requests and also produce an itinerary of all vehicles.

## Entry point
	SchedulerMain.java
## Main class and method
	ScheduleGenerator.java -> getSchedules()

## Algorithm

1. Sort passenger trips based on earliest pick up.
2. Initialize a vehicleList with one vehicle and a pointer currentVehicleNumber to 0.
3. While passengerTripList is not empty:
i. If current vehicle has no schedules:
a. Add events corresponding to first passenger trip from sorted list to current vehicle’s
schedule and remove passenger trip from passengerTripList.

ii. Else if current vehicle has full schedule:
a. Create new schedule for current and find most optimal trip (covered in section 2).
b. If an optimal trip was found, insert corresponding events and remove corresponding
passenger trip from passengerTripList.
c. If an optimal trip was not found, move on to the next vehicle by incrementing
currentVehicleNumber by 1. Add a vehicle to vehicleList if currVehicleNumber ==
vehicleListSize – 1.

iii. Else find most optimal trip to insert for current schedule (covered in section 3).
a. If an optimal trip was found, insert corresponding events and remove corresponding
passenger trip from passengerTripList.
b. If an optimal trip was not found, move on to the next vehicle by incrementing
currentVehicleNumber by 1. Add a vehicle to vehicleList if currVehicleNumber ==
vehicleListSize – 1.

Terms:
LatestPickupTime – latest time by which passenger must be picked up so that they can be dropped off by
the latest allowable dropoff time with no stops in between.
PotentialPickupTime – time at which vehicle will reach passenger’s pickup location.
BufferTime – Difference between LatestPickupTime and PotentialPickupTime.
Section 2: Find most optimal trip for new schedule.
1. From the sorted passengerTripList search for the first passenger trip where the
PotentialPickupTime is equal or earlier than the earliest allowable pickup time of that passenger.
2. If such a passenger trip was found, then that is the most optimal trip for the new schedule of the
vehicle in current consideration.
3. Else, search for the trip which results in the maximum non-negative BufferTime.

Terms:
BufferTime – For a given event in a given schedule, it is the maximum time difference by which the event
can be delayed such that the given event and all next events in the given schedule complete within their
allowable time limit.
BufferTimeDelta – Amount of time by which the BufferTime would reduce if the given passenger trip
events are inserted in the current schedule.
Section 3: Find most optimal trip insertion for current schedule and given passenger trip
1. Between each two events (inclusive of before the first event and after the last), attempt to
insert the current passenger trip events both consecutively and non-consecutively and calculate
the BufferTimeDelta.
2. Find the insertion index where BufferTimeDelta is minimum and such that BufferTimeDelta is
not greater than BufferTime.
