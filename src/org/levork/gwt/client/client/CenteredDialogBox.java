package org.levork.gwt.client.client;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

public class CenteredDialogBox extends DialogBox {
    public void show() {
	DOM.setStyleAttribute(getElement(), "visibility", "hidden");
    	super.show();
    	DeferredCommand.addCommand(new Command() {
            public void execute() {
		center();
		DOM.setStyleAttribute(getElement(), "visibility", "visible");
            }
        });
    }
}
