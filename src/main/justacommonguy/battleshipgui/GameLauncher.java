package justacommonguy.battleshipgui;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import java.io.File;

import javax.swing.UIManager;

import justacommonguy.battleshipgui.gui.BattleshipGUI;

public class GameLauncher {

	public static Settings gameSettings = new Settings(new File("settings.properties"));
	public static GameServer gameServer = new GameServer();
	public static BattleshipGUI gameGUI;
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
		}
		catch (Exception ex) {
			System.out.println("Failed to set LaF");
		}
	
		gameServer.start(args);
	}
}
