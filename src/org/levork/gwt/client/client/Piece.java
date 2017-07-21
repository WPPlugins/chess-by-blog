package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import java.util.Iterator;
import java.util.ArrayList;
public abstract class Piece implements ClickHandler {

    protected Piece(char symbol, int color, Square square) {
	m_color = color;
	m_symbol = symbol;
	m_square = square;
	m_moved = false;
	if (m_square != null) {
	    m_square.setPiece(this);
	    m_board = m_square.getBoard();
	} else {
	    m_board = null;
	}
    }
    
    // ClickListener methods
    public void onClick(ClickEvent event) {
	if (event.getSource() == m_square) {
	    m_board.selectPiece(this);
	}
    }

    public void clickFromSquare(Square square) {
        if (square == m_square) {
            m_board.selectPiece(this);
        }
    }
    
    // Considers whether the piece can move to the indicated square
    // based on own piece occupation and enemy check considerations
    public final boolean isValidMove(Square square) {

	if (m_square == square) return false;
	if (square == null) return false;

	final Piece oldPiece = square.getPiece();
	if (oldPiece != null && oldPiece.m_color == m_color) return false;

	// Temporarily move ourselves without updating
	final Square oldSquare = m_square;
	oldSquare.setPiece(null, false);
	// Make sure we save the piece we may potentially be
	// clobbering
	if (oldPiece != null) {
	    m_board.removePiece(oldPiece);
	}
	square.setPiece(this, false);
	m_square = square;

	// Check whether our king is under check
	boolean checked = m_board.isChecked(m_color);
	
	// Put us back to where we were
	square.setPiece(oldPiece, false);
	if (oldPiece != null) {
	    m_board.addPiece(oldPiece);
	}
	oldSquare.setPiece(this, false);
	m_square = oldSquare;

	return !checked;
    }

    // Moves to another square, capturing the piece on that square if
    // one exists.
    public void move(Square square) {
	String notation = notate(square);
	m_board.nextTurn(notation, new PieceMoveCommand(square));
    }

    protected String notateSuffix(final Square square) {
	// Temporarily move our pieces, see if this move would lead to
	// check, checkmate, or stalemate for the opposing player
	Square oldSquare = m_square;	
	oldSquare.setPiece(null, false);
	Piece oldPiece = square.getPiece();
	if (oldPiece != null) {
	    m_board.removePiece(oldPiece);
	}
	square.setPiece(this, false);
	m_square = square;

	// Check whether opponent king is under check
	boolean checked = m_board.isChecked(1 - m_color);
	// Check whether oppponent has any valid moves
	boolean hasValidMove = m_board.hasValidMove(1 - m_color);
	
	// Put us back to where we were
	square.setPiece(oldPiece, false);
	if (oldPiece != null) {
	    m_board.addPiece(oldPiece);
	}
	oldSquare.setPiece(this, false);
	m_square = oldSquare;
	return checked ? (hasValidMove ? "+" : "++") : (hasValidMove ? "" : "=");
    }

    // Returns a string indicating the algebraic notation for moving
    // to the square. The default implementation suffices for most
    // pieces
    protected String notate(final Square square) {
	return notate(square, true);
    }
	
