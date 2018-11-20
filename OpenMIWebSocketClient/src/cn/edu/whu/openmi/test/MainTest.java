package cn.edu.whu.openmi.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InputStream is=null;
		String inputTemp = "H:\\360sync\\sync\\openmi\\workspace\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\models\\Hargreaves\\TemperatureInput-all.txt";
		try {
			is = new FileInputStream(new File(inputTemp));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			int i = 0;
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				System.out.println(line);
				if (line.startsWith("//") || line.trim().equals("")) {
					continue;
				}
				//get the latitude in the first line.
				if (i==0) {
					String[] meta = line.split(";");
					Double latitude = Double.parseDouble(meta[0]);
					Double noValue = Double.parseDouble(meta[2]);
					i=1;
					continue;
				}
				String[] values = line.split("\t");
				// yyyy-MM-dd HH:mm:ss,2000/6/27 0:00
				System.out.println(values);
//				timestamps.add(OpenMIUtilities.str2JulianDate(values[0].trim(),"yyyy/MM/dd HH:mm"));
				Double[] temps = new Double[]{Double.parseDouble(values[1]),Double.parseDouble(values[2]),Double.parseDouble(values[3])};
//				timeValues.add(temps);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
