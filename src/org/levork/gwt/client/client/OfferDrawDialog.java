package org.levork.gwt.client.client;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
public class OfferDrawDialog extends CenteredDialogBox {

    public OfferDrawDialog(Board board) {
	m_board = board;
	final VerticalPanel vpanel = new VerticalPanel ();
	vpanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	final Label label = new Label("Are you sure you want to offer a draw?", true);
	label.setStyleName("CBB-dialog-message");
	vpanel.add(label);
	vpanel.setStyleName("CBB-dialog");
	final HorizontalPanel hpanel = new HorizontalPanel ();
	hpanel.add(new Button("OK", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		OfferDrawDialog.this.hide();
		m_board.offerDraw();
	    }
	}));
	hpanel.add(new Button("Cancel", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		OfferDrawDialog.this.hide();
	    }
	}));
	vpanel.add(hpanel);
	setWidget(vpanel);
	setText("Confirm draw");
    }
    private final Board m_board;
}
