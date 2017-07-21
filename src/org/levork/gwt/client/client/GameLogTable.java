package org.levork.gwt.client.client;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
import java.util.ArrayList;
import java.util.Iterator;
public class GameLogTable extends FlexTable implements ClickHandler
{
    public GameLogTable(Board board, final String whiteLastName, final String blackLastName) {
        super();
        m_board = board;
        m_whiteLastName = whiteLastName;
        m_blackLastName = blackLastName;
        m_rows = 0;
        m_columns = 0;
        m_selectedRow = m_selectedColumn = m_selectedPly = -1;
        m_movelist = new ArrayList<String>();
	m_boardmaplist = new ArrayList<String>();
	m_rowlist = new ArrayList<Integer>();
	m_columnlist = new ArrayList<Integer>();
	m_commentlist = new ArrayList<String>();
        this.addClickHandler(this);
	setText(0, 1, "White");
	setText(0, 2, "Black");
	getCellFormatter().setStyleName(0, 1, "CBB-gamelog-header");
	getCellFormatter().setStyleName(0, 2, "CBB-gamelog-header");
        if (m_whiteLastName != null) {
            setText(1, 1, m_whiteLastName);
            getCellFormatter().setStyleName(1, 1, "CBB-gamelog-header");
            m_rows = 1;
        }
        if (m_blackLastName != null) {
            setText(1, 2, m_blackLastName);
            getCellFormatter().setStyleName(1, 2, "CBB-gamelog-header");
            m_rows = 1;
        }
    }

    public void onClick(ClickEvent event) {
        
        Cell cell = getCellForEvent(event);
        if (cell == null) return;

        int row = cell.getRowIndex();
        int col = cell.getCellIndex();

        // Figure out what turn we actually clicked on.
        int color = Piece.WHITE;
        int ply = 0;

        // Clicked on a move number
        if (col % 3 == 0) return;

        // Clicked on a header
        if (row == 0) return;
        if (row == 1 && (m_blackLastName != null || m_whiteLastName != null)) return;

        // Figure out the ply and color corresponding to current
        // entry
        if (m_blackLastName != null || m_whiteLastName != null) {
            ply = 2 * ((col / 3) * 20 + row - 2) + 1;
        } else {
            ply = 2 * ((col / 3) * 21 + row - 1) + 1;
        }
        if (col % 3 == 2) {
            ply++;
        }
        // Already selected?
        if (ply == m_selectedPly) {
            return;
        }

        // New selection: format the cells accordingly
        if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
            getCellFormatter().removeStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
        }
        m_selectedRow = row;
        m_selectedColumn = col;
	getCellFormatter().addStyleName(row, col, "CBB-gamelog-selected-entry");

