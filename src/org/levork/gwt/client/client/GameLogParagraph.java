package org.levork.gwt.client.client;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
import java.util.ArrayList;
import java.util.Iterator;
public class GameLogParagraph extends FlowPanel
{
    public GameLogParagraph(Board board, final String whiteLastName, final String blackLastName) {
        super();
        m_board = board;
        m_whiteLastName = whiteLastName;
        m_blackLastName = blackLastName;
        m_selectedPly = -1;
	m_plyCount = 1;
        m_movelist = new ArrayList<String>();
	m_boardmaplist = new ArrayList<String>();
	m_commentlist = new ArrayList<String>();
	m_labellist = new ArrayList<Label>();
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
	for (Label label : m_labellist) {
	    label.removeStyleName("CBB-gameparagraph-selected-entry");
	}
	if (m_movelist.size() > 1) {
	    Label label = m_labellist.get(m_movelist.size() - 2);
	    if (label != null) {
		label.addStyleName("CBB-gameparagraph-selected-entry");
	    }
	}
    }
    
    private void showMapAtPly(int ply, boolean singlestep, boolean forward) {
	if (ply == m_movelist.size() - 1) {
	    showCurrentPosition(singlestep);
	} else {
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
	for (Label label : m_labellist) {
	    label.removeStyleName("CBB-gameparagraph-selected-entry");
	}
	if (ply > 0) {
	    Label label = m_labellist.get(ply - 1);
	    label.addStyleName("CBB-gameparagraph-selected-entry");
	}
    }
    
    public void addComment(String comment) {
	if (m_commentlist.size() > 0) {
	    String oldcomment = m_commentlist.get(m_commentlist.size() - 1);
	    if (oldcomment.equals("")) {
		m_commentlist.set(m_commentlist.size() - 1, comment);
	    } else {
		m_commentlist.set(m_commentlist.size() - 1, oldcomment + " " + comment);
	    }
	    if (m_commentlist.size() > 1 && m_labellist.size() >= m_commentlist.size() - 1) {
		Label label = m_labellist.get(m_commentlist.size() - 2);
		if (label != null) {
		    label.addStyleName("CBB-gameparagraph-entry-with-comment");
		}
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
	if (color == Piece.WHITE) {
	    notation = Integer.toString(turn) + "." + notation + " ";
	} else {
	    notation = notation + " ";
	}
	if (turn != 0) {
	    final boolean hasComment = m_commentlist.get(m_movelist.size() - 1) != "";
	    final int currentPlyCount = m_plyCount;
	    Label newlabel = new Label();
	    notation = notation.replace(" ", "&nbsp;");	    
	    DOM.setInnerHTML(newlabel.getElement(), notation);
	    
	    newlabel.addClickHandler(new ClickHandler() {
		    public void onClick(ClickEvent event) {
                        int diff = currentPlyCount - GameLogParagraph.this.m_selectedPly;
			GameLogParagraph.this.showMapAtPly(currentPlyCount, diff == 1 || diff == -1, diff > 0);
			GameLogParagraph.this.m_selectedPly = currentPlyCount;
		    }
		});
	    m_plyCount++;
	    add(newlabel);
	    m_labellist.add(newlabel);

	    newlabel.setStylePrimaryName("CBB-gameparagraph-entry");
	    if (hasComment) {
		newlabel.addStyleName("CBB-gameparagraph-entry-with-comment");
	    }

            m_selectedPly = m_boardmaplist.size() - 1;
            for (Label label : m_labellist) {
                label.removeStyleName("CBB-gameparagraph-selected-entry");
            }
            Label label = m_labellist.get(m_selectedPly - 1);
            label.addStyleName("CBB-gameparagraph-selected-entry");
            
	}
    }

    public void start() {
	setPly(0);
    }

    public void rewind() {
	if (m_selectedPly == 0) return;
	if (m_selectedPly == -1) {
	    m_selectedPly = m_boardmaplist.size() - 1;
	} else {
	    m_selectedPly--;
	    showMapAtPly(m_selectedPly, true, false);
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
	}
    }

    public void end() {
	setPly(m_boardmaplist.size() - 1);
    }

    public void setPly(int ply) {
        if (ply >= 0 && ply < m_boardmaplist.size()) {
            int diff = ply - m_selectedPly;
            boolean forward = (diff > 0);
            m_selectedPly = ply;
	    showMapAtPly(m_selectedPly, diff == 1 || diff == -1, forward);
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

    private ArrayList<Label> m_labellist;
    
    int m_selectedPly;
    int m_plyCount;
}
