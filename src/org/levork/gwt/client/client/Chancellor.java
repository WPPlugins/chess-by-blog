package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import java.util.ArrayList;
public class Chancellor extends Piece {
    Chancellor(int color, Square square) {
	super('C', color, square);
    }

    protected void getMoveList(ArrayList<Square> movelist) {
	Square square, a, b;
	int neighbor;
	for (neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    if (m_square.getNeighbor(neighbor) != null) {
		a = m_square.getNeighbor(neighbor).getNeighbor(neighbor);
		if (a != null) {
		    if (isValidMove(b = a.getNeighbor((neighbor + 3) % 4))) {
			movelist.add(b);
		    }
		    if (isValidMove(b = a.getNeighbor((neighbor + 1) % 4))) {
			movelist.add(b);
		    }
		}
	    }
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
	Square square, a, b;
	int neighbor;
	for (neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    if (m_square.getNeighbor(neighbor) != null) {
		a = m_square.getNeighbor(neighbor).getNeighbor(neighbor);
		if (a != null) {
		    if (target == a.getNeighbor((neighbor + 3) % 4)) return isValidMove(target);
		    if (target == a.getNeighbor((neighbor + 1) % 4)) return isValidMove(target);
		}
	    }
	    square = m_square;
	    do {
		square = square.getNeighbor(neighbor);
		if (square == target) return isValidMove(target);
	    } while (square != null && square.getPiece() == null);
	}
	return false;
    }

    
    protected boolean threatens(Square square) {
	Square s, a, b;
	int neighbor;
	for (neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    if (m_square.getNeighbor(neighbor) != null) {
		a = m_square.getNeighbor(neighbor).getNeighbor(neighbor);
		if (a != null) {
		    if (square == a.getNeighbor((neighbor + 3) % 4)) return true;
		    if (square == a.getNeighbor((neighbor + 1) % 4)) return true;
		}
	    }
	    s = m_square;
	    do {
		s = s.getNeighbor(neighbor);
		if (s == square) return true;
	    } while (s != null && s.getPiece() == null);
	}
	return false;
    }
}
