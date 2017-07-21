/*
 * Plugin Name: Chess By Blog 
 * Plugin URI: http://www.levork.org/cbb 
 * Description: Allows you to view and play chess boards with others via
 *  your blog. 
 * Version: 1.1.4
 * Author: Julian Fong 
 * Author URI: http://www.levork.org/ 
 *  
 * Copyright (C) 2007-9 by Julian Fong (http://www.levork.org/).
 * All rights reserved.   
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.levork.gwt.client.client;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ChessByBlog implements EntryPoint {

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
	createBoards();
    }

    private static native void createBoards() /*-{
        var allDivs = $doc.getElementsByTagName("div");
	for (var i = 0; i < allDivs.length; i++) {
	    if (allDivs[i].className == "CBB-board") {
	       @org.levork.gwt.client.client.Board::Create(Lcom/google/gwt/user/client/Element;)(allDivs[i]);
	   }
	}
    }-*/;
}

