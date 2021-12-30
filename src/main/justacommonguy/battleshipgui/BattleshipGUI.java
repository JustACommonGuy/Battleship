package justacommonguy.battleshipgui;

import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import justacommonguy.battleshipgui.player.AllyPlayer;
import justacommonguy.battleshipgui.player.EnemyPlayer;
import justacommonguy.guiutils.GUI;
import justacommonguy.guiutils.SwingUtils;

// !Should keep this in mind: https://www.oracle.com/java/technologies/javase/codeconventions-fileorganization.html#1852
// !Move stuff somewhere else to avoid violating single responsibility. Make a separate class for game logic.
// TODO. Add better exception handling
// TODO. Add better logging
//? Add a counter of guesses?
public class BattleshipGUI implements GUI {

	private GameClient client;

	public static BattleshipGUI gameGUI;
	public static final double Y_RESOLUTION = 
			Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	public static final double X_RESOLUTION =
			Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	
	// !
	// ? Maybe player should be in GameLogic
	private AllyPlayer player;
	// ? Only the enemy's name might be needed
	private EnemyPlayer enemy;

	private JFrame frame = new JFrame();
	private JPanel playerMap;
	private JPanel enemyMap;
	private JLabel enemyGridLabel;
	//TODO. Change this panel when game has started.
	private JPanel buttonPanel;
	private JButton hostButton;
	private JButton joinButton;

	// !
	public BattleshipGUI(GameClient client) {
		this.client = client;
	}

	public String askName() {
		JOptionPane popup = new JOptionPane("Please input your username.", JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
		popup.setWantsInput(true);
		JDialog dialog = popup.createDialog(new JFrame(), "Welcome");

		popup.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();

		String name = (String) popup.getInputValue();
		// Insist (rather aggressively) on a valid name with recursion.
		if (name.equals("")) {
			name = askName();
		}
		
		return name;
	}

	// ? Divide the method into several smaller GUI methods.
	@Override
	public void start(int closeOperation) {
		//? Maybe disable close button so the player will quit appropiately.
		//https://www.coderanch.com/t/344419/java/deactivate-close-minimise-resizable-window
		
		/* Size of the elements is based on resolution. 
		GUI might blow up in displays that do not have 16:9 resolutions. */
		SwingUtils.setUpJFrame(frame, (int) ((0.75) * X_RESOLUTION), (int) ((0.75) * Y_RESOLUTION));

		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(3, 3, 3, 3);
		JPanel playArea = new JPanel(new GridBagLayout());
		Font bold = new Font(Font.SANS_SERIF, Font.BOLD, 20);

		playerMap = player.makeMap();
		enemyMap = enemy.makeMap();
		JLabel gridLabel = new JLabel("YOUR GRID");
		enemyGridLabel = new JLabel(enemy + "'S GRID");
		gridLabel.setFont(bold);
		enemyGridLabel.setFont(bold);

		SwingUtils.setGridBagConstraintsValues(gbc, 0, 0, 0, 0, insets);
		playArea.add(gridLabel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 1, 0, 0, 0, insets);
		playArea.add(enemyGridLabel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 0, 1, 0, 0, insets);
		playArea.add(playerMap, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 1, 1, 0, 0, insets);
		playArea.add(enemyMap, gbc);
		JPanel eastPanel = new JPanel();
		eastPanel.add(playArea);


		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JPanel chatPanel = new JPanel();

		hostButton = new JButton("Host game");
		hostButton.addActionListener(new HostGameListener());
		joinButton = new JButton("Join game");
		joinButton.addActionListener(new JoinGameListener());

		buttonPanel.add(hostButton);
		//// buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(joinButton);
		westPanel.add(buttonPanel);
		westPanel.add(chatPanel);

		frame.add(westPanel, BorderLayout.WEST);
		frame.add(eastPanel, BorderLayout.EAST);
		/* For reasons that humanity will never know, 
		the dumb frame needs to be brought to the front when a popup appears. */
		SwingUtils.frameToFront(frame);
	}

	public class HostGameListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			client.hostGame();
		}
	}

	public class JoinGameListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JLabel info = new JLabel("Please input the host's IP Address and Port.");
			info.setAlignmentX(Component.CENTER_ALIGNMENT);
			JTextField addressField = new JTextField();
			addressField.putClientProperty("JTextField.placeholderText", "IP Address");
			addressField.setText(gameSettings.getSetting("default_ip_address"));
			JTextField portField = new JTextField();
			portField.putClientProperty("JTextField.placeholderText", "Port");
			portField.setText(gameSettings.getSetting("default_port"));
			JCheckBox saveInfoCheck = new JCheckBox("Save to default settings", true);
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(info);
			panel.add(addressField);
			panel.add(portField);
			panel.add(saveInfoCheck);
	
			int option = JOptionPane.showConfirmDialog(frame, panel, "Join game", 
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (option == JOptionPane.OK_OPTION) {
				if (saveInfoCheck.isSelected()) {
					gameSettings.setSetting("default_ip_address", addressField.getText());
					gameSettings.setSetting("default_port", portField.getText());
					gameSettings.saveSettings();
				}
				client.joinGame(addressField.getText(), Integer.parseInt(portField.getText()));
			}
		}
	}

	public void startGame(String enemyName) {
		enemyGridLabel.setText(enemyName + "'S GRID");
	}
}
