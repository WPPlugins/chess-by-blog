package org.levork.gwt.client.client;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
public class AcceptDrawDialog extends CenteredDialogBox {

    public AcceptDrawDialog(Board board) {
	m_board = board;
	final VerticalPanel vpanel = new VerticalPanel ();
	vpanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	final Label label = new Label("Would you like to accept or refuse the offered draw?", true);
	label.setStyleName("CBB-dialog-message");
	vpanel.add(label);
	vpanel.setStyleName("CBB-dialog");
	final HorizontalPanel hpanel = new HorizontalPanel ();
	hpanel.add(new Button("Accept", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		AcceptDrawDialog.this.hide();
		m_board.acceptDraw();
	    }
	}));
	hpanel.add(new Button("Refuse", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		AcceptDrawDialog.this.hide();
		m_board.refuseDraw();
	    }
	}));	
	hpanel.add(new Button("Cancel", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		AcceptDrawDialog.this.hide();
	    }
	}));
	vpanel.add(hpanel);
	setWidget(vpanel);
	setText("Accept or refuse draw");
    }
    private final Board m_board;
}
