package cn.edu.whu.openmi.models.Hargreaves;

import java.util.Calendar;

import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out
				.println(calculatePET(13.9, 31.7, 22.8, 35.06666667, 51708.0));
	}

	private static double calculatePET(double minTemp, double maxTemp,
			double averTemp, double latitude, double jlianDay) {
		// TimeStamp ts = (TimeStamp)this.getCurrentTime();
		Calendar calendar = CalendarConverter
				.modifiedJulian2Gregorian(jlianDay);
		// int j = dt.DayOfYear;
		int j = calendar.get(Calendar.DAY_OF_YEAR);
		double dr = 1 + 0.033 * Math.cos((2 * Math.PI * j) / 365);

		// ---- calculate the solar declination
		double d = 0.4093 * Math.sin((2 * Math.PI * j) / 365 - 1.405);

		// ---- calculate the sunset hour angle
		// -- get latitude in degrees
		/*
		 * ElementSet es = (ElementSet)this.GetInputExchangeItem(0).ElementSet;
		 * Element e = es.GetElement(eid);
		 */
		// double p = e.GetVertex(0).y * Math.PI / 180;
		double p = latitude * Math.PI / 180;
		// -- calc ws
		double ws = Math.acos(-1 * Math.tan(p) * Math.tan(d));

		// ---- calculate the total incoming extra terrestrial solar radiation
		// (tested against
		// http://www.engr.scu.edu/~emaurer/tools/calc_solar_cgi.pl)
		double Ra = 15.392
				* dr
				* (ws * Math.sin(p) * Math.sin(d) + Math.cos(p) * Math.cos(d)
						* Math.sin(ws));

		// ---- calculate PET (From Hargreaves and Samani 1985)
		// -- calculate latent heat of vaporization (from Water Resources
		// Engineering, David A. Chin)
		double L = 2.501 - 0.002361 * averTemp;
		double PET = (0.0023 * Ra * Math.sqrt(maxTemp - minTemp) * (averTemp + 17.8))
				/ L;

		return PET;
	}
}
