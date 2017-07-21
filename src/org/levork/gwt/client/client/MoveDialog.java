package org.levork.gwt.client.client;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
public class MoveDialog extends CenteredDialogBox {

    public MoveDialog(String text, Board board) {
	m_board = board;
	final VerticalPanel vpanel = new VerticalPanel ();
	vpanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	final Label label = new Label(text, true);
	label.setStyleName("CBB-dialog-message");
	vpanel.add(label);
	vpanel.setStyleName("CBB-dialog");
	vpanel.add(new Button("OK", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		MoveDialog.this.hide();
	    }
	}));
	setWidget(vpanel);
	setText("Move status");	
    }
    private final Board m_board;
}
