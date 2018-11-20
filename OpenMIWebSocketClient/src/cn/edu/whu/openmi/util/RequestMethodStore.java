package cn.edu.whu.openmi.util;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;


/**
 * Sends a request with a post or get method.
 * 
 * @author Mingda Zhang
 * 
 */
public class RequestMethodStore {

	/**
	 * Sends a request by post method and returns response in string format.
	 * 
	 * @param param
	 *            content to send
	 * @param input_url
	 *            service address.
	 * 
	 */
	public static String POST(String param, String input_url) {

		try {
			URL url = new URL(input_url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);

			PrintWriter xmlOut = new PrintWriter(con.getOutputStream());
			xmlOut.write(param);
			xmlOut.flush();
			BufferedReader response = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String result = "";
			String line;
			while ((line = response.readLine()) != null) {
				result += "\n" + line;
			}
			return result.toString();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	/**
	 * Sends a request by post method and returns response in document format.
	 * 
	 * @param param
	 *            content to send
	 * @param input_url
	 *            service address.
	 * 
	 */
	public static Document POSTDoc(String param, String input_url) {

		try {
			URL url = new URL(input_url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/xml");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);

			PrintWriter xmlOut = new PrintWriter(con.getOutputStream());
			xmlOut.write(param);
			xmlOut.flush();
			SAXReader reader = new SAXReader();
			Document doc = reader.read(con.getInputStream());
			return doc;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sends a request by get method and returns response in document format.
	 * 
	 * @param param
	 *            content to send.
	 * @param url
	 *            service address.
	 * 
	 */
	public static Document GETDoc(String url, String param) {
		String realUrl = null;
		if (url.endsWith("?")) {
			realUrl = url + param;
		} else {
			realUrl = url + "?" + param;
		}
		return GetDoc(realUrl);
	}

	/**
	 * Sends a request by post method and returns response in string format.
	 * 
	 * @param url
	 *            service address and parameters.
	 * 
	 */
	public static Document GetDoc(String url) {
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			SAXReader reader = new SAXReader();
			Document document = reader.read(conn.getInputStream());
			return document;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	/**
	 * Sends a request by get method and returns response in string format.
	 * 
	 * @param url
	 *            service address containing parameters.
	 * 
	 */
	public static String GETStr(String url) {

		try {

			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("accept", "text/xml");
			//Change the http connection way
			//conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("connection", "close");
			//conn.setRequestProperty("Keep-Alive", "1");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

			BufferedReader response = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuffer result = new StringBuffer();
			String line;
			while ((line = response.readLine()) != null) {
				result.append(line.trim() + "\n");
			}
			return result.toString();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}

	}


	public static void main(String[] args) {
	}
}
