package justacommonguy.battleshipgui.cell;

import java.util.ArrayList;

public class HighlightInitiator {
	
	private final ArrayList<HighlightListener> listeners = new ArrayList<HighlightListener>();

	public void addHighlightListener(HighlightListener listener) {
		listeners.add(listener);
	}

	public void fire() {
		for (HighlightListener listener : listeners) {
			listener.cellHighlighted();
		}
	}

	public void unfire() {
		for (HighlightListener listener : listeners) {
			listener.cellUnhighlighted();
		}
	}
}