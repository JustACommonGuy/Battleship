package justacommonguy.battleshipgui.gui;

public class DragInitiator {
	
	//** Since only one ship will be dragged at a time, only one listener should be active. */
	DragListener listener;

	public void setDragListener(DragListener listener) {
		this.listener = listener;
	}

	public void removeDragListener() {
		this.listener = null;
	}

	public void dragged() {
		listener.shipDragged();
	}

	public void dragging() {
		listener.draggingShip();
	}
}
