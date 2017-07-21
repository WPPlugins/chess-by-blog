package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import java.util.ArrayList;
public class Knight extends Piece {
    Knight(int color, Square square) {
	super('N', color, square);
    }

    protected void getMoveList(ArrayList<Square> movelist) {
	Square a, b;
	for (int neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
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
	}
    }

    public boolean canMoveTo(Square target) {
	// Can't move to a square of the same color
	if (target.getColor() == m_square.getColor()) return false;
	Square a, b;
	for (int neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    if (m_square.getNeighbor(neighbor) != null) {
		a = m_square.getNeighbor(neighbor).getNeighbor(neighbor);
		if (a != null) {
		    if (target == a.getNeighbor((neighbor + 3) % 4)) return isValidMove(target);
		    if (target == a.getNeighbor((neighbor + 1) % 4)) return isValidMove(target);
		}
	    }
	}
	return false;
    }
    
    protected boolean threatens(Square square) {
	// Can't threaten a square of the same color
	if (m_square.getColor() == square.getColor()) return false;
	Square a, b;
	for (int neighbor = Square.TOP; neighbor <= Square.LEFT; ++neighbor) {
	    if (m_square.getNeighbor(neighbor) != null) {
		a = m_square.getNeighbor(neighbor).getNeighbor(neighbor);
		if (a != null) {
		    if (square == a.getNeighbor((neighbor + 3) % 4)) return true;
		    if (square == a.getNeighbor((neighbor + 1) % 4)) return true;
		}
	    }
	}
	return false;
    }
}
