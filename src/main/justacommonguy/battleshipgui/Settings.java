package justacommonguy.battleshipgui;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Settings {

	private Properties properties = new Properties();
	private File file;

	private static String SERVER_PORT = "1337";
	private static String IP_ADDRESS = "";
	private static String CONNECTION_PORT = "";
	private static String MISS = "0,0,0";
	private static String SHIP = "173,173,173";
	private static String HIT = "200,100,30";
	private static String KILL = "150,30,30";

	public Settings(File file) {
		this.file = file;
		while (true) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				properties.load(reader);
				break;
			}
			catch (FileNotFoundException e) {
				loadDefaultSettings();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadDefaultSettings() {

		properties.setProperty("username", "");
		properties.setProperty("server_port", SERVER_PORT);
		properties.setProperty("default_ip_address", IP_ADDRESS);
		properties.setProperty("default_port", CONNECTION_PORT);
		
		properties.setProperty("enemy_miss_color", MISS);
		properties.setProperty("enemy_ship_color", SHIP);
		properties.setProperty("enemy_hit_color", HIT);
		properties.setProperty("enemy_kill_color", KILL);
		properties.setProperty("ally_miss_color", MISS);
		properties.setProperty("ally_ship_color", SHIP);
		properties.setProperty("ally_hit_color", HIT);
		properties.setProperty("ally_kill_color", KILL);

		saveSettings();
	}

	public void saveSettings() {
		try {
			properties.store(new BufferedWriter(new FileWriter(file)), "Battleship settings");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Color stringToColor(String s) {
		String[] colorArray = s.split(",");
		int red = Integer.parseInt(colorArray[0]);
		int green = Integer.parseInt(colorArray[1]);
		int blue = Integer.parseInt(colorArray[2]);

		return new Color(red, green, blue);
	}

	public String getSetting(String key) {
		return properties.getProperty(key);
	}

	public Color getColor(String key) {
		return stringToColor(properties.getProperty(key));
	}

	public void setSetting(String key, String value) {
		properties.setProperty(key, value);
	}
}
