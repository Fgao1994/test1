package cn.edu.whu.openmi.provenance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;

import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.buffer.SmartBuffer;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public class ProvUtils {
	public static boolean saveProv2File(String filepath,
			List<SmartBufferInfo> bufferInfos) {

		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));

			String timeFormat = "yyyy-MM-dd hh:mm:ss";
			bw.write("Provenance infomation.");
			bw.newLine();

			for (SmartBufferInfo bufferInfo : bufferInfos) {
				ITime time = bufferInfo.getTime();
				if (!(time instanceof TimeStamp)) {
					continue;
				}
				Calendar calendar = CalendarConverter
						.modifiedJulian2Gregorian(((TimeStamp) time)
								.getModifiedJulianDay());
				String timeStr = OpenMIUtilities.calendar2Str(calendar,
						timeFormat);
				SmartBuffer smartBuffer = bufferInfo.getSmartBuffer();
				bw.write("------------------------------------------------------------------------------");
				bw.newLine();
				bw.write("linkid: " + bufferInfo.getLinkId());
				bw.newLine();
				bw.write("Time: " + timeStr);
				bw.newLine();
				int num = smartBuffer.getTimesCount();
				bw.write("There is " + num + " buffered value: ");
				bw.newLine();
				for (int i = 0; i < num; i++) {
					ITime time2 = smartBuffer.getTimeAt(i);
					if (time2 instanceof TimeStamp) {
						Calendar calendar2 = CalendarConverter
								.modifiedJulian2Gregorian(((TimeStamp) time2)
										.getModifiedJulianDay());
						String timeStr2 = OpenMIUtilities.calendar2Str(
								calendar2, timeFormat);
						bw.write(timeStr2 + ", ");
					} else {
						bw.write("not a Timestamp, ");
					}
					IValueSet valueSet = smartBuffer.getValuesAt(i);

					if (valueSet instanceof ScalarSet) {
						ScalarSet scalarSet = (ScalarSet) valueSet;
						double value = scalarSet.get(0);
						bw.write(value + "");
					} else {
						bw.write("Not a ScalarSet.");
					}

					bw.newLine();
				}
			}
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}

	}
}
