package org.levork.gwt.client.client;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.animation.client.Animation;

public class PieceAnimation extends Animation {

    public PieceAnimation(AbsolutePanel panel) {
    	m_basePanel = panel;
	m_animPanel = new SimplePanel();
        m_animPanel.setVisible(false);
	m_animPanel.setStyleName("CBB-square");
	m_animPanel.addStyleName("CBB-blankSquare");
        m_animPanel.setVisible(false);
        DOM.setStyleAttribute(m_animPanel.getElement(), "position", "absolute");
	m_secondAnimPanel = new SimplePanel();
        m_secondAnimPanel.setVisible(false);
	m_secondAnimPanel.setStyleName("CBB-square");
	m_secondAnimPanel.addStyleName("CBB-blankSquare");
        m_secondAnimPanel.setVisible(false);        
        DOM.setStyleAttribute(m_secondAnimPanel.getElement(), "position", "absolute");

	m_basePanel.add(m_animPanel, 0, 0);        
	m_basePanel.add(m_secondAnimPanel, 0, 0);
    }

    public void animate(Piece piece, Square from, Square to) {
        animate(piece, from, to, null, null, null, null);
    }

    public void animate(Piece piece, Square from, Square to, Command postcommand) {
        animate(piece, from, to, null, null, null, postcommand);
    }
    

    public void animate(Piece piece, Square from, Square to,
                        Piece secondPiece, Square secondFrom, Square secondTo,
                        Command postcommand) {
        cancel();
        m_postCommand = postcommand;
        m_piece = piece;
        m_fromSquare = from;
        m_toSquare = to;
        m_secondPiece = secondPiece;
        m_secondFromSquare = secondFrom;
        m_secondToSquare = secondTo;
        m_animPanel.addStyleName(m_piece.getStyleName());
        if (m_secondPiece != null) {
            m_secondAnimPanel.addStyleName(m_secondPiece.getStyleName());
        }

        // Start the animation
        run(500);
    }
    
    
    public void onStart() {
        int baseleft = m_basePanel.getAbsoluteLeft();
        int basetop = m_basePanel.getAbsoluteTop();
        m_fromleft = m_fromSquare.getAbsoluteLeft() - baseleft;
        m_toleft = m_toSquare.getAbsoluteLeft() - baseleft;
        m_fromtop = m_fromSquare.getAbsoluteTop() - basetop;
        m_totop = m_toSquare.getAbsoluteTop() - basetop;
        DOM.setStyleAttribute(m_animPanel.getElement(), "left", m_fromleft + "px");
        DOM.setStyleAttribute(m_animPanel.getElement(), "top", m_fromtop + "px");
        m_animPanel.setVisible(true);
        if (m_secondPiece != null) {
            m_fromleft2 = m_secondFromSquare.getAbsoluteLeft() - baseleft;
            m_toleft2 = m_secondToSquare.getAbsoluteLeft() - baseleft;
            m_fromtop2 = m_secondFromSquare.getAbsoluteTop() - basetop;
            m_totop2 = m_secondToSquare.getAbsoluteTop() - basetop;
            DOM.setStyleAttribute(m_secondAnimPanel.getElement(), "left", m_fromleft2 + "px");
            DOM.setStyleAttribute(m_secondAnimPanel.getElement(), "top", m_fromtop2 + "px");
            m_secondAnimPanel.setVisible(true);
        }
    }
    
    public void onUpdate(double progress) {
        double ip = interpolate(progress);
        if (ip < 0.0) ip = 0.0;
        if (ip > 1.0) ip = 1.0;
        DOM.setStyleAttribute(m_animPanel.getElement(), "left", m_fromleft + (int) (ip * (m_toleft - m_fromleft)) + "px");
        DOM.setStyleAttribute(m_animPanel.getElement(), "top", m_fromtop + (int) (ip * (m_totop - m_fromtop)) + "px");
        if (m_secondPiece != null) {
            DOM.setStyleAttribute(m_secondAnimPanel.getElement(), "left", m_fromleft2 + (int) (ip * (m_toleft2 - m_fromleft2)) + "px");
            DOM.setStyleAttribute(m_secondAnimPanel.getElement(), "top", m_fromtop2 + (int) (ip * (m_totop2 - m_fromtop2)) + "px");
        }
    }

    // The default implementation does not call onComplete if the animation hadn't
    // started. This won't be sufficient for us, we want to *always* call
    // onComplete
    public void onCancel() {
    	onComplete();
    }

    public void onComplete() {
        m_animPanel.setVisible(false);
        if (m_piece != null) {
            m_animPanel.removeStyleName(m_piece.getStyleName());
            if (m_toSquare != null) {
	        m_toSquare.setDisplayedPiece(m_piece);
	        m_toSquare = null;
	    }
	    m_piece = null;
	}
        if (m_secondPiece != null) {
            m_secondAnimPanel.setVisible(false);
            m_secondAnimPanel.removeStyleName(m_secondPiece.getStyleName());
            m_secondToSquare.setDisplayedPiece(m_secondPiece);
            m_secondPiece = null;
        }
        if (m_postCommand != null) {
            m_postCommand.execute();
            m_postCommand = null;
        }
    }

    private Piece m_piece;
    private Square m_fromSquare;
    private Square m_toSquare;
    private Piece m_secondPiece;
    private Square m_secondFromSquare;
    private Square m_secondToSquare;
    private SimplePanel m_animPanel;
    private SimplePanel m_secondAnimPanel;
    private Command m_postCommand;
    private AbsolutePanel m_basePanel;
    int m_fromleft, m_fromtop, m_toleft, m_totop;
    int m_fromleft2, m_fromtop2, m_toleft2, m_totop2;
}    