        if (ply == m_boardmaplist.size() - 1) {
            int diff = ply - m_selectedPly;            
	    showCurrentPosition(diff == 1 || diff == -1);
	} else {
            int diff = ply - m_selectedPly;
	    showMapAtPly(ply, diff == 1 || diff == -1, diff > 0);
	}
        m_selectedPly = ply;
    }

    private void showCurrentPosition(boolean singlestep) {
	String notation = m_movelist.get(m_movelist.size() - 1);
	if (!m_commentlist.get(m_commentlist.size() - 1).equals("")) {
            if (notation.equals("")) {
                notation = m_commentlist.get(m_commentlist.size() - 1);
            } else {
                notation += " - " + m_commentlist.get(m_commentlist.size() - 1);
            }
	}
	m_board.showCurrentPosition(notation, singlestep);
    }
    
    private void showMapAtPly(int ply, boolean singlestep, boolean forward) {
	String notation = m_movelist.get(ply);
	if (!m_commentlist.get(ply).equals("")) {
            if (notation.equals("")) {
                notation = m_commentlist.get(ply);
            } else {
                notation += " - " + m_commentlist.get(ply);
            }
	}
	m_board.showMap(notation, m_boardmaplist.get(ply), singlestep, forward);
    }
    
    public void addComment(String comment) {
	if (m_commentlist.size() > 0) {
	    String oldcomment = m_commentlist.get(m_commentlist.size() - 1);
	    if (oldcomment.equals("")) {
		m_commentlist.set(m_commentlist.size() - 1, comment);
		int color = (m_commentlist.size()) % 2;
		getCellFormatter().addStyleName(m_rows, m_columns * 3 + color + 1, "CBB-gamelog-entry-with-comment");
	    } else {
		m_commentlist.set(m_commentlist.size() - 1, oldcomment + " " + comment);
	    }
	}
    }
    
    public void addMove(int color, int turn, String notation, String boardmap) {
	if (notation == "") {
	    m_movelist.add("");
	} else if (color == Piece.WHITE) {
            m_movelist.add(Integer.toString(turn) + '.' + notation);
        } else {
            m_movelist.add(Integer.toString(turn) + "... " + notation);
        }
	while (m_commentlist.size() < m_movelist.size()) {
	    m_commentlist.add("");
	}
	m_boardmaplist.add(boardmap);
        if (turn != 0 && color == Piece.WHITE) {
            m_rows++;
            if (m_rows == 22) {
                m_columns++;
                final int wcol = 3 * m_columns + 1;
                final int bcol = wcol + 1;
                setText(0, wcol, "White");
                setText(0, bcol, "Black");
                getCellFormatter().setStyleName(0, wcol, "CBB-gamelog-header");
                getCellFormatter().setStyleName(0, bcol, "CBB-gamelog-header");
                m_rows = 1;
                if (m_blackLastName != null || m_whiteLastName != null) {
                    if (m_whiteLastName != null) {
                        setText(1, wcol, m_whiteLastName);
                        getCellFormatter().setStyleName(1, wcol, "CBB-gamelog-header");
                    }
                    if (m_blackLastName != null) {
                        setText(1, bcol, m_blackLastName);
                        getCellFormatter().setStyleName(1, bcol, "CBB-gamelog-header");
                    }
                    m_rows = 2;
                }
            }
	    setText(m_rows, m_columns * 3, Integer.toString(turn));
	    getCellFormatter().setStyleName(m_rows, m_columns * 3, "CBB-gamelog-number");
	}
        setText(m_rows, m_columns * 3 + color + 1, notation);
        getCellFormatter().setStylePrimaryName(m_rows, m_columns * 3 + color + 1, "CBB-gamelog-entry");
        if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
            getCellFormatter().removeStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
        }
	m_rowlist.add(m_rows);
	m_columnlist.add(m_columns * 3 + color + 1);
	m_selectedRow = m_rows;
	m_selectedColumn = m_columns * 3 + color + 1;
	m_selectedPly = m_boardmaplist.size() - 1;
	getCellFormatter().addStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");	
    }

    public void start() {
	if (m_selectedPly != 0 && m_boardmaplist.size() > 0) {
            int diff = m_selectedPly;
	    m_selectedPly = 0;
	    showMapAtPly(m_selectedPly, diff == 1, false);
	    if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
		getCellFormatter().removeStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
	    }
	}
    }

    public void rewind() {
	if (m_selectedPly == 0) return;
	if (m_selectedPly == -1) {
	    m_selectedPly = m_boardmaplist.size() - 1;
	} else {
	    m_selectedPly--;
	    showMapAtPly(m_selectedPly, true, false);
	    if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
		getCellFormatter().removeStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
	    }
	    m_selectedRow = m_rowlist.get(m_selectedPly);
	    m_selectedColumn = m_columnlist.get(m_selectedPly);
            if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
                getCellFormatter().addStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
            }
	}
	
    }

    public void forward() {
	if (m_selectedPly != -1 && m_selectedPly != m_boardmaplist.size() - 1) {
	    m_selectedPly++;
	    if (m_selectedPly == m_boardmaplist.size() - 1) {
		showCurrentPosition(true);
	    } else {
		showMapAtPly(m_selectedPly, true, true);
	    }
	    if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
		getCellFormatter().removeStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
	    }
	    m_selectedRow = m_rowlist.get(m_selectedPly);
	    m_selectedColumn = m_columnlist.get(m_selectedPly);
	    getCellFormatter().addStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
	}
    }

    public void end() {
	if (m_movelist.size() > 0) {
            int diff = m_boardmaplist.size() - 1 - m_selectedPly;
	    showCurrentPosition(diff == 1 || diff == -1);
	    m_selectedPly = m_boardmaplist.size() - 1;
	    if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
		getCellFormatter().removeStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
	    }
	    m_selectedRow = m_rowlist.get(m_selectedPly);
	    m_selectedColumn = m_columnlist.get(m_selectedPly);
	    getCellFormatter().addStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
	}
    }

    public void setPly(int ply) {
        if (ply >= 0 && ply < m_boardmaplist.size()) {
            int diff = ply - m_selectedPly;
            boolean forward = (diff > 0);
            m_selectedPly = ply;
	    if (m_selectedPly == m_boardmaplist.size() - 1) {
		showCurrentPosition(diff == 1 || diff == -1);
	    } else {
		showMapAtPly(m_selectedPly, diff == 1 || diff == -1, forward);
	    }
	    if (m_selectedRow >= 0 && m_selectedColumn >= 0) {
		getCellFormatter().removeStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
	    }
	    m_selectedRow = m_rowlist.get(m_selectedPly);
	    m_selectedColumn = m_columnlist.get(m_selectedPly);
            if (m_selectedRow >= 0 && m_selectedColumn >= 0) {            
                getCellFormatter().addStyleName(m_selectedRow, m_selectedColumn, "CBB-gamelog-selected-entry");
            }
        }
    }
    
    final private Board m_board;

    final private String m_whiteLastName;
    final private String m_blackLastName;

    // String list of moves, sized by ply
    private ArrayList<String> m_movelist;
    
    // String list of board maps, sized by ply
    private ArrayList<String> m_boardmaplist;

    // String list of comments, sized by ply
    private ArrayList<String> m_commentlist;

    // Mapping from turn to row
    private ArrayList<Integer> m_rowlist;

    // Mapping from turn to column
    private ArrayList<Integer> m_columnlist;

    int m_rows;
    int m_columns;
    int m_selectedRow;
    int m_selectedColumn;
    int m_selectedPly;
}
