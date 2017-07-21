package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import java.util.ArrayList;
public class Pawn extends Piece {
    Pawn(int color, Square square) {
	super('P', color, square);
	m_enPassantCapturable = false;	
    }

    // Override for move, to handle the special case of en passant and
    // promotion.
    public void move(Square square) {
	if (square.getRank() == m_board.getRanks() || square.getRank() == 1) {
	    // Non interactive moves shouldn't get to this code,
	    // unless the PGN was botched, and was missing the
	    // promotion notation.
	    new PromotionDialog(square).show();
	} 
	// If we moved diagonally into an empty square, it must be
	// en passant
	else if (square.getPiece() == null && square.getFile() != m_square.getFile()) {
	    String notation = notate(square);
	    m_board.nextTurn(notation, new EnPassantMoveCommand(square));
	    m_enPassantCapturable = false;
	} else {
	    // We can only be captured by en passant only if we moved
	    // two squares
	    m_enPassantCapturable = (square.getRank() - m_square.getRank() == 2 || m_square.getRank() - square.getRank() == 2);
	    super.move(square);
	}
    }

    // Special case of movement which handles pawn promotion
    public void promote(Square square, char promoteTo) {
	String notation = notate(square, false) + "=" + promoteTo;
	// Computing the notation suffix for a promotion must use the
	// attacking power of the newly promoted piece
	Square oldSquare = m_square;	
	oldSquare.setPiece(null, false);
	Piece oldPiece = square.getPiece();
	if (oldPiece != null) {
	    m_board.removePiece(oldPiece);
	}
	Piece promotedPiece = promotePawn(promoteTo, square);
        square.setDisplayedPiece(oldPiece);
	m_board.removePiece(this);
	m_board.addPiece(promotedPiece);
	// Check whether opponent king is under check
	boolean checked = m_board.isChecked(1 - m_color);
	// Check whether oppponent has any valid moves
	boolean hasValidMove = m_board.hasValidMove(1 - m_color);
	// Put us back to where we were
	m_board.addPiece(this);
	m_board.removePiece(promotedPiece);
	promotedPiece.m_square = null;
	square.setPiece(oldPiece, false);
	if (oldPiece != null) {
	    m_board.addPiece(oldPiece);
	}
	oldSquare.setPiece(this, false);
	m_square = oldSquare;
	notation += checked ? (hasValidMove ? "+" : "++") : (hasValidMove ? "" : "=");
	m_board.nextTurn(notation, new PromotionMoveCommand(square, promoteTo));
    }

    // Override to handle special case of en passant. Promotion is
    // handled separately
    protected String notateSuffix(final Square square) {
	if (square.getPiece() == null && square.getFile() != m_square.getFile()) {
	    // Temporarily move both pawns, see if this move would
	    // lead to check, checkmate, or stalemate for the opposing
	    // player
	    final Square oldSquare = m_square;
	    final Square passedSquare = square.getNeighbor(m_color == Piece.WHITE ? Square.BOTTOM : Square.TOP);
	    Piece pawn = passedSquare.getPiece();
	    if (pawn == null) return ""; // Something wrong going on here..
	    // Remove and save captured pawn
	    passedSquare.setPiece(null, false);
	    m_board.removePiece(pawn);
	    // Move our pawn
	    m_square.setPiece(null, false);
	    m_square = square;
	    m_square.setPiece(this, false);
	    // Check whether opponent king is under check
	    boolean checked = m_board.isChecked(1 - m_color);
	    // Check whether oppponent has any valid moves
	    boolean hasValidMove = m_board.hasValidMove(1 - m_color);
	    // Move our pawn back
	    m_square.setPiece(null, false);
	    m_square = oldSquare;
	    m_square.setPiece(this, false);
	    // Restore captured pawn
	    m_board.addPiece(pawn);
	    passedSquare.setPiece(pawn, false);
	    return checked ? (hasValidMove ? "+" : "++") : (hasValidMove ? "" : "=");
	} else {
	    return super.notateSuffix(square);
	}
    }

    protected String notate(final Square square, boolean withSuffix) {
	String suffix = withSuffix ? notateSuffix(square) : "";	
	if (square.getPiece() == null) {
	    if ((square.getFile() != m_square.getFile())) {
		return m_square.getFile() + "x" + square.getName() + " e.p." + suffix;	    
	    } else {
		return square.getName() + suffix;
	    }
	} else {
	    return m_square.getFile() + "x" + square.getName() + suffix;
	}
    }
    
