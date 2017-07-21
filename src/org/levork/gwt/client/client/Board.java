package org.levork.gwt.client.client;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.http.client.URL;
import java.util.Iterator;
import java.util.ArrayList;

public class Board extends SimplePanel {

    public class GameTimer extends Timer {
        public void run() {
            if (m_posthash != null) {
                doCheckUpdate(m_updateURL, "postid=" + DOM.getElementProperty(Board.this.getElement(), "id") + "&posthash=" + m_posthash);
            }
        }
    }
    
    // Public entry point to create Boards
    static public void Create(Element boardElement) {
	if (boardElement != null) {
	    try {
		Board board = new Board(boardElement);
	    } catch (Exception e) {
		DOM.setInnerText(boardElement, e.toString());
	    }
	}
    }
    
    // Private constructor for Boards. To create one, use the static
    // CreateBoard method
    private Board(Element element) {
	super(element);
	m_selectedMovelist = new ArrayList<Square>();
	m_result = ONGOING;
	m_nranks = 8;
	m_nfiles = 8;
	m_color = Piece.WHITE;
	m_turn = 0;
        m_startPly = -1;
	m_loading = true;
	m_selectedPiece = null;
	m_lastMovedPiece = null;
	m_authkey = null;
	m_userid = null;
	m_posthash = null;
        m_postmoveURL = GWT.getModuleBaseURL() + "postmove.php";
        m_updateURL = GWT.getModuleBaseURL() + "update.php";
	m_optionFlags = 0;
	m_canPlay = new boolean[2];
	m_canPlay[Piece.WHITE] = m_canPlay[Piece.BLACK] = false;
	m_canPotentiallyCastle = new int[2][2];
	m_canPotentiallyCastle[0] = new int[2];
	m_canPotentiallyCastle[1] = new int[2];
	m_canPotentiallyCastle[0][0] = -1;
	m_canPotentiallyCastle[0][1] = -1;
	m_canPotentiallyCastle[1][0] = -1;
	m_canPotentiallyCastle[1][1] = -1;
	m_canACastle = m_canHCastle = false;
        m_isCurrent = true;
        m_timer = new GameTimer();
        m_pieceAnimation = null;

        // Strip out any <pre> tags that may be present in the
        // children. The server may be interposing them in an attempt
        // to evade WordPress auto-formatting problems.
        String innerHtml = DOM.getInnerHTML(element);
        innerHtml = innerHtml.replaceAll("\\<pre\\>", "");
        innerHtml = innerHtml.replaceAll("\\</pre\\>", "");
        innerHtml = innerHtml.replaceAll("\\<PRE\\>", "");
        innerHtml = innerHtml.replaceAll("\\</PRE\\>", "");
        DOM.setInnerHTML(element, innerHtml);
        
        // Elements corresponding to following strings
        Element authkeyInput = null, useridInput = null, posthashInput = null, optionFlagsInput = null, intervalInput = null;
	// Get the (up to four) input children of the div, if they exist.
	int childCount = DOM.getChildCount(element);
        // Attempt to deal with any PREs still wrapped inside DIVs
        while (childCount == 1 && DOM.getChild(element, 0).getTagName().toLowerCase() == "pre") {
            element = DOM.getChild(element, 0);
            childCount = DOM.getChildCount(element);
        }
	if (childCount >= 1) {
	    for (int i = 0; i < childCount; ++i) {
		Element child = DOM.getChild(element, i);
		String name = DOM.getElementProperty(child, "name");
		if (name != null) {
		    if (name.equals("authkey")) {
			authkeyInput = child;
			m_authkey = DOM.getElementProperty(authkeyInput, "value").trim();
		    }
		    else if (name.equals("userid")) {
			useridInput = child;
			m_userid = DOM.getElementProperty(useridInput, "value").trim();
		    }
		    else if (name.equals("posthash")) {
			posthashInput = child;
			m_posthash = DOM.getElementProperty(posthashInput, "value").trim();
		    }
		    else if (name.equals("cbbflags")) {
			optionFlagsInput = child;
			m_optionFlags = Integer.parseInt(DOM.getElementProperty(optionFlagsInput, "value").trim());
		    }
                    else if (name.equals("cbbinterval")) {
                        intervalInput = child;
			m_timerInterval = Integer.parseInt(DOM.getElementProperty(intervalInput, "value").trim()) * 1000;
		    }
		}
	    }
            if (authkeyInput != null) DOM.removeChild(element, authkeyInput);
	    if (useridInput != null) DOM.removeChild(element, useridInput);
	    if (posthashInput != null) DOM.removeChild(element, posthashInput);
	    if (optionFlagsInput != null) DOM.removeChild(element, optionFlagsInput);
	    if (intervalInput != null) DOM.removeChild(element, intervalInput);
	}
	m_boardString = DOM.getInnerText(element);
        m_pgn = m_boardString;
	DOM.setInnerText(element, "Loading..");
	// In case the DIV was set invisible
	setVisible(true);

	// Deferring the final board creation allows the "Loading"
	// text to be seen
	DeferredCommand.addCommand(new Command() {
	    public void execute() {
		try {
		    Board.this.FinishCreate();
		} catch (Exception e) {
		    DOM.setInnerText(Board.this.getElement(), e.toString());
		}
	    }
	});
	onAttach();
    }
    
    public void FinishCreate() {
	// Create the user interface elements
	m_vpanel = new TightGrid("CBB-chessboard");
	m_vpanel.resize(3, 1);

	// Hide the vpanel until it finishes laying out
	DOM.setStyleAttribute(m_vpanel.getElement(), "visibility", "hidden");
	m_whiteLabel = new Label();
	m_blackLabel = new Label();
	m_whiteLabel.setStyleName("CBB-side-label");
	m_blackLabel.setStyleName("CBB-side-label");
        if ((m_optionFlags & EVENTLABEL) != 0) {
            m_eventLabel = new Label();
            m_eventLabel.setStyleName("CBB-event-label");
        }
        if ((m_optionFlags & SITELABEL) != 0) {
            m_siteLabel = new Label();
            m_siteLabel.setStyleName("CBB-event-label");
        }
	final TightGrid boardPanel = new TightGrid();
	if ((m_optionFlags & TOPFILELABEL) != 0) {
	    m_topFileLabelGrid = new TightGrid("CBB-file-grid");
	}
	if ((m_optionFlags & BOTTOMFILELABEL) != 0) {
	    m_bottomFileLabelGrid = new TightGrid("CBB-file-grid");
	}
	m_grid = new TightGrid("CBB-chessboard", "CBB-chessboard-td");
	m_gridPanel = new AbsolutePanel();
	m_gridPanel.add(m_grid, 0, 0);
	if ((m_optionFlags & LEFTRANKLABEL) != 0) {
	    m_leftRankLabelGrid = new TightGrid("CBB-rank-grid");
	}
	if ((m_optionFlags & RIGHTRANKLABEL) != 0) {
	    m_rightRankLabelGrid = new TightGrid("CBB-rank-grid");
	}
	int nrows = 4; // for at least m_blackLabel, m_gridPanel, m_whiteLabel, m_statusLbel
	if (m_eventLabel != null) nrows++;
	if (m_siteLabel != null) nrows++;
	if (m_topFileLabelGrid != null) nrows++;
	if (m_bottomFileLabelGrid != null) nrows++;
	boardPanel.resize(nrows, 3);
	nrows = 0;
        if (m_eventLabel != null) {
            boardPanel.setWidget(nrows++, 1, m_eventLabel);
        }
        if (m_siteLabel != null) {
            boardPanel.setWidget(nrows++, 1, m_siteLabel);
        }
	boardPanel.setWidget(nrows++, 1, m_blackLabel);
	if (m_topFileLabelGrid != null) {
	    boardPanel.setWidget(nrows++, 1, m_topFileLabelGrid);
	}

	if (m_leftRankLabelGrid != null) {
	    boardPanel.setWidget(nrows, 0, m_leftRankLabelGrid);
	}
	boardPanel.setWidget(nrows, 1, m_gridPanel);
	if (m_rightRankLabelGrid != null) {
	    boardPanel.setWidget(nrows, 2, m_rightRankLabelGrid);
	}
	nrows++;
	if (m_bottomFileLabelGrid != null) {
	    boardPanel.setWidget(nrows++, 1, m_bottomFileLabelGrid);
	}
	boardPanel.setWidget(nrows++, 1, m_whiteLabel);
	m_statusLabel = new TextArea();
	m_statusLabel.setVisibleLines(2);
	m_statusLabel.setReadOnly(true);
	m_statusLabel.setStyleName("CBB-status");
	centerElement(m_statusLabel.getElement());
	boardPanel.setWidget(nrows++, 1, m_statusLabel);

	final TightGrid buttonPanel = new TightGrid();
	buttonPanel.resize(1, 9);
 	m_aCastleButton = new SizedPushButton("", "CBB-acastle-button", "A-Side Castle", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		if (m_canACastle) {
		    int rank = m_kings[m_color].getSquare().getRank() - 1;
		    m_kings[m_color].castle("O-O-O", m_squares[m_canPotentiallyCastle[m_color][0]][rank].getPiece(), m_squares[2][rank], m_squares[3][rank]);
		}
	    }
	});
 	m_hCastleButton = new SizedPushButton("", "CBB-hcastle-button", Character.toString((char) ('A' + m_nfiles - 1)) + "-Side Castle", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		if (m_canHCastle) {
		    int rank = m_kings[m_color].getSquare().getRank() - 1;
		    m_kings[m_color].castle("O-O", m_squares[m_canPotentiallyCastle[m_color][1]][rank].getPiece(), m_squares[m_nfiles - 2][rank], m_squares[m_nfiles - 3][rank]);
		}
	    }
	});
 	m_drawButton = new SizedPushButton("", "CBB-draw-button", "Offer Draw", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		if (Board.this.m_result == ONGOING) {
		    new OfferDrawDialog(Board.this).show();
		} else {
		    new AcceptDrawDialog(Board.this).show();
		}
	    }
	});
 	m_resignButton = new SizedPushButton("", "CBB-resign-button", "Resign", new ClickHandler() {
	    public void onClick(ClickEvent event) {
		new ResignDialog(Board.this).show();
	    }
	});
 	m_flipButton = new SizedPushButton("", "CBB-flip-button", "Flip Board", new ClickHandler() {
	    public void onClick(ClickEvent event) {
                Board.this.cancelAnimation();                                
		Board.this.flip();
	    }
	});
 	m_startButton = new SizedPushButton("", "CBB-start-button", "Rewind To Start", new ClickHandler() {
            public void onClick(ClickEvent event) {
                Board.this.cancelAnimation();
		if (Board.this.m_gameLogTable != null) {
		    Board.this.m_gameLogTable.start();
		} else {
		    Board.this.m_gameLogParagraph.start();
		}
	    }
	});
 	m_rewindButton = new SizedPushButton("", "CBB-rewind-button", "Rewind", new ClickHandler() {
            public void onClick(ClickEvent event) {
                Board.this.cancelAnimation();
		if (Board.this.m_gameLogTable != null) {
		    Board.this.m_gameLogTable.rewind();
		} else {
		    Board.this.m_gameLogParagraph.rewind();
		}
	    }
	});
 	m_forwardButton = new SizedPushButton("", "CBB-forward-button", "Forward", new ClickHandler() {
            public void onClick(ClickEvent event) {
                Board.this.cancelAnimation();
		if (Board.this.m_gameLogTable != null) {
		    Board.this.m_gameLogTable.forward();
		} else {
		    Board.this.m_gameLogParagraph.forward();
		}
	    }
	});
 	m_endButton = new SizedPushButton("", "CBB-end-button", "Forward To End", new ClickHandler() {
            public void onClick(ClickEvent event) {
                Board.this.cancelAnimation();
		if (Board.this.m_gameLogTable != null) {
		    Board.this.m_gameLogTable.end();
		} else {
		    Board.this.m_gameLogParagraph.end();
		}
	    }
	});
        m_pgnButton = new SizedPushButton("", "CBB-pgn-button", "Download PGN", new ClickHandler() {
            public void onClick(ClickEvent event) {
                openWindow("<html><body><pre>" + URL.decode(m_pgn) + "</pre></body></html>");
	    }

            private native void openWindow(String text) /*-{
                var pgnWindow = $wnd.open(null, null, "height=400,width=400,toolbar=no,menubar=no,location=no");
                pgnWindow.document.open();
                pgnWindow.document.write(text);
                pgnWindow.document.close();
            }-*/;
	});
	buttonPanel.setWidget(0, 0, m_hCastleButton);
	buttonPanel.setWidget(0, 1, m_aCastleButton);
	buttonPanel.setWidget(0, 2, m_drawButton);
	buttonPanel.setWidget(0, 3, m_resignButton);
	buttonPanel.setWidget(0, 4, m_flipButton);
	buttonPanel.setWidget(0, 5, m_startButton);
	buttonPanel.setWidget(0, 6, m_rewindButton);
	buttonPanel.setWidget(0, 7, m_forwardButton);
	buttonPanel.setWidget(0, 8, m_endButton);
