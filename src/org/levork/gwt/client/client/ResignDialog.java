package org.levork.gwt.client.client;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
public class ResignDialog extends CenteredDialogBox {

    public ResignDialog(Board board) {
	m_board = board;
	final VerticalPanel vpanel = new VerticalPanel ();
	vpanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	final Label label = new Label("Are you sure you want to resign?", true);
	label.setStyleName("CBB-dialog-message");
	vpanel.add(label);
	vpanel.setStyleName("CBB-dialog");
	final Button ok = new Button("OK", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		ResignDialog.this.hide();
		m_board.resign();
	    }
	});
	final Button cancel = new Button("Cancel", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		ResignDialog.this.hide();
	    }
	});
	final HorizontalPanel hpanel = new HorizontalPanel ();
	hpanel.add(ok);
	hpanel.add(cancel);
	vpanel.add(hpanel);
	setWidget(vpanel);
	setText("Confirm resignation");
    }
    private final Board m_board;
}
