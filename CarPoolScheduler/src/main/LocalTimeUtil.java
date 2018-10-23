package main;

import java.time.LocalTime;

public class LocalTimeUtil {
	public static int minus(LocalTime t1, LocalTime t2) {
		return (t1.toSecondOfDay() - t2.toSecondOfDay())/60;
	}
}
