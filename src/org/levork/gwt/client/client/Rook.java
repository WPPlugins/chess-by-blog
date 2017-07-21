package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import java.util.ArrayList;
public class Rook extends Piece {
    Rook(int color, Square square) {
	super('R', color, square);
    }

    // Overrides piece move
    public void move(Square square) {
	String notation = notate(square);
	m_board.nextTurn(notation, new RookMoveCommand(square));
    }
    
    protected void getMoveList(ArrayList<Square> movelist) {
	Square square;
	for (int neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    square = m_square;
	    do {
		square = square.getNeighbor(neighbor);
		if (isValidMove(square)) {
		    movelist.add(square);
		}
	    } while (square != null && square.getPiece() == null);
	}
    }

    public boolean canMoveTo(Square target) {
	// Can't move to square if we're not on the same rank/file
	if (m_square.getRank() != target.getRank() &&
	    m_square.getFile() != target.getFile()) return false;
	Square square;
	for (int neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    square = m_square;
	    do {
		square = square.getNeighbor(neighbor);
		if (square == target) return isValidMove(target);
	    } while (square != null && square.getPiece() == null);
	}
	return false;
    }

    protected boolean threatens(Square square) {
	// Can't possibly threaten if we're not on the same rank/file
	if (m_square.getRank() != square.getRank() &&
	    m_square.getFile() != square.getFile()) return false;
	Square s;
	for (int neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    s = m_square;
	    do {
		s = s.getNeighbor(neighbor);
		if (s == square) return true;
	    } while (s != null && s.getPiece() == null);
	}
	return false;
    }

    // Exactly the same as PieceMoveCommand save that it clears the
    // castling flags
    protected final class RookMoveCommand implements Command {
	RookMoveCommand(Square square) {
	    m_toSquare = square;
	}
	public final void execute() {
            Square from = Rook.this.m_square;
	    m_board.clearCastleFlag(Rook.this.getColor(), Rook.this.m_square.getFile() - 'a');
	    Rook.this.m_square.setPiece(null);
	    // Kill the other piece if it exists
	    Piece piece = m_toSquare.getPiece();
	    if (piece != null) {
		m_board.removePiece(piece);
		piece.m_square = null;
	    }
            // Don't update the display immediately, let the animation
            // completion do that
	    m_toSquare.setPiece(Rook.this, false);
	    Rook.this.m_square = m_toSquare;
	    Rook.this.m_moved = true;
	    m_board.setLastMovedPiece(Rook.this);
            m_board.animatePiece(Rook.this, from, m_toSquare);
	}
	private final Square m_toSquare;
    }
}
