package justacommonguy.battleshipgui;

import static justacommonguy.battleshipgui.config.Settings.gameSettings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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

import justacommonguy.battleshipgui.cell.Ally.AllyMap;
import justacommonguy.battleshipgui.cell.Enemy.EnemyMap;
import justacommonguy.guiutils.GUI;
import justacommonguy.guiutils.SwingUtils;

// !Should keep this in mind: https://www.oracle.com/java/technologies/javase/codeconventions-fileorganization.html#1852
// TODO. Add better exception handling
// TODO. Add better logging
//? Add a counter of guesses?
public class BattleshipGUI implements GUI {

	private GameClient client;

	public static final double Y_RESOLUTION = 
			Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	public static final double X_RESOLUTION =
			Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	/** GridLayout does not allow for specific cell sizes, so we set a global
		size to display squared cells. */
	private static final Dimension PANEL_SIZE = new Dimension(
			(int) ( Y_RESOLUTION / 3), (int) (Y_RESOLUTION / 3));
	
	private String enemyName = "OPPONENT";

	private JFrame frame = new JFrame();
	private JPanel playArea = new JPanel(new GridBagLayout());
	private JPanel allyMapPanel;
	private JButton sendShipsButton;
	private JPanel enemyMapPanel;
	private JLabel attackLabel;
	// TODO Change label according to settings ("your" or "name's")
	private JLabel gridLabel;
	private JLabel enemyGridLabel;
	//TODO. Change this panel when game has started.
	private JPanel buttonPanel;
	private JButton hostButton;
	private JButton joinButton;
	private JButton restartButton;

	public BattleshipGUI(GameClient client) {
		this.client = client;
	}

	public static String askName() {
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
		else if (name.equals("uninitializedValue")) {
            System.exit(0);
		}
		
		return name;
	}

	// TODO Divide the method into several smaller GUI methods.
	@Override
	public void start(int closeOperation) {
		//? Maybe disable close button so the player will quit appropiately.
		//https://www.coderanch.com/t/344419/java/deactivate-close-minimise-resizable-window
		
		/* Size of the elements is based on resolution. 
		GUI might blow up in displays that do not have 16:9 resolutions. */
		SwingUtils.setUpJFrame(frame, (int) ((0.75) * X_RESOLUTION), (int) ((0.75) * Y_RESOLUTION));

		JPanel eastPanel = initEastArea();
		JPanel westPanel = initWestPanel();

		frame.add(westPanel, BorderLayout.WEST);
		frame.add(eastPanel, BorderLayout.EAST);
		/* For reasons that humanity will never know, 
		the dumb frame needs to be brought to the front when a popup appears. */
		SwingUtils.frameToFront(frame);
	}

	private JPanel initEastArea() {
		JPanel eastPanel = new JPanel();
		eastPanel.add(playArea);
		return eastPanel;
	}

	private JPanel initWestPanel() {
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JPanel chatPanel = new JPanel();

		hostButton = new JButton("Host game");
		hostButton.addActionListener(new HostGameListener());
		joinButton = new JButton("Join game");
		joinButton.addActionListener(new JoinGameListener());
		restartButton = new JButton("Restart game");
		restartButton.addActionListener(new RestartGameListener());

		buttonPanel.add(hostButton);
		//// buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(joinButton);
		westPanel.add(buttonPanel);
		westPanel.add(chatPanel);
		return westPanel;
	}

	// TODO. Add a randomize button
	public void initPlayArea(AllyMap allyMap, EnemyMap enemyMap) {
		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(3, 3, 3, 3);
		Font bold = new Font(Font.SANS_SERIF, Font.BOLD, 20);

		allyMapPanel = allyMap.makeMap(PANEL_SIZE);
		sendShipsButton = new JButton("Confirm ship placement.");
		sendShipsButton.addActionListener(allyMap);
		sendShipsButton.addActionListener(new SendShipsListener());
		allowPlacement(false);

		enemyMapPanel = enemyMap.makeMap(PANEL_SIZE);
		attackLabel = new JLabel();
		allowAttack(false);
		
		gridLabel = new JLabel("YOUR GRID");
		enemyGridLabel = new JLabel(enemyName + "'S GRID");
		gridLabel.setFont(bold);
		enemyGridLabel.setFont(bold);

		SwingUtils.setGridBagConstraintsValues(gbc, 0, 0, 0, 0, insets);
		playArea.add(gridLabel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 1, 0, 0, 0, insets);
		playArea.add(enemyGridLabel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 0, 1, 0, 0, insets);
		playArea.add(allyMapPanel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 1, 1, 0, 0, insets);
		playArea.add(enemyMapPanel, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 0, 2, 0, 0, insets);
		playArea.add(sendShipsButton, gbc);
		SwingUtils.setGridBagConstraintsValues(gbc, 1, 2, 0, 0, insets);
		playArea.add(attackLabel, gbc);
	}

	private class HostGameListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			client.hostGame();
		}
	}

	private class JoinGameListener implements ActionListener {

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

	private class RestartGameListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			restart();			
		}
	}

	public void startGame(String enemyName) {
		enemyGridLabel.setText(enemyName + "'S GRID");
	}

	private class SendShipsListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			allowPlacement(false);
		}
	}

	public void allowPlacement(boolean allow) {
		sendShipsButton.setVisible(allow);
	}

	public void allowAttack(boolean allow) {
		synchronized (attackLabel) {
			attackLabel.setText(allow ? "Select a cell to attack..." : "");
		}
	}

	public void updateAttackLabel(String text, int millis) {
		new Thread(() -> {
			synchronized (attackLabel) {
				attackLabel.setText(text);
				try {
					Thread.sleep(millis);
				}
				catch (InterruptedException e) {}
				attackLabel.setText("");
			}
		}, "AttackLabelUpdate").start();
	}

	void restart() {
		frame.dispose();
		client.restart();
	}
}
