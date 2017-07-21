package org.levork.gwt.client.client;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;

public class SizedPushButton extends PushButton {

    SizedPushButton(String upText, String style, String title, ClickHandler handler) {
	super(upText, handler);
	// PushButton builds disabled/down/hover/etc stylenames on top
	// of the base style name
	setStyleName(style);
	addStyleName("CBB-button");
	DOM.setElementProperty(getElement(), "title", title);	
    }

    // See note in Square.fixWidth().
    public void fixWidth() {
	setWidth(DOM.getElementProperty(SizedPushButton.this.getElement(), "clientHeight"));
    }
}