//	buttonPanel.setWidget(0, 9, m_pgnButton);
	centerElement(buttonPanel.getElement());
	centerElement(boardPanel.getElement());
	m_vpanel.setWidget(0, 0, boardPanel);
	m_vpanel.setWidget(1, 0, buttonPanel);

	m_fenString = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	// Handle the tags, if they exist
	int index = m_boardString.lastIndexOf(']');
	String movelistString;
	if (index != -1) {
	    // Handle the tags. Search for the CBBIds which determine
	    // who's playing the game, and check if the current userid
	    // matches
	    String tagString = m_boardString.substring(0, index);
	    String[] tagList = tagString.split("\\][^\\[]*\\[");
	    for (int i = 0; i < tagList.length; ++i) {
		String tag = tagList[i].trim();
		if (tag.charAt(0) == '[') tag = tag.substring(1);
		if (tag.startsWith("CBBWhiteId ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			String whiteid = tag.substring(fq + 1, lq);
			if (whiteid.equals(m_userid) || whiteid.equals("*")) {
			    m_canPlay[Piece.WHITE] = true;
			}
		    }
		} else if (tag.startsWith("CBBBlackId ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			String blackid = tag.substring(fq + 1, lq);
			if (blackid.equals(m_userid) || blackid.equals("*")) {
			    m_canPlay[Piece.BLACK] = true;
			}
		    }
		} else if (tag.startsWith("White ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			String name = tag.substring(fq + 1, lq);
			int fc = name.indexOf(",");
			if (fc != -1) {
			    m_whiteLastName = name.substring(0, fc);
			} else {
			    m_whiteLastName = name;
			}
                        m_whiteLastName = m_whiteLastName.trim();
		    }		    
		} else if (tag.startsWith("Black ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			String name = tag.substring(fq + 1, lq);
			int fc = name.indexOf(",");
			if (fc != -1) {
			    m_blackLastName = name.substring(0, fc);
			} else {
			    m_blackLastName = name;
			}
                        m_blackLastName = m_blackLastName.trim();
		    }		    
		} else if (tag.startsWith("Result ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			String result = tag.substring(fq + 1, lq);
			result = result.trim();
			if (result.equals("1-0")) {
			    m_result = WHITEWON;
			} else if (result.equals("0-1")) {
			    m_result = BLACKWON;
			} else if (result.equals("1/2-")) {
			    m_result = WHITEOFFEREDDRAW;
			} else if (result.equals("-1/2")) {
			    m_result = BLACKOFFEREDDRAW;
			} else if (result.equals("1/2 - 1/2")) {
			    m_result = DRAW;
			} else {
			    m_result = ONGOING;
			}
		    }
		} else if (tag.startsWith("FEN ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			m_fenString = tag.substring(fq + 1, lq);
			m_fenString = m_fenString.trim();
		    }		    
                } else if (tag.startsWith("Event ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			m_eventString = tag.substring(fq + 1, lq);
			m_eventString = m_eventString.trim();
                        if (m_eventString.equals("")) {
                            m_eventString = null;
                        }
                    }
                } else if (tag.startsWith("Site ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			m_siteString = tag.substring(fq + 1, lq);
			m_siteString = URL.decode(m_siteString.trim());
                        if (m_siteString.equals("")) {
                            m_siteString = null;
                        }
                    }
                } else if (tag.startsWith("Date ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			m_dateString = tag.substring(fq + 1, lq);
			m_dateString = m_dateString.trim();
                        if (m_dateString.equals("")) {
                            m_dateString = null;
                        }
                    }
                } else if (tag.startsWith("Round ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			m_roundString = tag.substring(fq + 1, lq);
			m_roundString = m_roundString.trim();
                        if (m_roundString.equals("") || m_roundString.equals("-")) {
                            m_roundString = null;
                        }
                    }
                } else if (tag.startsWith("JsCom ")) {
		    int fq = tag.indexOf('"'), lq = tag.lastIndexOf('"');
		    if (fq != -1 && lq != -1 && fq != lq) {
			String jsComString = tag.substring(fq + 1, lq);
			jsComString = jsComString.trim();
                        if (!jsComString.equals("")) {
                            String[] jsCommands = jsComString.split(":");
                            if (jsCommands.length > 0) {
                                for (int jsi = 0; jsi < jsCommands.length; ++jsi) {
                                    if (jsCommands[jsi].startsWith("startply ")) {
					try {
					    m_startPly = Integer.parseInt(jsCommands[jsi].substring(9));
					} catch (NumberFormatException e) {
					    m_startPly = -1;
					}
                                        if (m_startPly < 0) m_startPly = -1;
                                    }
                                }
                            }
                        }
                    }
                }

	    }
	    movelistString = m_boardString.substring(index + 1);
	} else {
	    movelistString = m_boardString;
	}
        if (m_eventLabel != null) {
            if (m_eventString != null) {
                if (m_roundString != null) {
                    m_eventLabel.setText(m_eventString + ", Round " + m_roundString);
                } else {
                    m_eventLabel.setText(m_eventString);
                }
            }
        }
        if (m_siteLabel != null) {
            if (m_siteString != null) {
                if (m_dateString != null) {
                    m_siteLabel.setText(m_siteString + ", " + m_dateString);
                } else {
                    m_siteLabel.setText(m_siteString);
                }
            } else if (m_dateString != null) {
                m_siteLabel.setText(m_dateString);
            }
        }
	if (m_whiteLastName != null && !m_whiteLastName.equals("")) {
	    m_whiteLabel.setText(m_whiteLastName + "/White");
	} else {
	    m_whiteLabel.setText("White");
	}
	if (m_blackLastName != null && !m_blackLastName.equals("")) {
	    m_blackLabel.setText(m_blackLastName + "/Black");
	} else {
	    m_blackLabel.setText("Black");
	}

	// Parse the FEN string to determine the initial board setup
	// and number of squares
	String[] fenTokens = m_fenString.split(" ");
	if (fenTokens.length == 6) {
	    // Determine board dimensions by looking at the piece
	    // placement record. Count fwd slashes to determine ranks;
	    // partially parse the first rank to count files
	    int pos = 0;
	    m_nranks = 0;
	    while ((pos = fenTokens[0].indexOf('/', pos)) > -1) {
		pos++;
		m_nranks++;
	    }
	    m_nranks++;
	    int firstSlash = fenTokens[0].indexOf('/');
	    String firstRank = fenTokens[0].substring(0, firstSlash);
	    m_nfiles = 0;
	    for (int file = 0; file < firstRank.length(); ++file) {
		if (firstRank.charAt(file) >= '0' && firstRank.charAt(file) <= '9') {
		    m_nfiles += (firstRank.charAt(file) - '0');
		} else {
		    m_nfiles++;
		}
	    }
	}
	DOM.setElementProperty(m_hCastleButton.getElement(), "title", Character.toString((char) ('A' + m_nfiles - 1)) + "-Side Castle");
	// Set up labels
	if (m_leftRankLabelGrid != null) {
	    m_leftRankLabelGrid.resize(m_nranks, 1);
	}
	if (m_rightRankLabelGrid != null) {	
	    m_rightRankLabelGrid.resize(m_nranks, 1);
	}
	for (int rank = 0; rank < m_nranks; ++rank) {
	    if (m_leftRankLabelGrid != null) {
	    	Label label = new Label(Integer.toString(m_nranks - rank));
	    	label.setStyleName("CBB-rank-label");
		m_leftRankLabelGrid.setWidget(rank, 0, label);
	    }
	    if (m_rightRankLabelGrid != null) {
    	    	Label label = new Label(Integer.toString(m_nranks - rank));
	    	label.setStyleName("CBB-rank-label");
		m_rightRankLabelGrid.setWidget(rank, 0, label);
	    }
	}
	// The file labels are padded by one cell because of the left rank label
	if (m_topFileLabelGrid != null) {
	    m_topFileLabelGrid.resize(1, m_nfiles + 1);
	}
	if (m_bottomFileLabelGrid != null) {	
	    m_bottomFileLabelGrid.resize(1, m_nfiles + 1);
	}
	for (char file = 0; file < m_nfiles; ++file) {
	    if (m_topFileLabelGrid != null) {
	    	Label label = new Label(Character.toString((char)(file + 'a')));
	    	label.setStyleName("CBB-file-label");
		m_topFileLabelGrid.setWidget(0, file + 1, label); 
	    }
	    if (m_bottomFileLabelGrid != null) {
    	    	Label label = new Label(Character.toString((char)(file + 'a')));
	    	label.setStyleName("CBB-file-label");
		m_bottomFileLabelGrid.setWidget(0, file + 1, label);
	    }
	}

	// Set up the array of squares
	m_squares = new Square[m_nfiles][m_nranks];
	int squareColor = Piece.BLACK;
	m_grid.resize(m_nranks, m_nfiles);
	for (char file = 0; file < m_nfiles; ++file) {
	    m_squares[file] = new Square[m_nranks];
	    for (int rank = 0; rank < m_nranks; ++rank) {
		Square square = new Square((char)(file + 'a'), rank + 1, squareColor, this);
		m_squares[file][rank] = square;
		m_grid.setWidget(m_nranks - rank - 1, file, square);
		squareColor = 1 - squareColor;
	    }
	    squareColor = 1 - squareColor;	    
	}
	for (char file = 0; file < m_nfiles; ++file) {
	    for (int rank = 0; rank < m_nranks; ++rank) {	    
		if (file != m_nfiles - 1) {
		    m_squares[file][rank].setRight(m_squares[file + 1][rank]);
		}
		if (file != 0) {
		    m_squares[file][rank].setLeft(m_squares[file - 1][rank]);
		}
		if (rank != m_nranks - 1) {
		    m_squares[file][rank].setTop(m_squares[file][rank + 1]);
		}
		if (rank != 0) {
		    m_squares[file][rank].setBottom(m_squares[file][rank - 1]);
		}
	    }
	}

	// Handle the movelist. Delete any HTML that may have snuck in
	movelistString = movelistString.replaceAll("\\<[^\\>]*\\>", "");
	if ((m_optionFlags & PARAGRAPHLOG) != 0) {
            m_logPanel = null;
	    m_gameLogParagraph = new GameLogParagraph(this, m_whiteLastName, m_blackLastName);
            m_vpanel.setWidget(2, 0, m_gameLogParagraph);            
	} else {
            m_logPanel = new DisclosurePanel("Initial position");
            m_logPanel.setOpen((m_optionFlags & EXPANDGAMELOG) != 0);
            m_logPanel.setStyleName("CBB-gamelogDisclosurePanel");
	    m_gameLogTable = new GameLogTable(this, m_whiteLastName, m_blackLastName);
	    m_logPanel.add(m_gameLogTable);
            m_logPanel.getHeader().setStyleName("CBB-header");
            m_vpanel.setWidget(2, 0, m_logPanel);
	}

	parseFenString(m_fenString);

	// Initialize 0th ply
	if (m_gameLogTable != null) {
	    m_gameLogTable.addMove(m_color, m_turn, "", getMap());
	} else {
	    m_gameLogParagraph.addMove(m_color, m_turn, "", getMap());
	}
	// Parse the move list
        parseMoveList(movelistString);

        if (m_logPanel != null) {
            // This is some serious skullduggery to deal with the fact
            // that the DisclosurePanel class is final, and contains an
            // anchor tag child that by default has the class "header" -
            // which conflicts with many WordPress style sheets. Said
            // child can't be altered in any way, since it's not the
            // header widget, it wraps the header widget.
            DOM.setElementProperty(DOM.getChild(DOM.getChild(DOM.getChild(DOM.getChild(m_logPanel.getElement(), 0), 0), 0), 0), "className", "CBB-header");
            // More skullduggery to deal with the child that has the style
            // "content"
            DOM.setElementProperty(DOM.getChild(DOM.getChild(DOM.getChild(DOM.getChild(DOM.getChild(m_logPanel.getElement(), 0), 1), 0), 0), 0), "className", "CBB-content");
        }
        
        // Advance to starting position
        if (m_startPly != -1) {
	    if (m_gameLogTable != null) {
		m_gameLogTable.setPly(m_startPly);
	    } else {
		m_gameLogParagraph.setPly(m_startPly);
	    }
        }
	m_loading = false;
	DOM.setInnerText(getElement(), "");	
	add(m_vpanel);

        // Schedule an update
        if (m_timerInterval != 0 && m_posthash != null) {
            m_timer.schedule(m_timerInterval);
        }

        // Set up animation
        if ((m_optionFlags & ANIMATION) != 0) {
            m_pieceAnimation = new PieceAnimation(m_gridPanel);
        }
        
	// Last command to execute: sets the Square sizes correctly,
	// after that's done we can go ahead and make the board visible
	DeferredCommand.addCommand(new Command() {
	    public void execute() {
		m_hCastleButton.fixWidth();
		m_aCastleButton.fixWidth();
		m_drawButton.fixWidth();
		m_resignButton.fixWidth();
		m_flipButton.fixWidth();
		m_startButton.fixWidth();
		m_rewindButton.fixWidth();
		m_forwardButton.fixWidth();
		m_endButton.fixWidth();
                m_pgnButton.fixWidth();
		
		m_gridPanel.setHeight(DOM.getElementProperty(m_grid.getElement(), "clientHeight") + "px");
		m_gridPanel.setWidth(DOM.getElementProperty(m_grid.getElement(), "clientWidth") + "px");

		// Resize the labels to match the computed square sizes 
		for (int rank = 0; rank < m_nranks; ++rank) {
		    if (m_leftRankLabelGrid != null) {
			m_leftRankLabelGrid.getCellFormatter().setHeight(rank, 0, DOM.getElementProperty(m_grid.getCellFormatter().getElement(rank, 0), "clientHeight"));
		    }
		    if (m_rightRankLabelGrid != null) {
			m_rightRankLabelGrid.getCellFormatter().setHeight(rank, 0, DOM.getElementProperty(m_grid.getCellFormatter().getElement(rank, 0), "clientHeight"));
		    }
		}
 		for (int file = 0; file < m_nfiles; ++file) {
		    if (m_topFileLabelGrid != null) {
			m_topFileLabelGrid.getCellFormatter().setWidth(0, file + 1, DOM.getElementProperty(m_grid.getCellFormatter().getElement(0, file), "clientWidth"));
		    }
		    if (m_bottomFileLabelGrid != null) {
			m_bottomFileLabelGrid.getCellFormatter().setWidth(0, file + 1, DOM.getElementProperty(m_grid.getCellFormatter().getElement(0, file), "clientWidth"));
		    }
 		}

                // Resize logs and status label to be the same width as the board
                String boardwidth = DOM.getElementProperty(m_grid.getElement(), "clientWidth") + "px";
                if (m_gameLogParagraph != null) {
                    m_gameLogParagraph.setWidth(boardwidth);
                }
                if (m_logPanel != null) {
                    m_logPanel.setWidth(boardwidth);
                }
                m_statusLabel.setWidth(DOM.getElementProperty(m_grid.getElement(), "clientWidth") + "px");
                
		// With the board properly laid out, the spinner can
		// be initialized with the correct position
		m_spinner = new SpinPanel(Board.this);
		
		// Finally, we can make the board visible!
		DOM.setStyleAttribute(m_vpanel.getElement(), "visibility", "visible");
	    }
	});
    }

    public void parseFenString(String fenString) {
        for (int rank = 0; rank < m_nranks; ++rank) {
            for (int file = 0; file < m_nfiles; ++file) {
                m_squares[file][rank].setPiece(null);
            }
        }
        
	String[] fenTokens = fenString.split(" ");
	if (fenTokens.length == 6) {
	    // Set up pieces
	    m_allPieces = new ArrayList<ArrayList<Piece>>(2);
	    m_allPieces.add(new ArrayList<Piece>());
	    m_allPieces.add(new ArrayList<Piece>());
	    m_kings = new King[2];
	    String[] fenRanks = fenTokens[0].split("/");
	    for (int rank = 0; rank < m_nranks; ++rank) {
		int file = 0, letter = 0;
		while (letter < fenRanks[m_nranks - 1 - rank].length() && file < m_nfiles) {
		    char symbol = fenRanks[m_nranks - 1 - rank].charAt(letter);
		    if (Character.isDigit(symbol)) {
			switch(symbol) {
			    case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				file += symbol - '0';
				break;
			    case '1':
				// Special case: if the next character is a 0,
				// increment by 10. Otherwise increment by 1.
				if (letter + 1 < fenRanks[m_nranks - 1 - rank].length() &&
				    fenRanks[m_nranks - 1 - rank].charAt(letter + 1) == '0') {
				    file += 10;
				} else {
				    file++;
				}
				break;
			    default:
				break;
			}
		    } else {
			Piece p = Piece.createPieceFromSymbol(symbol, m_squares[file][rank]);
			if (p != null) {
			    m_allPieces.get(p.getColor()).add(p);
			    if (p instanceof King) {
				m_kings[p.getColor()] = (King) p;
			    }
			}
			file++;
		    }
		    letter++;
		}
	    }

	    // Parse the turn first, then set up active color
	    m_turn = Integer.parseInt(fenTokens[5]) - 1;
	    if (fenTokens[1].startsWith("w")) {
		m_color = Piece.WHITE;
	    } else {
		m_color = Piece.BLACK;
	    }

	    // Check en passant target square
	    if (!fenTokens[3].startsWith("-") && fenTokens[3].length() >= 2) {
		int file = fenTokens[3].charAt(0) - 'a';
		int rank = fenTokens[3].charAt(1) - '1';
		// look for the pawn that made the move, which is
		// ahead of this square, and indicate that it can be
		// captured
		if (m_color == Piece.BLACK && rank == 2 && file >= 0 && file < m_nfiles) {
		    Piece pawn = m_squares[file][rank + 1].getPiece();
		    if (pawn != null && pawn instanceof Pawn && pawn.getColor() == Piece.WHITE) {
			setLastMovedPiece(pawn);
			((Pawn) pawn).setEnPassantCapturable(true);
		    }
		} else if (m_color == Piece.WHITE && rank == (m_nranks - 3) && file >= 0 && file < m_nfiles) {
		    Piece pawn = m_squares[file][rank - 1].getPiece();
		    if (pawn != null && pawn instanceof Pawn && pawn.getColor() == Piece.BLACK) {
			setLastMovedPiece(pawn);
			((Pawn) pawn).setEnPassantCapturable(true);
		    }
		}
	    }

	    // Castling availability.
	    for (int letter = 0; letter < fenTokens[2].length(); letter++) {
		switch(fenTokens[2].charAt(letter)) {
		    case 'K':
			for (int file = m_kings[Piece.WHITE].getSquare().getFile() - 'a' + 1; file < m_nfiles; ++file) {
			    Piece rook = m_squares[file][m_kings[Piece.WHITE].getSquare().getRank() - 1].getPiece();
			    if (rook != null && rook instanceof Rook && rook.getColor() == Piece.WHITE) {
				m_canPotentiallyCastle[Piece.WHITE][1] = file;
				break;
			    }
			}
			break;
		    case 'Q':
			for (int file = m_kings[Piece.WHITE].getSquare().getFile() - 'a' - 1; file >= 0; --file) {
			    Piece rook = m_squares[file][m_kings[Piece.WHITE].getSquare().getRank() - 1].getPiece();
			    if (rook != null && rook instanceof Rook && rook.getColor() == Piece.WHITE) {
				m_canPotentiallyCastle[Piece.WHITE][0] = file;
				break;
			    }
			}
			break;
		    case 'k':
			for (int file = m_kings[Piece.BLACK].getSquare().getFile() - 'a'; file < m_nfiles; ++file) {
			    Piece rook = m_squares[file][m_kings[Piece.BLACK].getSquare().getRank() - 1].getPiece();
			    if (rook != null && rook instanceof Rook && rook.getColor() == Piece.BLACK) {
				m_canPotentiallyCastle[Piece.BLACK][1] = file;
				break;
			    }
			}
			break;
		    case 'q':
			for (int file = m_kings[Piece.BLACK].getSquare().getFile() - 'a' - 1; file >= 0; --file) {
			    Piece rook = m_squares[file][m_kings[Piece.BLACK].getSquare().getRank() - 1].getPiece();
			    if (rook != null && rook instanceof Rook && rook.getColor() == Piece.BLACK) {
				m_canPotentiallyCastle[Piece.BLACK][0] = file;
				break;
			    }
			}
			break;
		    default:
			// Try to apply Shredder-FEN: encoding the files directly
			if (fenTokens[2].charAt(letter) >= 'A' && fenTokens[2].charAt(letter) <= 'A' + m_nfiles) {
			    int file = fenTokens[2].charAt(letter) - 'A';
			    Piece rook = m_squares[file][m_kings[Piece.WHITE].getSquare().getRank() - 1].getPiece();
			    if (rook != null && rook instanceof Rook && rook.getColor() == Piece.WHITE) {
				if (file > m_kings[Piece.WHITE].getSquare().getFile() - 'a') {
				    m_canPotentiallyCastle[Piece.WHITE][1] = file;
				} else {
				    m_canPotentiallyCastle[Piece.WHITE][0] = file;				    
				}
			    }
			}
			else if (fenTokens[2].charAt(letter) >= 'a' && fenTokens[2].charAt(letter) <= 'z' + m_nfiles) {
			    int file = fenTokens[2].charAt(letter) - 'a';
			    Piece rook = m_squares[file][m_kings[Piece.BLACK].getSquare().getRank() - 1].getPiece();
			    if (rook != null && rook instanceof Rook && rook.getColor() == Piece.BLACK) {
				if (file > m_kings[Piece.BLACK].getSquare().getFile() - 'a') {
				    m_canPotentiallyCastle[Piece.BLACK][1] = file;
				} else {
				    m_canPotentiallyCastle[Piece.BLACK][0] = file;				    
				}
			    }
			}
			break;
		}
	    }
	    // Ensure the files are sorted
	    if (m_canPotentiallyCastle[Piece.WHITE][0] > -1 && m_canPotentiallyCastle[Piece.WHITE][1] > -1 && m_canPotentiallyCastle[Piece.WHITE][0] > m_canPotentiallyCastle[Piece.WHITE][1]) {
		int swap = m_canPotentiallyCastle[Piece.WHITE][0];
		m_canPotentiallyCastle[Piece.WHITE][0] = m_canPotentiallyCastle[Piece.WHITE][1];
		m_canPotentiallyCastle[Piece.WHITE][1] = swap;
	    }
	    if (m_canPotentiallyCastle[Piece.BLACK][0] > -1 && m_canPotentiallyCastle[Piece.BLACK][1] > -1 && m_canPotentiallyCastle[Piece.BLACK][0] > m_canPotentiallyCastle[Piece.BLACK][1]) {
		int swap = m_canPotentiallyCastle[Piece.BLACK][0];
		m_canPotentiallyCastle[Piece.BLACK][0] = m_canPotentiallyCastle[Piece.BLACK][1];
		m_canPotentiallyCastle[Piece.BLACK][1] = swap;
	    }
	    updateCastlingStatus();
	}
    }

    public void parseMoveList(final String movelistString) {
        m_loading = true;
	int c = 0;
	final int strlen = movelistString.length();
	do {
	    // Figure out next token
	    String token = null;
	    char cc = movelistString.charAt(c);
	    while (c < strlen && Character.isSpace((cc = movelistString.charAt(c)))) {
		c++;
	    }
	    if (c >= strlen) break;
	    // Check for comment start
	    if (cc == '{') {
		int lastc = c++;
		while (c < strlen && (cc = movelistString.charAt(c)) != '}') {
		    c++;
		}
		if (c != strlen) c++;
		token = movelistString.substring(lastc + 1, c - 1);
		token.replaceAll("\\n", " ");
		// Add comments directly to game log table
		if (!token.equals("")) {
		    if (m_gameLogTable != null) {
			m_gameLogTable.addComment(token);
		    } else {
			m_gameLogParagraph.addComment(token);
		    }
		}
		if (c >= strlen) break;
		else continue;
	    }
	    // Treat recursive annotation variations as comments
	    else if (cc == '(') {
		int lastc = c++;
		int nestLevel = 1;
		while (c < strlen) {
		    cc = movelistString.charAt(c);
		    c++;
		    if (cc == ')') {
			nestLevel--;
			if (nestLevel == 0) {
			    break;
			}
		    } else if (cc == '(') {
			nestLevel++;
		    }
		}
		token = movelistString.substring(lastc, c);
		token.replaceAll("\\n", " ");
		// Add comments directly to game log table
		if (!token.equals("")) {
		    if (m_gameLogTable != null) {
			m_gameLogTable.addComment(token);
		    } else {
			m_gameLogParagraph.addComment(token);
		    }
		}
		if (c >= strlen) break;
		else continue;
	    }
	    // Handle numeric annotation glyphs? (how?)
	    else if (cc == '$') {
	    } else {
		int lastc = c++;
		while (c < strlen && !Character.isSpace((cc = movelistString.charAt(c)))) {
		    c++;
		}
		token = movelistString.substring(lastc, c);
		if (c != strlen) c++;	
	    }

	    if (token == null || token.equals("")) break;

	    // Ignore numeric annotation glyphs
	    if (token.startsWith("$")) continue;
	    
	    // Ignore leading numbers and dots on move, and trailing
	    // +, #, ! and ? signs
	    token = token.replaceFirst("^\\d*\\.*([^+#!?]+)[+#!?]*$", "$1");
	    // if last character is '=', drop it
	    if (token.length() > 1 && token.charAt(token.length() - 1) == '=') {
		token = token.substring(0, token.length() - 1);
	    }
	    if (token.equals("Resigns")) {
		resign();
	    }
	    // Handle castling moves
	    else if (token.startsWith("O-O-O")) {
		int rank = m_kings[m_color].getSquare().getRank() - 1;
		m_kings[m_color].castle("O-O-O", m_squares[m_canPotentiallyCastle[m_color][0]][rank].getPiece(), m_squares[2][rank], m_squares[3][rank]);
	    }
	    else if (token.startsWith("O-O")) {
		int rank = m_kings[m_color].getSquare().getRank() - 1;
		m_kings[m_color].castle("O-O", m_squares[m_canPotentiallyCastle[m_color][1]][rank].getPiece(), m_squares[m_nfiles - 2][rank], m_squares[m_nfiles - 3][rank]);
	    }
	    else {
		if (token.length() >= 2) {
		    boolean promotion = false;
		    char promoteSymbol = '0';
		    // Check for pawn promotion
		    if (token.length() > 1 && token.charAt(token.length() - 2) == '=') {
			promotion = true;
			promoteSymbol = token.charAt(token.length() - 1);
			token = token.substring(0, token.length() - 2);			
		    }

		    // For all subsequent cases, the target square is the
		    // last two characters. We can parse them first
		    int toFile = token.charAt(token.length() - 2) - 'a';
		    int toRank = token.charAt(token.length() - 1) - '1';
		    if (toFile < 0 || toFile >= m_nfiles || toRank < 0 || toRank >= m_nranks) continue;
		    Square toSquare = m_squares[toFile][toRank];
		    token = token.substring(0, token.length() - 2);

		    // Check for capture
		    if (token.length() > 0 && token.charAt(token.length() - 1) == 'x') {
			token = token.substring(0, token.length() - 1);
                        // bail if there's nothing to capture! (but
                        // watch out for en passant capture)
			if (toSquare.getPiece() == null && !(token.length() > 0 && Character.isLowerCase(token.charAt(0)) && (toRank == 2 || toRank == m_nranks - 3))) continue;
		    }
		    if (token.length() > 0) {
			// Handle piece move
			if (Character.isUpperCase(token.charAt(0))) {
			    int fromFile = -1;
			    int fromRank = -1;
			    if (token.length() == 3) {
				// Piece was disambiguated with rank and file
				fromFile = token.charAt(1);
				fromRank = token.charAt(2) - '0';
				if (fromFile < 'a' || fromFile > 'a' + m_nfiles || fromRank < 1 || fromRank > m_nranks) continue;
			    } else if (token.length() == 2) {
				// Piece was disambiguated with either rank or file
				if (Character.isDigit(token.charAt(1))) {
				    fromRank = token.charAt(1) - '0';
				    if (fromRank < 1 || fromRank > m_nranks) continue;
				} else {
				    fromFile = token.charAt(1);
				    if (fromFile < 'a' || fromFile > 'a' + m_nfiles) continue;
				}
			    }
			    if (token.charAt(0) == 'K') {
				if (m_kings[m_color].canMoveTo(toSquare)) {
				    m_kings[m_color].move(toSquare);
				}
			    } else {
                                for (Piece piece : m_allPieces.get(m_color)) {
				    if (fromRank != -1 && piece.getSquare().getRank() != fromRank) continue;
				    if (fromFile != -1 && piece.getSquare().getFile() != fromFile) continue;
				    if (piece.getSymbol() == token.charAt(0)) {
					if (piece.canMoveTo(toSquare)) {
					    piece.move(toSquare);
					    break;
					}
				    }
				}
			    }
			}
			// Should be pawn capture
			else {
			    int fromFile = token.charAt(0);
			    if (fromFile < 'a' || fromFile > 'a' + m_nfiles) continue;
			    if (m_color == Piece.WHITE && toRank > 0) {
				Piece pawn = m_squares[fromFile - 'a'][toRank - 1].getPiece();
				if (pawn != null) {
				    if (promotion && pawn instanceof Pawn) {
					((Pawn) pawn).promote(toSquare, promoteSymbol);
				    } else {
					pawn.move(toSquare);
				    }
				} else if (toRank == m_nranks - 3) { // en passant
				    pawn = m_squares[fromFile - 'a'][3].getPiece();
				    if (pawn != null) {
					pawn.move(toSquare);
				    }
				}
			    } else if (m_color == Piece.BLACK && toRank < m_nranks - 1) {
				Piece pawn = m_squares[fromFile - 'a'][toRank + 1].getPiece();
				if (pawn != null) {
				    if (promotion && pawn instanceof Pawn) {
					((Pawn) pawn).promote(toSquare, promoteSymbol);
				    } else {
					pawn.move(toSquare);
				    }
				} else if (toRank == 2) { // en passant
				    pawn = m_squares[fromFile - 'a'][4].getPiece();
				    if (pawn != null) {
					pawn.move(toSquare);
				    }
				}
			    }				
			}
		    }
		    // Should be pawn move
		    else {
			if ((m_color == Piece.WHITE && toRank > 1) || (m_color == Piece.BLACK && toRank < m_nranks - 2)) {
			    Piece pawn = m_squares[toFile][toRank + (m_color == Piece.WHITE ? -1 : 1)].getPiece();
			    if (pawn != null) {
				if (promotion && pawn instanceof Pawn) {
				    ((Pawn) pawn).promote(toSquare, promoteSymbol);
				} else {
				    pawn.move(toSquare);
				}
			    } else {
				pawn = m_squares[toFile][toRank + (m_color == Piece.WHITE ? -2 : 2)].getPiece();
				// Can't promote on a two step move
				if (pawn != null) {
				    pawn.move(toSquare);
				}
			    }
			}
		    }
		}
	    }
	} while (c < strlen);
        m_loading = false;
	updateStatusLabel();
    }

    // Returns the color whose turn it is
    public int getColor() {
	return m_color;
    }

    // Returns whether the current color is actually allowed to play
    // based on userids
    public boolean canPlay() {
	return (m_result == ONGOING) && (m_canPlay[m_color]);
    }
    
    // Marks a piece as being selected
    public void selectPiece(Piece piece) {
	if (m_selectedPiece == piece) {
	    m_selectedPiece = null;
	    piece.getSquare().setSelected(false);
	    setMoveableSquares(false);
	} else {
	    if (m_selectedPiece != null) {
		m_selectedPiece.getSquare().setSelected(false);
		setMoveableSquares(false);
	    }
	    piece.getSquare().setSelected(true);
	    m_selectedPiece = piece;
	    setMoveableSquares(true);
	}
    }
    
    // Marks or clears the squares to which the currently selected
    // piece can move
    protected final void setMoveableSquares(boolean moveable) {
	if (moveable) {
	    m_selectedMovelist.clear();
	    m_selectedPiece.getMoveList(m_selectedMovelist);
            for (Square movesquare : m_selectedMovelist) {
		movesquare.setMoveable(moveable);
	    }
	} else {
            for (Square movesquare : m_selectedMovelist) {
		movesquare.setMoveable(moveable);
	    }
	}	    
    }

    public void offerDraw() {
	if (!m_loading && m_authkey != null && m_posthash != null) {
	    if (m_color == Piece.WHITE) {
		m_updateCommand = new Command() {
			public void execute() {
			    Board.this.m_result = WHITEOFFEREDDRAW;
			}
		    };
		m_updateNotation = null;		
		doPostTurn(m_postmoveURL, "move=1/2-&postid=" + DOM.getElementProperty(getElement(), "id") + "&authkey=" + m_authkey + "&posthash=" + m_posthash);	
	    } else {
		m_updateCommand = new Command() {
			public void execute() {
			    Board.this.m_result = BLACKOFFEREDDRAW;
			}
		    };
		m_updateNotation = null;		
		doPostTurn(m_postmoveURL, "move=-1/2&postid=" + DOM.getElementProperty(getElement(), "id") + "&authkey=" + m_authkey + "&posthash=" + m_posthash);	
	    }
	} else {
	    m_result = (m_color == Piece.WHITE) ? WHITEOFFEREDDRAW : BLACKOFFEREDDRAW;
	    updateGameLog(null);
	}
    }

    public void acceptDraw() {
	if (!m_loading && m_authkey != null && m_posthash != null) {
	    m_updateCommand = new Command() {
		public void execute() {
		    Board.this.m_result = DRAW;
		}
	    };
	    m_updateNotation = null;	
	    doPostTurn(m_postmoveURL, "move=1/2 - 1/2&postid=" + DOM.getElementProperty(getElement(), "id") + "&authkey=" + m_authkey + "&posthash=" + m_posthash);	
	} else {
	    m_result = DRAW;
	    updateGameLog(null);
	}
    }

    public void refuseDraw() {
	if (!m_loading && m_authkey != null && m_posthash != null) {
	    m_updateCommand = new Command() {
		public void execute() {
		    Board.this.m_result = ONGOING;
		}
	    };
	    m_updateNotation = null;	
	    doPostTurn(m_postmoveURL, "move=*&postid=" + DOM.getElementProperty(getElement(), "id") + "&authkey=" + m_authkey + "&posthash=" + m_posthash);	
	} else {
	    m_result = ONGOING;
	    updateGameLog(null);	
	}	
    }

    public void resign() {
	if (!m_loading && m_authkey != null && m_posthash != null) {
	    String serverNotation;
	    if (m_color == Piece.WHITE) {
		serverNotation = (m_turn + 1) + ". Resigns";
	        m_updateCommand = new Command() {
		    public void execute() {
		        Board.this.m_result = BLACKWON;
		    }
	        };				
	    } else {
		serverNotation = " Resigns";
	        m_updateCommand = new Command() {
		    public void execute() {
		        Board.this.m_result = WHITEWON;
		    }
	        };
	    }
	    m_updateNotation = "Resigns";
	    doPostTurn(m_postmoveURL, "move=" + serverNotation + "&postid=" + DOM.getElementProperty(getElement(), "id") + "&authkey=" + m_authkey + "&posthash=" + m_posthash);	
	} else {
	    m_result = (m_color == Piece.WHITE) ? BLACKWON : WHITEWON;
	    updateGameLog("Resigns");
	}
    }

    // Handles change of turn. Input string is the move notation for
    // the current turn.
    public void nextTurn(String notation, Command updateCommand) {
	if (!m_loading && m_authkey != null && m_posthash != null) {
	    // Before posting the move to the server, modify it slightly
	    String serverNotation;
	    if (m_color == Piece.BLACK) {
		serverNotation = " " + notation;
	    } else {
		serverNotation = (m_turn + 1) + "." + notation;
	    }
	    // Replace all plus signs with %2B
	    serverNotation = serverNotation.replaceAll("\\+", "%2B");
	    doPostTurn(m_postmoveURL, "move=" + serverNotation + "&postid=" + DOM.getElementProperty(getElement(), "id") + "&authkey=" + m_authkey + "&posthash=" + m_posthash);
	    if (updateCommand != null) m_updateCommand = updateCommand;
	    m_updateNotation = notation;
	} else {
	    if (updateCommand != null) updateCommand.execute();
	    updateGameLog(notation);
	}
    }

    private void updateGameLog(String notation) {
	if (notation != null) {
	    if (m_color == Piece.WHITE) {
		m_turn++;
	    }
            if (m_logPanel != null) {
                if (m_turn == 0) {
                    m_logPanel.getHeaderTextAccessor().setText(notation);
                } else if (m_color == Piece.WHITE) {
                    m_logPanel.getHeaderTextAccessor().setText(Integer.toString(m_turn) + "." + notation);
                } else {
                    m_logPanel.getHeaderTextAccessor().setText(Integer.toString(m_turn) + "... " + notation);		    
                }
            }
	    if (m_gameLogTable != null) {
		m_gameLogTable.addMove(m_color, m_turn, notation, getMap());
	    } else {
		m_gameLogParagraph.addMove(m_color, m_turn, notation, getMap());
	    }
	    m_color = 1 - m_color;
	    char lastchar = notation.charAt(notation.length() - 1);
	    if (lastchar == '+') {
		if (notation.charAt(notation.length() - 2) == '+') {
		    m_result = (m_color == Piece.WHITE) ? BLACKWON : WHITEWON;
		}
	    } else if (lastchar == '=') {
		m_result = DRAW;
	    }
	}
	updateCastlingStatus();
	updateStatusLabel();
    }

    private void updateCastlingStatus() {
	m_canACastle = m_canHCastle = false;

	if (isChecked(m_color)) return;

	// Determine whether the color can castle on the a side
	if (m_canPotentiallyCastle[m_color][0] >= 0) {
	    boolean canCastle = true;
	    // Make sure king can move all the way to the c file. Note
	    // that this may be to the left or to the right of the
	    // current king's file!
	    King king = m_kings[m_color];
	    Square kingSquare = king.getSquare();
	    Square square = kingSquare;
	    while (square.getFile() != 'c') {
		if (square.getFile() > 'c') {
		    square = square.getNeighbor(Square.LEFT);
		} else {
		    square = square.getNeighbor(Square.RIGHT);
		}
		if (square.getFile() - 'a' != m_canPotentiallyCastle[m_color][0] && !king.isValidMove(square)) {
		    canCastle = false;
		    break;
		}
	    }
	    if (canCastle) {
		// So far, so good. Make sure the rook can move all the
		// way to the d file.
		square = m_squares[m_canPotentiallyCastle[m_color][0]][kingSquare.getRank() - 1];
		Piece rook = square.getPiece();
		if (rook != null && rook instanceof Rook) {
		    while (square.getFile() != 'd') {
			if (square.getFile() > 'd') {
			    square = square.getNeighbor(Square.LEFT);
			} else {
			    square = square.getNeighbor(Square.RIGHT);
			}
			if (square != kingSquare && square.getPiece() != null) {
			    canCastle = false;
			    break;
			}
		    }
		}
	    }
	    m_canACastle = canCastle;
	}
	// Determine whether the color can castle on the h side
	if (m_canPotentiallyCastle[m_color][1] >= 0) {
	    boolean canCastle = true;
	    // Make sure king can move all the way to the g file. Note
	    // that this may be to the left or to the right of the
	    // current king's file!
	    King king = m_kings[m_color];
	    Square kingSquare = king.getSquare();
	    Square square = kingSquare;
	    while (square.getFile() != 'a' + m_nranks - 2) {
		if (square.getFile() > 'a' + m_nranks - 2) {
		    square = square.getNeighbor(Square.LEFT);
		} else {
		    square = square.getNeighbor(Square.RIGHT);
		}
		if (square.getFile() - 'a' != m_canPotentiallyCastle[m_color][1] && !king.isValidMove(square)) {
		    canCastle = false;
		    break;
		}
	    }
	    if (canCastle) {
		// So far, so good. Make sure the rook can move all
		// the way to the f file.
		square = m_squares[m_canPotentiallyCastle[m_color][1]][kingSquare.getRank() - 1];
		Piece rook = square.getPiece();
		if (rook != null && rook instanceof Rook) {
		    while (square.getFile() != 'a' + m_nranks - 3) {
			if (square.getFile() > 'a' + m_nranks - 3) {
			    square = square.getNeighbor(Square.LEFT);
			} else {
			    square = square.getNeighbor(Square.RIGHT);
			}
			if (square != kingSquare && square.getPiece() != null) {
			    canCastle = false;
			    break;
			}
		    }
		}
	    }
	    m_canHCastle = canCastle;
	}
    }
    
    private void updateStatusLabel() {
	if (!m_loading) {
	    m_drawButton.setEnabled(false);
	    m_resignButton.setEnabled(false);
	    String status = "";
	    // We have several statuses here. If the game is concluded,
	    // update accordingly. Otherwise, if there is an auth key, 
	    // this is a live game that can update the database. Indicate 
	    // whether or not this is your move, your opponent's move, or 
	    // a game where you are simply spectating.
	    if (m_result == BLACKOFFEREDDRAW || m_result == WHITEOFFEREDDRAW) {
		m_canACastle = m_canHCastle = false;		
	        if (m_authkey != null && m_posthash != null) {
		    if ((m_result == BLACKOFFEREDDRAW && m_canPlay[Piece.WHITE]) ||
			(m_result == WHITEOFFEREDDRAW && m_canPlay[Piece.BLACK])) {
			status = "Draw offered, awaiting acceptance (post will update).";
			DOM.setElementProperty(m_drawButton.getElement(), "title", "Accept or Refuse Draw");
			m_drawButton.setEnabled(true);
		    } else {
			status = "Closed, awaiting opponent's acceptance of your draw offer.";
		    }
		} else {
		    status = "Closed game. ";
		    if (m_result == BLACKOFFEREDDRAW) {
		        status += " Black has offered a draw.";
		    } else {
		        status += " White has offered a draw.";
		    }
		    m_canACastle = m_canHCastle = false;
		}
	    } else if (m_result == ONGOING) {
	        if (m_authkey != null && m_posthash != null) {
		    if (m_canPlay[m_color]) {
			status = "Open game, awaiting your move (post will update).";
			DOM.setElementProperty(m_drawButton.getElement(), "title", "Offer Draw");			
			m_drawButton.setEnabled(true);
			m_resignButton.setEnabled(true);
		    } else if (m_canPlay[1 - m_color]) {
	    		status = "Closed for opponent's move.";
			m_canACastle = m_canHCastle = false;
		    } else {
		    	status = "Closed game. ";
			if (m_color == Piece.WHITE) {
			    status += " White to move.";
			} else {
			    status += " Black to move.";
			}
			m_canACastle = m_canHCastle = false;
		    }
		} else {
		    // If there isn't an auth key, either it's a closed
		    // game or a wide open game which doesn't update the
		    // database
		    if (m_canPlay[m_color]) {
		        status = "Open game, moves will not be saved to post.";
		    } else {
		        status = "Closed game. ";
			m_canACastle = m_canHCastle = false;
		    }
		    if (m_color == Piece.WHITE) {
		        status += " White to move.";
		    } else {
		        status += " Black to move.";
		    }
		}
	    } else if (m_result == WHITEWON) {
		status = "White wins.";
	    } else if (m_result == BLACKWON) {
	        status = "Black wins.";
	    } else if (m_result == DRAW) {
		status = "Draw.";
	    }
	    m_statusLabel.setText(status);
	    // Update the castle buttons
	    m_aCastleButton.setEnabled(m_canACastle);
	    m_hCastleButton.setEnabled(m_canHCastle);
	}
    }
    
    // Posts the turn to the server
    private void doPostTurn(String url, String postData) {
	m_postData = postData;
	m_spinner.show();
	RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
	try {
	    Request response = builder.sendRequest(postData,
               new RequestCallback() {
                   public void onError(Request request, Throwable exception) {
                       m_spinner.hide();
                       new MoveDialog("Your move failed to post to the database.", Board.this).show();
                   }
                   
                   public void onResponseReceived(Request request, Response response) {
                       m_spinner.hide();
                       switch (response.getStatusCode()) {
                           case 200:
                               // success!
                               if (m_updateCommand != null) {
                                   m_updateCommand.execute();
                                   updateGameLog(m_updateNotation);
                                   m_updateCommand = null;
                                   m_updateNotation = null;
                                   String newContent = response.getText().trim();
                                   m_posthash = newContent.substring(0, 32);
                                   m_pgn = newContent.substring(32);
                                   m_postData = null;
                               }
                               break;
                           case 301: // Moved permanently
                           case 302: // Moved temporarily
                               // Resend it to the new URL
                               doPostTurn(response.getHeader("Location").trim(), m_postData);
                               break;
                           case 403:
                               new MoveDialog("You moved failed to post to the database: " + response.getText().trim(), Board.this).show();
                               m_postData = null;
                               break;
                           case 404:
                               new MoveDialog("Your move failed to post to the database due to a missing script on the server. Please ensure the plugin was correctly installed.", Board.this).show();
                               m_postData = null;
                               break;
                           default:
                               new MoveDialog("Your move failed to post to the database - status code:" + response.getStatusCode() + ", status response:" + response.getText().trim(), Board.this).show();
                               m_postData = null;
                               break;
                       }
                   }
               });
	} catch (RequestException e) {
	    m_spinner.hide();
	    new MoveDialog("Your move failed to post to the database for an unknown reason.", Board.this).show();
            m_postData = null;
	}
    }

    // Asks the server for an update on the current board
    private void doCheckUpdate(String url, String postData) {
        // Don't ask for an update if we're waiting for a move,
        // just reschedule
        if (m_postData != null) {
            m_timer.schedule(m_timerInterval);
            return;
        }
	RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
	try {
	    Request response = builder.sendRequest(postData,
                new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                    }
                   
                    public void onResponseReceived(Request request, Response response) {
                        switch (response.getStatusCode()) {
                            case 200:
                                // success!
                                // 1 or 0: update or not
                                String newContent = response.getText().trim();
                                if (!newContent.isEmpty()) {
                                    m_posthash = newContent.substring(0, 32);
                                    newContent = newContent.substring(32);
                                    m_pgn = newContent;
                                    int index = newContent.lastIndexOf(']');
                                    String movelistString = newContent.substring(index + 1);
                                    movelistString.replaceAll("\\<[^\\>]*\\>", "");
                                    parseFenString(m_fenString);
				    if (m_gameLogTable != null) {
					m_logPanel.remove(m_gameLogTable);
					m_gameLogTable = new GameLogTable(Board.this, m_whiteLastName, m_blackLastName);
					m_logPanel.add(m_gameLogTable);
					// FIXME: is this correct?
					m_gameLogTable.addMove(m_color, m_turn, "", getMap());
				    } else {
					m_gameLogParagraph = new GameLogParagraph(Board.this, m_whiteLastName, m_blackLastName);
					m_vpanel.setWidget(2, 0, m_gameLogParagraph);
				    }
                                    parseMoveList(movelistString);
                                    m_timer.schedule(m_timerInterval);
                                } else {
                                    m_timer.schedule(m_timerInterval);
                                }
                                break;
                            case 301: // Moved permanently
                            case 302: // Moved temporarily
                                // Resend it to the new URL
                                doCheckUpdate(response.getHeader("Location").trim(), m_postData);
                                break;
                            case 403:
                            case 404:
                            default:
                                // Don't schedule new updates
                                break;
                        }
                    }
                });
	} catch (RequestException e) {
	}
    }

    public void removePiece(Piece piece) {
	m_allPieces.get(piece.getColor()).remove(m_allPieces.get(piece.getColor()).indexOf(piece));
    }

    public void addPiece(Piece piece) {
	m_allPieces.get(piece.getColor()).add(piece);
    }

    public final Iterator getColorPiecesIterator(int color) {
	return m_allPieces.get(m_color).iterator();
    }

    // Queries whether the King of a given color is under check
    public final boolean isChecked(int color) {
	Square square = m_kings[color].getSquare();
        for (Piece piece : m_allPieces.get(1 - color)) {
	    if (piece.threatens(square)) return true;
	}
	return false;
    }
    
    // Queries whether any piece of the given color has a valid move
    public final boolean hasValidMove(int color) {
	ArrayList<Square> movelist = new ArrayList<Square>();
        for (Piece piece : m_allPieces.get(color)) {
	    piece.getMoveList(movelist);
	    if (!movelist.isEmpty()) {
		return true;
	    }
	}
	return false;
    }

    public Piece getSelectedPiece() {
	return m_selectedPiece;
    }

    public Piece getLastMovedPiece() {
	return m_lastMovedPiece;
    }

    public void setLastMovedPiece(Piece piece) {
	m_lastMovedPiece = piece;
    }
        
    // Returns the width of the board.
    public int getWidth() {
	return m_grid.getOffsetWidth();
    }
    
    // Returns the position of X coordinate of center of the board 
    public int getCenterX() {
	return m_grid.getAbsoluteLeft() + m_grid.getOffsetWidth() / 2;
    }

    // Returns the position of X coordinate of center of the board 
    public int getCenterY() {
	return m_grid.getAbsoluteTop() + m_grid.getOffsetHeight() / 2;
    }

    public int getRanks() {
	return m_nranks;
    }

    public int getFiles() {
	return m_nfiles;
    }

    public void clearCastleFlag(int m_color, int file) {
	if (m_canPotentiallyCastle[m_color][0] == file) {
	    m_canPotentiallyCastle[m_color][0] = -1;
	}
	if (m_canPotentiallyCastle[m_color][1] == file) {
	    m_canPotentiallyCastle[m_color][1] = -1;
	}
    }
    
    public void clearBothCastleFlags(int m_color) {
	m_canPotentiallyCastle[m_color][0] = m_canPotentiallyCastle[m_color][1] = -1;
    }

    public boolean isCurrent() { return m_isCurrent; }

    public void flip() {
	Widget tmp;
	String tmpText;
	for (int rank = 0; rank < m_nranks / 2; ++rank) {
	    for (int file = 0; file < m_nfiles; ++file) {
		tmp = m_grid.getWidget(m_nranks - rank - 1, file);
		m_grid.setWidget(m_nranks - rank - 1, file,
				 m_grid.getWidget(rank, file));
		m_grid.setWidget(rank, file, tmp);
	    }
	    if (m_leftRankLabelGrid != null) {
		tmpText = m_leftRankLabelGrid.getText(m_nranks - rank - 1, 0);
		m_leftRankLabelGrid.setText(m_nranks - rank - 1, 0, m_leftRankLabelGrid.getText(rank, 0));
		m_leftRankLabelGrid.setText(rank, 0, tmpText);
	    }
	    if (m_rightRankLabelGrid != null) {
		tmpText = m_rightRankLabelGrid.getText(m_nranks - rank - 1, 0);
		m_rightRankLabelGrid.setText(m_nranks - rank - 1, 0, m_rightRankLabelGrid.getText(rank, 0));
		m_rightRankLabelGrid.setText(rank, 0, tmpText);
	    }
	}
	for (int file = 0; file < m_nfiles / 2; ++file) {
	    for (int rank = 0; rank < m_nranks; ++rank) {
		tmp = m_grid.getWidget(rank, m_nfiles - file - 1);
		m_grid.setWidget(rank, m_nfiles - file - 1,
				 m_grid.getWidget(rank, file));
		m_grid.setWidget(rank, file, tmp);
	    }
	    if (m_topFileLabelGrid != null) {
		tmpText = m_topFileLabelGrid.getText(0, m_nfiles - file);
		m_topFileLabelGrid.setText(0, m_nfiles - file, m_topFileLabelGrid.getText(0, file + 1));
		m_topFileLabelGrid.setText(0, file + 1, tmpText);
	    }
	    if (m_bottomFileLabelGrid != null) {
		tmpText = m_bottomFileLabelGrid.getText(0, m_nfiles - file);
		m_bottomFileLabelGrid.setText(0, m_nfiles - file, m_bottomFileLabelGrid.getText(0, file + 1));
		m_bottomFileLabelGrid.setText(0, file + 1, tmpText);
	    }
	}
	tmpText = m_whiteLabel.getText();
	m_whiteLabel.setText(m_blackLabel.getText());
	m_blackLabel.setText(tmpText);
    }

    public String getMap() {
	String map = "";
	for (int file = 0; file < m_nfiles; ++file) {
	    for (int rank = 0; rank < m_nranks; ++rank) {
		map += m_squares[file][rank].getMapSymbol();
	    }
	}
	return map;
    }

    public void showMap(String notation, String map, boolean singlestep, boolean forward) {
        showMapHelper(notation, map, singlestep, forward);
    }

    public void showCurrentPosition(String notation, boolean singlestep) {
        showMapHelper(notation, null, singlestep, true);
    }

    // If map is null, then we are showing the "current" position
    private void showMapHelper(String notation, final String map, boolean singlestep, boolean forward) {
        cancelAnimation();
	if (map != null && m_selectedPiece != null) {
	    m_selectedPiece.getSquare().setSelected(false);
	    setMoveableSquares(false);
	    m_selectedPiece = null;
	}
	int i = 0, file, rank;
        boolean validsinglemove = true;
        Square fromsquare = null, fromsquare2 = null;
        Square tosquare = null, tosquare2 = null;
        Piece prevpiece = null, prevpiece2 = null;
        Piece prevfrompiece = null, prevfrompiece2 = null;
        Piece movepiece = null, movepiece2 = null;
	Piece[][] oldpieces = new Piece[m_nfiles][m_nranks];
	for (file = 0; file < m_nfiles; ++file) {
	    oldpieces[file] = new Piece[m_nranks];
	    for (rank = 0; rank < m_nranks; ++rank) {
                oldpieces[file][rank] = m_squares[file][rank].getDisplayPiece();
            }
        }
	for (file = 0; file < m_nfiles; ++file) {
	    for (rank = 0; rank < m_nranks; ++rank) {
                if (map != null) {
                    m_squares[file][rank].displayMapSymbol(map.charAt(i++));
                } else {
                    m_squares[file][rank].displayCurrentPiece();
                }
                
                Piece prev = oldpieces[file][rank];
                Piece post = m_squares[file][rank].getDisplayPiece();
                if (validsinglemove) {
                    // Piece departed this square
                    if (prev != null && post == null) {
                        if (fromsquare != null) {
                            if (fromsquare2 == null) {
                                fromsquare2 = m_squares[file][rank];
                                prevfrompiece2 = prev;                                
                            } else {
                                validsinglemove = false;
                            }
                        } else {
                            fromsquare = m_squares[file][rank];
                            prevfrompiece = prev;
                        }
                    }
                    // Piece entered an empty square
                    else if (prev == null && post != null) {
                        if (movepiece != null) {
                            if (movepiece2 == null) {
                                tosquare2 = m_squares[file][rank];
                                prevpiece2 = prev;
                                movepiece2 = post;
                            } else {
                                validsinglemove = false;
                            }
                        } else {
                            tosquare = m_squares[file][rank];
                            prevpiece = prev;
                            movepiece = post;
                        }
                    }
                    // Piece captured
                    else if (prev != null && post != null &&
                             prev.getColor() != post.getColor()) {
                        if (forward) {
                            if (movepiece != null) {
                                validsinglemove = false;
                            } else {
                                tosquare = m_squares[file][rank];
                                prevpiece = prev;
                                movepiece = post;
                            }
                        } else {
                            fromsquare = m_squares[file][rank];
                        }
                    }
                }
	    }
	}
        if (singlestep && validsinglemove && fromsquare != null && tosquare != null && movepiece != null) {
            // Try to deal with castling, where two pieces of the same
            // color are moving at the same time
            if (fromsquare2 != null && tosquare2 != null && movepiece2 != null &&
                movepiece2.getColor() == movepiece.getColor()) {
                tosquare.setDisplayedPiece(prevpiece);
                tosquare2.setDisplayedPiece(prevpiece2);
                // Make sure the froms are correct
                if (movepiece.getMapSymbol() != prevfrompiece.getMapSymbol()) {
                    Square tmp = fromsquare2;
                    fromsquare2 = fromsquare;
                    fromsquare = tmp;
                }
                animatePiece(movepiece, fromsquare, tosquare,
                             movepiece2, fromsquare2, tosquare2, new Command() {
                                     final public void execute() {
                                         int i = 0;
                                         for (int file = 0; file < m_nfiles; ++file) {
                                             for (int rank = 0; rank < m_nranks; ++rank) {
                                                 if (map != null) {
                                                     m_squares[file][rank].displayMapSymbol(map.charAt(i++));
                                                 } else {
                                                     m_squares[file][rank].displayCurrentPiece();
                                                 }
                                             }
                                         }
                                     }
                                 });
            }
            // Detect en passant going forward - the ambiguity is
            // there are two squares where a piece is "leaving",
            // but only one square where a piece "arrives".
            else if (forward && prevfrompiece != null && prevfrompiece2 != null && movepiece2 == null &&
                     prevfrompiece instanceof Pawn &&
                     prevfrompiece2 instanceof Pawn) {
                // Swap tosquares if needed
                if (tosquare2 != null && tosquare.getDisplayPiece() != null &&
                    tosquare2.getDisplayPiece() != null &&
                    movepiece.getColor() != tosquare.getDisplayPiece().getColor() &&
                    movepiece.getColor() == tosquare2.getDisplayPiece().getColor()) {
                    tosquare = tosquare2;
                }
                // Swap fromsquares if needed
                if (movepiece.getColor() != prevfrompiece.getColor() &&
                    movepiece.getColor() == prevfrompiece2.getColor()) {
                    Square tmp = fromsquare;
                    fromsquare = fromsquare2;
                    fromsquare2 = tmp;
                    Piece tmp2 = prevfrompiece;
                    prevfrompiece = prevfrompiece2;
                    prevfrompiece2 = tmp2;
                }
                fromsquare2.setDisplayedPiece(prevfrompiece2);
                tosquare.setDisplayedPiece(prevpiece);
                animatePiece(movepiece, fromsquare, tosquare, new Command() {
                        final public void execute() {
                            int i = 0;
                            for (int file = 0; file < m_nfiles; ++file) {
                                for (int rank = 0; rank < m_nranks; ++rank) {
                                    if (map != null) {
                                        m_squares[file][rank].displayMapSymbol(map.charAt(i++));
                                    } else {
                                        m_squares[file][rank].displayCurrentPiece();
                                    }
                                }
                            }
                        }
                    });
            }

            // Detect en passant going backward - ambiguity here
            // is that there are two empty squares where a piece
            // enters, but only one square that the piece is leaving
            else if (!forward && movepiece2 != null && fromsquare2 == null &&
                     movepiece instanceof Pawn &&
                     movepiece2 instanceof Pawn &&
                     movepiece.getColor() != movepiece2.getColor()) {
                if (movepiece.getColor() == prevfrompiece.getColor()) {
                    tosquare.setDisplayedPiece(prevpiece);
                    animatePiece(movepiece, fromsquare, tosquare, new Command() {
                            final public void execute() {
                                int i = 0;
                                for (int file = 0; file < m_nfiles; ++file) {
                                    for (int rank = 0; rank < m_nranks; ++rank) {
                                        if (map != null) {
                                            m_squares[file][rank].displayMapSymbol(map.charAt(i++));
                                        } else {
                                            m_squares[file][rank].displayCurrentPiece();
                                        }
                                    }
                                }
                            }
                        });
                } else {
                    tosquare2.setDisplayedPiece(prevpiece2);
                    animatePiece(movepiece2, fromsquare, tosquare2, new Command() {
                            final public void execute() {
                                int i = 0;
                                for (int file = 0; file < m_nfiles; ++file) {
                                    for (int rank = 0; rank < m_nranks; ++rank) {
                                        if (map != null) {
                                            m_squares[file][rank].displayMapSymbol(map.charAt(i++));
                                        } else {
                                            m_squares[file][rank].displayCurrentPiece();
                                        }
                                    }
                                }
                            }
                        });
                }
            }
                
            // Detect pawn promotion            
            else if (prevfrompiece != null && movepiece.getMapSymbol() != prevfrompiece.getMapSymbol()) {
                final Square commandToSquare = tosquare;
                tosquare.setDisplayedPiece(prevpiece);
                animatePiece(prevfrompiece, fromsquare, tosquare, new Command() {
                        final public void execute() {
                            int i = 0;
                            for (int file = 0; file < m_nfiles; ++file) {
                                for (int rank = 0; rank < m_nranks; ++rank) {
                                    if (map != null) {
                                        m_squares[file][rank].displayMapSymbol(map.charAt(i++));
                                    } else {
                                        m_squares[file][rank].displayCurrentPiece();
                                    }
                                }
                            }
                        }
                    });
            }
            // Standard move
            else if (movepiece2 == null) {
                tosquare.setDisplayedPiece(prevpiece);
                animatePiece(movepiece, fromsquare, tosquare, new Command() {
                        final public void execute() {
                            int i = 0;
                            for (int file = 0; file < m_nfiles; ++file) {
                                for (int rank = 0; rank < m_nranks; ++rank) {
                                    if (map != null) {
                                        m_squares[file][rank].displayMapSymbol(map.charAt(i++));
                                    } else {
                                        m_squares[file][rank].displayCurrentPiece();
                                    }
                                }
                            }
                        }
                    });
            }
        }
        if (map != null) {
            if (m_logPanel != null) {
                // safari hack fix                
                if (notation == null || notation == "") {
                    notation = " ";
                }
                m_logPanel.getHeaderTextAccessor().setText(notation);
            }
            m_isCurrent = false;
            m_drawButton.setEnabled(false);
            m_resignButton.setEnabled(false);
            m_aCastleButton.setEnabled(false);
            m_hCastleButton.setEnabled(false);
            m_statusLabel.setText("Browsing game history.");
        } else {
            m_isCurrent = true;
            if (m_logPanel != null || notation == "") {
                // safari hack fix
                if (notation == null || notation == "") {
                    notation = " ";
                }
                m_logPanel.getHeaderTextAccessor().setText(notation);
            }
            updateStatusLabel();
        }
    }

    public void animatePiece(Piece piece, Square from, Square to,
                             Piece secondPiece, Square secondFrom, Square secondTo,
                             Command postcommand) {
        if (m_pieceAnimation != null) {
            m_pieceAnimation.animate(piece, from, to, secondPiece, secondFrom, secondTo,
                                     postcommand);
        } else {
            to.setDisplayedPiece(piece);
            if (secondPiece != null) {
                secondTo.setDisplayedPiece(secondPiece);
            }
            if (postcommand != null) {
                postcommand.execute();
            }
        }
    }
    
    public void animatePiece(Piece piece, Square from, Square to, Command postcommand) {
        if (m_pieceAnimation != null) {
            m_pieceAnimation.animate(piece, from, to, postcommand);
        } else {
            to.setDisplayedPiece(piece);
            if (postcommand != null) {
                postcommand.execute();
            }
        }
    }

    public void animatePiece(Piece piece, Square from, Square to) {
        if (m_pieceAnimation != null) {
            m_pieceAnimation.animate(piece, from, to);
        } else {
            to.setDisplayedPiece(piece);
        }
    }
    

    public void cancelAnimation() {
        if (m_pieceAnimation != null) {
            m_pieceAnimation.cancel();
        }
    }

    public void centerElement(Element element) {
    	DOM.setStyleAttribute(element, "marginLeft", "auto");
	DOM.setStyleAttribute(element, "marginRight", "auto");
    }

    //////////////////////////////////////////////////

    private final String m_boardString;

    /// Result of the game
    private int m_result;
    static public final int ONGOING = 0;
    static public final int WHITEWON = 1;
    static public final int BLACKWON = 2;
    static public final int WHITEOFFEREDDRAW = 3;
    static public final int BLACKOFFEREDDRAW = 4;
    static public final int DRAW = 5;

    // Number of ranks on the board
    private int m_nranks;

    // Number of files on the board
    private int m_nfiles;
    
    // Color of the current player
    private int m_color;

    // Current turn
    private int m_turn;    

    // Starting half move to display (-1 if not specified)
    private int m_startPly;
    
    // Whether the board is still loading the initial PGN
    private boolean m_loading;
    
    // Currently selected piece
    private Piece m_selectedPiece;

    // The (cached) movelist of the currently selected piece
    private ArrayList<Square> m_selectedMovelist;

    // The piece that was last moved
    private Piece m_lastMovedPiece;

    // Whether the indicated color is allowed to play
    private boolean[] m_canPlay;

    // Player names. First name isn't currently used
    private String m_whiteFirstName, m_whiteLastName;
    private String m_blackFirstName, m_blackLastName;

    // Array of squares for the board
    private Square[][] m_squares;

    // All the pieces in the game
    private ArrayList<ArrayList<Piece>> m_allPieces;

    // The two kings of the game
    private King[] m_kings;

    // Whether each side can potentially castle. The integers stored
    // here are the files of the rooks with which the king can castle,
    // stored in order of increasing file.
    private int[][] m_canPotentiallyCastle;

    // Whether the current color can castle on the a-side
    private boolean m_canACastle;

    // Whether the current color can castle on the h-side
    private boolean m_canHCastle;

    // Initial set up of the board in FEN notation
    private String m_fenString;

    // Whether the board display is current to the last move  
    private boolean m_isCurrent;

    // Representation of the game in PGN form
    private String m_pgn;
    
    //////////////////////////////////////////////////
    // Database post variables
    //////////////////////////////////////////////////

    // Authorization key for the session
    private String m_authkey;

    // Userid of the player
    private String m_userid;

    // Post hash, used to verify the content hasn't changed
    private String m_posthash;

    // Copy of the data we attempted to post
    private String m_postData;

    // Deferred command to run upon successful post
    private Command m_updateCommand;

    // Deferred notation to update
    private String m_updateNotation;

    // URL for postmove.php
    private final String m_postmoveURL;

    // URL for update.php
    private final String m_updateURL;
    
    // Game timer
    GameTimer m_timer;

    // Interval between timer updates
    static int m_timerInterval = 0;
    
    //////////////////////////////////////////////////
    // User interface elements and handling
    //////////////////////////////////////////////////

    // Vertical panel holding everything
    private TightGrid m_vpanel;
    
    // Grid used to layout the squares
    private TightGrid m_grid;
    
    // Absolute panel containing the grid
    private AbsolutePanel m_gridPanel;

    // Grids used to layout the labels
    private TightGrid m_topFileLabelGrid, m_bottomFileLabelGrid, m_leftRankLabelGrid, m_rightRankLabelGrid;

    // Status label indicating move freedom
    private TextArea m_statusLabel;

    // Stores game log in algebraic notation
    private GameLogTable m_gameLogTable;
    private GameLogParagraph m_gameLogParagraph;

    // Spin panel used to block the UI during post request
    private SpinPanel m_spinner;

    // Button to offer a draw
    private SizedPushButton m_drawButton;

    // Button to resign
    private SizedPushButton m_resignButton;

    // Button for castling
    private SizedPushButton m_aCastleButton;
    private SizedPushButton m_hCastleButton;

    // Button to flip board
    private SizedPushButton m_flipButton;

    // Game navigation buttons
    private SizedPushButton m_startButton;
    private SizedPushButton m_rewindButton;
    private SizedPushButton m_forwardButton;
    private SizedPushButton m_endButton;

    // PGN download button
    private SizedPushButton m_pgnButton;
    
    // Disclosure panel showing the last move and hiding the gamelog
    private DisclosurePanel m_logPanel;

    // Event information and label
    private String m_eventString;
    private String m_siteString;
    private String m_dateString;
    private String m_roundString;
    private Label m_eventLabel;
    private Label m_siteLabel;
    
    // Labels for each side
    private Label m_blackLabel;
    private Label m_whiteLabel;
	    
    // Flags passed in from the browser which control behaviour
    private int m_optionFlags;
    static public final int LEFTRANKLABEL = 1;
    static public final int TOPFILELABEL = 2;
    static public final int RIGHTRANKLABEL = 4;
    static public final int BOTTOMFILELABEL = 8;
    static public final int EXPANDGAMELOG = 16;
    static public final int EVENTLABEL = 32;
    static public final int SITELABEL = 64;
    static public final int PARAGRAPHLOG = 128;
    static public final int ANIMATION = 256;    

    // Animator
    private PieceAnimation m_pieceAnimation;
}