    protected void getMoveList(ArrayList<Square> movelist) {
	Square square;
	final int forward = (m_color == WHITE) ? Square.TOP : Square.BOTTOM;
	final int forwardleft = (m_color == WHITE) ? Square.TOPLEFT : Square.BOTTOMRIGHT;
	final int left = (m_color == WHITE) ? Square.LEFT : Square.RIGHT;
	final int right = (m_color == WHITE) ? Square.RIGHT : Square.LEFT;
	final int forwardright = (m_color == WHITE) ? Square.TOPRIGHT : Square.BOTTOMLEFT;
	final int enpassantrank = (m_color == WHITE) ? m_board.getRanks() - 3 : 4;
	square = m_square.getNeighbor(forward);
	if (square != null) {
	    // Special check for moving forward - since we can't
	    // attack pieces in front of us, we have to check that
	    // the square is explicitly not occupied
	    Piece piece = square.getPiece();
	    if (piece == null) {
		if (isValidMove(square)) {
		    movelist.add(square);
		}
		// Check for initial two-step move	    
		if (!m_moved) {
		    square = square.getNeighbor(forward);
		    if (square != null) {
			piece = square.getPiece();
			if (piece == null) {
			    if (isValidMove(square)) {
				movelist.add(square);
			    }
			}
		    }
		}
	    }
	    // Moving diagonally also handled specially, since we
	    // can only move there if the pieces are occupied
	    square = m_square.getNeighbor(forwardleft);
	    if (square != null && square.getPiece() != null && isValidMove(square)) {
		movelist.add(square);
	    }
	    square = m_square.getNeighbor(forwardright);
	    if (square != null && square.getPiece() != null && isValidMove(square)) {
		movelist.add(square);
	    }
	    // Handle en passant. isValidMove is insufficient, so we
	    // must do our own test for check locally
	    if (m_square.getRank() == enpassantrank) {
		if ((square = m_square.getNeighbor(left)) != null) {
		    Piece pawn = square.getPiece();
		    if ((pawn != null) && (pawn == m_board.getLastMovedPiece()) && (pawn instanceof Pawn) && ((Pawn) pawn).isEnPassantCapturable()) {
			// Temporarily move ourselves without updating
			final Square oldSquare = m_square;
			// Remove and save captured pawn
			square.setPiece(null, false);
			m_board.removePiece(pawn);
			// Move our pawn
			m_square.setPiece(null, false);
			m_square = m_square.getNeighbor(forwardleft);
			m_square.setPiece(this, false);			
			// Check whether our king is under check
			boolean checked = m_board.isChecked(m_color);
			// Move our pawn back
			m_square.setPiece(null, false);
			m_square = oldSquare;
			m_square.setPiece(this, false);
			// Restore captured pawn
			m_board.addPiece(pawn);
			square.setPiece(pawn, false);
			if (!checked) {
			    movelist.add(m_square.getNeighbor(forwardleft));
			}
		    }
		}
		if ((square = m_square.getNeighbor(right)) != null) {
		    Piece pawn = square.getPiece();
		    if ((pawn != null) && (pawn == m_board.getLastMovedPiece()) && (pawn instanceof Pawn) && ((Pawn) pawn).isEnPassantCapturable()) {
			// Temporarily move ourselves without updating
			final Square oldSquare = m_square;
			// Remove and save captured pawn
			square.setPiece(null, false);
			m_board.removePiece(pawn);
			// Move our pawn
			m_square.setPiece(null, false);
			m_square = m_square.getNeighbor(forwardright);
			m_square.setPiece(this, false);			
			// Check whether our king is under check
			boolean checked = m_board.isChecked(m_color);
			// Move our pawn back
			m_square.setPiece(null, false);
			m_square = oldSquare;
			m_square.setPiece(this, false);
			// Restore captured pawn
			m_board.addPiece(pawn);
			square.setPiece(pawn, false);
			if (!checked) {
			    movelist.add(m_square.getNeighbor(forwardright));
			}
		    }
		}
	    }
	}
    }

    // Since this is used for the purposes of determining check, we
    // don't have to worry about en passant (can't attack the King
    // that way)
    protected boolean threatens(Square square) {
	if (m_color == WHITE) {
	    return square == m_square.getNeighbor(Square.TOPLEFT) ||
		square == m_square.getNeighbor(Square.TOPRIGHT);
	} else {
	    return square == m_square.getNeighbor(Square.BOTTOMLEFT) ||
		square == m_square.getNeighbor(Square.BOTTOMRIGHT);
	}
    }

    protected final class EnPassantMoveCommand implements Command {
	EnPassantMoveCommand(Square square) {
	    m_toSquare = square;
	}
	public final void execute() {
            Square from = Pawn.this.m_square;
	    Pawn.this.m_square.setPiece(null);
            
	    // Kill the other pawn
	    final Square passedSquare = m_toSquare.getNeighbor(m_color == WHITE ? Square.BOTTOM : Square.TOP);
	    passedSquare.getPiece().m_square = null;
	    m_board.removePiece(passedSquare.getPiece());

            // Don't update the display immediately, let the animation
            // completion do that
            passedSquare.setPiece(null, false);            
	    m_toSquare.setPiece(Pawn.this, false);
	    Pawn.this.m_square = m_toSquare;
	    Pawn.this.m_moved = true;
	    Pawn.this.m_board.setLastMovedPiece(Pawn.this);
            Pawn.this.m_board.animatePiece(Pawn.this, from, m_toSquare, new Command() {
                    public final void execute() {
                        passedSquare.setDisplayedPiece(null);
                    }
                });
	}
	private final Square m_toSquare;
    }