    // Returns a string indicating the algebraic notation for moving
    // to the square. The default implementation suffices for most
    // pieces
    protected String notate(final Square square, boolean withSuffix) {
	String suffix = withSuffix ? notateSuffix(square) : "";
	for (Iterator i = m_board.getColorPiecesIterator(m_color); i.hasNext(); ) {
	    Piece piece = (Piece) i.next();
	    if (piece != this && piece.m_symbol == m_symbol) {
		if (piece.canMoveTo(square)) {
		    // If other piece is on the same rank, or rank and
		    // file are different, notation includes file
		    if (piece.m_square.getRank() == m_square.getRank() ||
			(piece.m_square.getRank() != m_square.getRank() &&
			 piece.m_square.getFile() != m_square.getFile())) {
			if (square.getPiece() == null) {
			    return Character.toString(m_symbol) + m_square.getFile() + square.getName() + suffix;
			} else {
			    return Character.toString(m_symbol) + m_square.getFile() + "x" + square.getName() + suffix;
			}
		    } else {
			// Pieces are on the same file, so notation
			// includes rank
			if (square.getPiece() == null) {
			    return Character.toString(m_symbol) + m_square.getRank() + square.getName() + suffix;
			} else {
			    return Character.toString(m_symbol) + m_square.getRank() + "x" + square.getName() + suffix;
			}
		    }
		}
	    }
	}
	return m_symbol + (square.getPiece() == null ? "" : "x") + square.getName() + suffix;
    }

    // Fills in the valid movelist for the piece
    protected abstract void getMoveList(ArrayList<Square> movelist);

    // Returns true if the piece can move to the given square. Default
    // implementation just uses a generated movelist
    public boolean canMoveTo(Square square) {
	ArrayList<Square> movelist = new ArrayList<Square>();
	getMoveList(movelist);
	return movelist.contains(square);
    }
    
    // Returns true if the piece can attack the indicated square,
    // false otherwise
    protected abstract boolean threatens(Square square);

    public final int getColor() {
	return m_color;
    }

    public final Square getSquare() {
	return m_square;
    }

    public final char getSymbol() {
	return m_symbol;
    }

    public final char getMapSymbol() {
	if (m_color == WHITE) {
	    return Character.toUpperCase(m_symbol);
	} else {
	    return Character.toLowerCase(m_symbol);
	}
    }
    
    public final String getStyleName() {
	return "CBB-" + s_colorNames[m_color] + m_symbol;
    }

    // The square to which the piece belongs
    protected Square m_square;

    // The board to which this piece belongs
    protected final Board m_board;
    
    // Symbol for this piece
    protected final char m_symbol;

    // Color for this piece
    protected final int m_color;

    // Whether this piece has moved at all during this game
    protected boolean m_moved;

    static public final int WHITE = 0;
    static public final int BLACK = 1;
    static public final String[] s_colorNames = {"white", "black"};

    // The Command that actually implements piece moving
    protected final class PieceMoveCommand implements Command {
	PieceMoveCommand(Square square) {
	    m_toSquare = square;
	}
	public final void execute() {
            Square from = Piece.this.m_square;
	    Piece.this.m_square.setPiece(null);
	    // Kill the other piece if it exists
	    Piece piece = m_toSquare.getPiece();
	    if (piece != null) {
		m_board.removePiece(piece);
		piece.m_square = null;
	    }
            // Don't update the display immediately, let the animation
            // completion do that
	    m_toSquare.setPiece(Piece.this, false);
	    Piece.this.m_square = m_toSquare;
	    Piece.this.m_moved = true;
	    m_board.setLastMovedPiece(Piece.this); 
            m_board.animatePiece(Piece.this, from, m_toSquare);
	}
	private final Square m_toSquare;
    }

    static final Piece createPieceFromSymbol(char symbol, Square square) {
	Piece p = null;
	int color;
	if (Character.isLowerCase(symbol)) {
	    color = BLACK;
	} else {
	    color = WHITE;
	}
	switch(Character.toLowerCase(symbol)) {
	    case 'p':
		p = new Pawn(color, square);
		break;
	    case 'n':
		p = new Knight(color, square);
		break;
	    case 'b':
		p = new Bishop(color, square);
		break;
	    case 'a':
		p = new Archbishop(color, square);
		break;
	    case 'r':
		p = new Rook(color, square);
		break;
	    case 'c':
		p = new Chancellor(color, square);
		break;
	    case 'q':
		p = new Queen(color, square);
		break;
	    case 'k':
		p = new King(color, square);
		break;
	    default:
		p = null;
		break;
	}
	return p;
    }
}
