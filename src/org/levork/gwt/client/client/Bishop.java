package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import java.util.ArrayList;
public class Bishop extends Piece {
    Bishop(int color, Square square) {
	super('B', color, square);
    }

    protected void getMoveList(ArrayList<Square> movelist) {
	Square square;
	for (int neighbor = Square.TOPRIGHT; neighbor <= Square.TOPLEFT; ++neighbor) {
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
	// Can't move to a square of a different color
	if (target.getColor() != m_square.getColor()) return false;
	Square square;
	for (int neighbor = Square.TOPRIGHT; neighbor <= Square.TOPLEFT; ++neighbor) {
	    square = m_square;
	    do {
		square = square.getNeighbor(neighbor);
		if (square == target) return isValidMove(target);
	    } while (square != null && square.getPiece() == null);
	}
	return false;
    }

    
    protected boolean threatens(Square square) {
	// Can't threaten a square of a different color
	if (m_square.getColor() != square.getColor()) return false;
	Square s;
	for (int neighbor = Square.TOPRIGHT; neighbor <= Square.TOPLEFT; ++neighbor) {
	    s = m_square;
	    do {
		s = s.getNeighbor(neighbor);
		if (s == square) return true;
	    } while (s != null && s.getPiece() == null);
	}
	return false;
    }
}