    protected final class PromotionMoveCommand implements Command {
	PromotionMoveCommand(Square square, char promoteTo) {
	    m_toSquare = square;
	    m_promoteTo = promoteTo;
	}
	public final void execute() {
            Square from = Pawn.this.m_square;            
	    Pawn.this.m_square.setPiece(null);
	    // Kill the other piece if it exists
	    Piece piece = m_toSquare.getPiece();
	    if (piece != null) {
		m_board.removePiece(piece);
	    }
	    m_board.removePiece(Pawn.this);
	    final Piece newpiece = promotePawn(m_promoteTo, m_toSquare);
            m_toSquare.setDisplayedPiece(piece);
	    m_board.addPiece(newpiece);
	    newpiece.m_moved = true;
	    m_board.setLastMovedPiece(newpiece);
            Pawn.this.m_board.animatePiece(Pawn.this, from, m_toSquare, new Command() {
                    public final void execute() {
                        m_toSquare.setDisplayedPiece(newpiece);
                    }
                });
	}
	private final Square m_toSquare;
	private final char m_promoteTo;
    }

    private Piece promotePawn(char promoteTo, Square square) {
	switch(promoteTo) {
	    case 'N':
		return new Knight(m_color, square);
	    case 'B':
		return new Bishop(m_color, square);
	    case 'R':
		return new Rook(m_color, square);
	    case 'Q':
		return new Queen(m_color, square);
	    case 'A':
		return new Archbishop(m_color, square);
	    case 'C':
		return new Chancellor(m_color, square);
	    default:
		return null;
	}
    }

    public class PromotionDialog extends CenteredDialogBox {

	public PromotionDialog(Square toSquare) {
	    m_toSquare = toSquare;
	    VerticalPanel vpanel = new VerticalPanel ();
	    vpanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	    vpanel.setStyleName("CBB-dialog");

	    vpanel.add(new Label("Select piece for pawn promotion:"));
	    HorizontalPanel hpanel = new HorizontalPanel();
	    m_knight = new RadioButton("promotion", "Knight");
	    m_bishop = new RadioButton("promotion", "Bishop");
	    m_rook = new RadioButton("promotion", "Rook");
	    m_queen = new RadioButton("promotion", "Queen");
	    m_queen.setValue(true);
	    hpanel.add(m_knight);
	    hpanel.add(m_bishop);
	    hpanel.add(m_rook);

	    // Slight kludge: assume that if the board has at least 10
	    // files, archbishops and chancellors are a viable option
	    if (Pawn.this.m_board.getFiles() >= 10) {
		m_archbishop = new RadioButton("promotion", "Archbishop");
		m_chancellor = new RadioButton("promotion", "Chancellor");
		hpanel.add(m_archbishop);
		hpanel.add(m_chancellor);
	    }
	    hpanel.add(m_queen);
	    vpanel.add(hpanel);
	    Button ok = new Button("OK", new ClickHandler() {
		public void onClick(ClickEvent event) {
		    PromotionDialog.this.hide();
		    if (Pawn.this.m_board.getFiles() >= 10) {
			Pawn.this.promote(PromotionDialog.this.m_toSquare, PromotionDialog.this.m_queen.getValue() ? 'Q' : (PromotionDialog.this.m_archbishop.getValue() ? 'A' : (PromotionDialog.this.m_chancellor.getValue() ? 'C' : (PromotionDialog.this.m_rook.getValue() ? 'R' : (PromotionDialog.this.m_bishop.getValue() ? 'B' : 'N')))));
		    } else {
			Pawn.this.promote(PromotionDialog.this.m_toSquare, PromotionDialog.this.m_queen.getValue() ? 'Q' : (PromotionDialog.this.m_rook.getValue() ? 'R' : (PromotionDialog.this.m_bishop.getValue() ? 'B' : 'N')));
		    }
		}
	    });
	    setText("Pawn promotion");
	    vpanel.add(ok);
	    setWidget(vpanel);
	}

	private final Square m_toSquare;
	RadioButton m_bishop, m_knight, m_rook, m_archbishop, m_chancellor, m_queen;
    }

    public boolean isEnPassantCapturable() {
	return m_enPassantCapturable;
    }

    public void setEnPassantCapturable(boolean capture) {
	m_enPassantCapturable = capture;
    }

    private boolean m_enPassantCapturable;
}
