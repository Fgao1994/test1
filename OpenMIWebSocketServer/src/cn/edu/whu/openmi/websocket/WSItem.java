package cn.edu.whu.openmi.websocket;

import java.util.HashMap;
import java.util.Map;

public class WSItem {
	String name, quantity, element;
	Map<String, String> valMap = new HashMap<String, String>();
	String latestTime = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public void addValue(String time, String value) {
		//只保留一个数据
		this.valMap.clear();
		this.valMap.put(time, value);
		this.latestTime = time;
		/*this.valMap.put(time, value);
		if (latestTime == null)
			this.latestTime = time;

		if (WSModelUtils.isFormerThan(this.latestTime, time)) {
			this.latestTime = time;
		}*/
	}

	public String getLatestTime() {
		return this.latestTime;
	}

	public String getValue(String time) {
		return this.valMap.get(time);
	}
}
