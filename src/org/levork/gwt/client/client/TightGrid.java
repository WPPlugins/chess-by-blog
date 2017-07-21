package org.levork.gwt.client.client;
import com.google.gwt.user.client.ui.*;
public class TightGrid extends Grid {
    public TightGrid(String stylename, String cellstylename) {
	super();
	m_cellstylename = cellstylename;
	if (stylename != null) {
	    setStyleName(stylename);
	}
	setBorderWidth(0);
	setCellPadding(0);
	setCellSpacing(0);
    }
    public TightGrid(String stylename) {
	this(stylename, null);
    }
    public TightGrid() {
	this(null, null);
    }
    public void resize(int rows, int columns) {
	super.resize(rows, columns);
	if (m_cellstylename != null) {
	    for (int i = 0; i < rows; ++i) {
		for (int j = 0; j < columns; ++j) {
		    getCellFormatter().setStyleName(i, j, m_cellstylename);
		}
	    }
	}
    }
    private final String m_cellstylename;
}
