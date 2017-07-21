package org.levork.gwt.client.client;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
public class Square extends SimplePanel {

    Square(char file, int rank, int color, Board board) {
	m_board = board;
	m_piece = null;
        m_pieceWidget = new SimplePanel();
	m_rank = rank;
	m_file = file;
	m_color = color;
	setStyleName("CBB-square");
	addStyleName(color == Piece.BLACK ? "CBB-blackSquare" : "CBB-whiteSquare");
	DOM.setEventListener(getElement(), this);
	sinkEvents(Event.ONCLICK | Event.ONDBLCLICK);
	m_top = m_right = m_bottom = m_left = null;
	m_ismoveable = false;

	// NB: this is a hack for IE 6.0. I don't know why it's needed
	// to force things to work correctly.
	setWidth("1px");
        add(m_pieceWidget);
    }

    // This seems like a craptacular way to work around IE's lack
    // of support for min-width in CSS. This won't work if I put
    // it in onLoad(), and I'm not supposed to override
    // onAttach()? And there's no direct GWT access to the client
    // height (height without decorations), hence we get the
    // attribute from the DOM.
    public void fixWidth() {
	setWidth(DOM.getElementProperty(Square.this.getElement(), "clientHeight"));
    }

    public void setPiece(Piece piece) {
	setPiece(piece, true);
    }
    
    public void setPiece(Piece piece, boolean update) {
	if (update) {
            setDisplayedPiece(piece);
        }
        m_piece = piece;
    }

    public void setDisplayedPiece(Piece piece) {
        if (m_displayPiece != piece) {
            m_pieceWidget.setStyleName("CBB-square");
	    if (piece != null) {
                m_pieceWidget.addStyleName(piece.getStyleName());
	    }
	    m_displayPiece = piece;
        }
    }
    
    public Piece getPiece() {
	return m_piece;
    }

    public Piece getDisplayPiece() {
        return m_displayPiece;
    }

    public void onBrowserEvent(Event event) {
	try {
	    if (DOM.eventGetType(event) == Event.ONCLICK) {
                m_board.cancelAnimation();
		// If playable
		if (m_board.canPlay()) {
                    if (!m_board.isCurrent()) return;
		    // If this square is a moveable square for the currently
		    // selected piece..
		    if (IsMoveable()) {
			// .. move selected piece to this square.
			m_board.getSelectedPiece().getSquare().setSelected(false);
			m_board.setMoveableSquares(false);
			m_board.getSelectedPiece().move(this);
		    } else {
			// Pass the click on to the piece, if there is one
			// sitting here, and it's the piece's turn
			if (m_piece != null && m_piece.getColor() == m_board.getColor()) {
			    m_piece.clickFromSquare(this);
			}
		    }
		}
		DOM.eventPreventDefault(event);
	    } else if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
		DOM.eventPreventDefault(event);	    
	    }
	} catch (Exception e) {
	    Window.alert("Error " + e.getMessage());
	}
    }

    public void setSelected(boolean selected) {
	if (selected) {
	    addStyleName("CBB-selectedSquare");
	} else {
	    removeStyleName("CBB-selectedSquare");
	}
    }

    public void setMoveable(boolean moveable) {
	if (moveable) {
	    m_ismoveable = true;
	    addStyleName("CBB-moveableSquare");
	} else {
	    m_ismoveable = false;
	    removeStyleName("CBB-moveableSquare");
	}
    }

    public final Square getNeighbor(int neighbor) {
	switch (neighbor) {
	    case TOP:
		return m_top;
	    case RIGHT:
		return m_right;
	    case BOTTOM:
		return m_bottom;
	    case LEFT:
		return m_left;
	    case TOPRIGHT:
		return (m_top != null) ? m_top.m_right : null;
	    case BOTTOMRIGHT:
		return (m_bottom != null) ? m_bottom.m_right : null;
	    case BOTTOMLEFT:
		return (m_bottom != null) ? m_bottom.m_left : null;
	    case TOPLEFT:
		return (m_top != null) ? m_top.m_left : null;
	    default:
		return null;
	}
    }
    
    public final void setLeft(Square square) {
	m_left = square;
    }

    public final void setRight(Square square) {
	m_right = square;
    }

    public final void setTop(Square square) {
	m_top = square;
    }

    public final void setBottom(Square square) {
	m_bottom = square;
    }

    static public final int TOP = 0;
    static public final int RIGHT = 1;
    static public final int BOTTOM = 2;
    static public final int LEFT = 3;
    static public final int TOPRIGHT = 4;
    static public final int BOTTOMRIGHT = 5;
    static public final int BOTTOMLEFT = 6;
    static public final int TOPLEFT = 7;
    
    public final boolean IsMoveable() {
	return m_ismoveable;
    }

    public final int getRank() {
	return m_rank;
    }

    public final char getFile() {
	return m_file;
    }

    public final int getColor() {
	return m_color;
    }

    public final String getName() {
	return m_file + Integer.toString(m_rank);
    }

    public final Board getBoard() {
	return m_board;
    }

    public final char getMapSymbol() {
	if (m_piece != null) {
	    return m_piece.getMapSymbol();
	} else {
	    return ' ';
	}
    }

    public final void displayMapSymbol(char symbol) {
	if ((m_displayPiece != null && m_displayPiece.getMapSymbol() == symbol) ||
	    (m_displayPiece == null && symbol == ' ')) {
	    return;
	}
	if (symbol == ' ') {
            setDisplayedPiece(null);
	} else {
	    setDisplayedPiece(Piece.createPieceFromSymbol(symbol, null));
	}
    }


    public final void displayCurrentPiece() {
        if (m_displayPiece != m_piece) {
            setDisplayedPiece(m_piece);
        }
    }
    
    
    private final int m_rank;

    private final char m_file;

    private final int m_color;
    
    // The board to which this square belongs
    private final Board m_board;
    
    // The piece that may potentially be in this square
    private Piece m_piece;

    // The widget associated with the piece
    private Widget m_pieceWidget;

    // The piece that is currently being displayed (might not be
    // the same as m_piece)
    private Piece m_displayPiece;

    // Square to the top
    private Square m_top;

    // Square to the right
    private Square m_right;

    // Square to the bottom
    private Square m_bottom;

    // Square to the left
    private Square m_left;

    // Marks whether this square is a legitimate move square for the
    // currently selected piece
    private boolean m_ismoveable;
}
