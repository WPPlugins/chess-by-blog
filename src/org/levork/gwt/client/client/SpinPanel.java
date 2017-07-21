package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
public class SpinPanel extends PopupPanel {
    public SpinPanel(Board board) {
	m_board = board;
	Label label = new Label("Posting move to blog..");
	label.setStyleName("CBB-post-message");
	setWidget(label);
    }

    public void show() {
	DOM.setStyleAttribute(getElement(), "visibility", "hidden");
    	super.show();
    	DeferredCommand.addCommand(new Command() {
            public void execute() {
		setPopupPosition(m_board.getCenterX() - getOffsetWidth() / 2,
				 m_board.getCenterY() - getOffsetHeight() / 2);
        	DOM.setStyleAttribute(getElement(),"visibility","visible");
            }
        });
    }
    
    private final Board m_board;    
}
