package google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import connection.BusLocation;
import connection.BusLocationsQuery;

public class GeocodeService {

	/*
	* Geocode request URL. Here see we are passing "json" it means we will get
	* the output in JSON format. You can also pass "xml" instead of "json" for
	* XML output. For XML output URL will be
	* "http://maps.googleapis.com/maps/api/geocode/xml";
	*/

	private static final String URLdistance = "http://maps.googleapis.com/maps/api/distancematrix/json";

	/*
	* Here the fullAddress String is in format like
	* "address,city,state,zipcode". Here address means "street number + route"
	* .
	*/
	
//	private String getJSONByGoogle(String fullAddress) {
//
//		/*
//		* Create an java.net.URL object by passing the request URL in
//		* constructor. Here you can see I am converting the fullAddress String
//		* in UTF-8 format. You will get Exception if you don't convert your
//		* address in UTF-8 format. Perhaps google loves UTF-8 format. :) In
//		* parameter we also need to pass "sensor" parameter. sensor (required
//		* parameter) — Indicates whether or not the geocoding request comes
//		* from a device with a location sensor. This value must be either true
//		* or false.
//		*/
//		URL url = null;
//		try {
//			url = new URL(URL + "?address="
//					+ URLEncoder.encode(fullAddress, "UTF-8") + "&sensor=false");
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// Open the Connection
//		URLConnection conn = null;
//		try {
//			conn = url.openConnection();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// This is Simple a byte array output stream that we will use to keep
//		// the output data from google.
//		ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
//
//		// copying the output data from Google which will be either in JSON or
//		// XML depending on your request URL that in which format you have
//		// requested.
//		try {
//			IOUtils.copy(conn.getInputStream(), output);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// close the byte array output stream now.
//		try {
//			output.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return output.toString(); // This returned String is JSON string from
//									// which you can retrieve all key value pair
//									// and can save it in POJO.
//	}

	private static String getJSONDistance(double latX, double lonX, double latY, double lonY) throws UnsupportedEncodingException {

		/*
		* Create an java.net.URL object by passing the request URL in
		* constructor. Here you can see I am converting the fullAddress String
		* in UTF-8 format. You will get Exception if you don't convert your
		* address in UTF-8 format. Perhaps google loves UTF-8 format. :) In
		* parameter we also need to pass "sensor" parameter. sensor (required
		* parameter) — Indicates whether or not the geocoding request comes
		* from a device with a location sensor. This value must be either true
		* or false.
		*/
		URL url = null;
		try {
			url = new URL(URLdistance + "?origins="+latX+","+lonX+"&destinations="+latY+","+lonY+"&sensor=false");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Open the Connection
		URLConnection conn = null;
		try {
			conn = url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// This is Simple a byte array output stream that we will use to keep
		// the output data from google.
		ByteArrayOutputStream output = new ByteArrayOutputStream(1024);

		// copying the output data from Google which will be either in JSON or
		// XML depending on your request URL that in which format you have
		// requested.
		try {
			IOUtils.copy(conn.getInputStream(), output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// close the byte array output stream now.
		try {
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return output.toString(); // This returned String is JSON string from
									// which you can retrieve all key value pair
									// and can save it in POJO.
	}

	public static int getTravelTimeByCoord(double latX, double lonX, double latY, double lonY) {
		String distanceFeed = null;
		try {
			distanceFeed = GeocodeService.getJSONDistance(latX,lonX,latY,lonY);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject ja = new JSONObject(distanceFeed);
		if (ja.get("origin_addresses").equals("[\"Zakinthos, Greece\"]")
				|| ja.get("destination_addresses").equals(
						"[\"Zakinthos, Greece\"]"))
			return -1;
		JSONArray rows = (JSONArray) ja.get("rows");
		JSONObject elmnts = (JSONObject) rows.get(0);
		JSONArray infoArr = (JSONArray) elmnts.get("elements");
		JSONObject infoJson = (JSONObject) infoArr.get(0);
		String st = ((JSONObject) infoJson.get("duration")).get("value")
				.toString();// .toString().replace(" mins",
							// "").replace(" min", "");
		return Integer.parseInt(st);
	}
	
	public static int getdistanceByCoord(double latX, double lonX, double latY, double lonY) {
		String distanceFeed = null	;
		try {
			distanceFeed = GeocodeService.getJSONDistance(latX,lonX,latY,lonY);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject ja = new JSONObject(distanceFeed);
		if (ja.get("origin_addresses").equals("[\"Zakinthos, Greece\"]")
				|| ja.get("destination_addresses").equals(
						"[\"Zakinthos, Greece\"]"))
			return -1;
		JSONArray rows = (JSONArray) ja.get("rows");
		JSONObject elmnts = (JSONObject) rows.get(0);
		JSONArray infoArr = (JSONArray) elmnts.get("elements");
		JSONObject infoJson = (JSONObject) infoArr.get(0);
		String st = ((JSONObject) infoJson.get("distance")).get("value")
				.toString();// .toString().replace(" mins",
							// "").replace(" min", "");
		return Integer.parseInt(st);
	}
	
	public static void main(String[] args) {
		BusLocationsQuery bus = new BusLocationsQuery();
		ArrayList<BusLocation> tabBus = bus.getAllBusLocation();
		for (int i=0; i<tabBus.size(); i++){
			BusLocation a = tabBus.get(i);
			for (int j=i; j<tabBus.size(); j++){
				BusLocation z = tabBus.get(j);
				int distance = GeocodeService.getdistanceByCoord(a.getLat(), a.getLng(), z.getLat(), z.getLng());
				int temps = GeocodeService.getTravelTimeByCoord(a.getLat(), a.getLng(), z.getLat(), z.getLng());
				System.out.println(a.getName()+"\t"+z.getName()+"\t"+distance+"\t"+temps);
			}
		}
	}
}