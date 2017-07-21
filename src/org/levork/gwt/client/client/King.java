package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import java.util.ArrayList;
public class King extends Piece {
    King(int color, Square square) {
	super('K', color, square);
    }

    // Overrides piece move
    public void move(Square square) {
	String notation = notate(square);
	m_board.nextTurn(notation, new KingMoveCommand(square));
    }
    
    public void castle(String notation, Piece rook, Square kingSquare, Square rookSquare) {
	// Generate notation suffix by temporarily moving both pieces
	// and seeing if move would lead to check, checkmate, or
	// stalemate.
	Square oldKingSquare = m_square, oldRookSquare = rook.getSquare();
	this.m_square.setPiece(null);
	kingSquare.setPiece(this);
	this.m_square = kingSquare;
	rook.m_square.setPiece(null);
	rookSquare.setPiece(rook);
	rook.m_square = rookSquare;
	
	// Check whether opponent king is under check
	boolean checked = m_board.isChecked(1 - m_color);
	// Check whether oppponent has any valid moves
	boolean hasValidMove = m_board.hasValidMove(1 - m_color);

	// Put us back
	this.m_square.setPiece(null);
	oldKingSquare.setPiece(this);
	this.m_square = oldKingSquare;
	rook.m_square.setPiece(null);
	oldRookSquare.setPiece(rook);
	rook.m_square = oldRookSquare;

	notation += checked ? (hasValidMove ? "+" : "++") : (hasValidMove ? "" : "=");
	m_board.nextTurn(notation, new CastleCommand(rook, kingSquare, rookSquare));
    }
    
    // Don't handle castling notation, since that's handled elsewhere.
    // And obviously there can't be two kings, so we don't need to
    // check for ambiguous moves
    protected String notate(final Square square, boolean withSuffix) {
	return m_symbol + (square.getPiece() == null ? "" : "x") + square.getName() + (withSuffix ? notateSuffix(square) : "");
    }
    
    protected void getMoveList(ArrayList<Square> movelist) {
	Square square;
	for (int neighbor = Square.TOP; neighbor <= Square.TOPLEFT; ++neighbor) {
	    square = m_square.getNeighbor(neighbor);
	    if (isValidMove(square)) {
		movelist.add(square);
	    }
	}
    }

    protected boolean threatens(Square square) {
	for (int neighbor = Square.TOP; neighbor <= Square.TOPLEFT; ++neighbor) {	
	    if (square == m_square.getNeighbor(neighbor)) return true;
	}
	return false;
    }

    private class CastleCommand implements Command {
	CastleCommand(Piece rook, Square kingSquare, Square rookSquare) {
	    m_kingSquare = kingSquare;
	    m_rookSquare = rookSquare;
	    m_rook = rook;
	}
	public final void execute() {
            Square kingFrom = King.this.m_square;
            Square rookFrom = m_rook.m_square;

	    King.this.m_square.setPiece(null);
	    m_kingSquare.setPiece(King.this, false);
	    King.this.m_square = m_kingSquare;
	    King.this.m_moved = true;
	    m_board.setLastMovedPiece(King.this);		
	    
	    m_rook.m_square.setPiece(null);
	    m_rookSquare.setPiece(m_rook, false);
	    m_rook.m_square = m_rookSquare;
	    m_rook.m_moved = true;

            m_board.animatePiece(King.this, kingFrom, m_kingSquare,
                                 m_rook, rookFrom, m_rookSquare, null);
	    m_board.clearBothCastleFlags(King.this.getColor());
	}
	private final Square m_kingSquare, m_rookSquare;
	private final Piece m_rook;
    }

    // Exactly the same as PieceMoveCommand save that it clears the
    // castling flags
    protected final class KingMoveCommand implements Command {
	KingMoveCommand(Square square) {
	    m_toSquare = square;
	}
	public final void execute() {
            Square from = King.this.m_square;
	    King.this.m_square.setPiece(null);
	    // Kill the other piece if it exists
	    Piece piece = m_toSquare.getPiece();
	    if (piece != null) {
		m_board.removePiece(piece);
		piece.m_square = null;
	    }
            // Don't update the display immediately, let the animation
            // completion do that
	    m_toSquare.setPiece(King.this, false);
	    King.this.m_square = m_toSquare;
	    King.this.m_moved = true;
	    m_board.setLastMovedPiece(King.this);
            m_board.animatePiece(King.this, from, m_toSquare);
	    m_board.clearBothCastleFlags(King.this.getColor());
	}
	private final Square m_toSquare;
    }    
}
