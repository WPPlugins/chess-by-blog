<?php
/*
Plugin Name: Chess By Blog
Plugin URI: http://www.levork.org/cbb
Description: Allows you to view and play chess boards with others via your blog.
Version: 1.1.5
Author: Julian Fong
Author URI: http://www.levork.org/

Copyright (C) 2007-10 by Julian Fong (http://www.levork.org/).  All
rights reserved.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

// Pre-2.6 compatibility
if ( ! defined( 'WP_CONTENT_URL' ) )
      define( 'WP_CONTENT_URL', get_option( 'siteurl' ) . '/wp-content' );
if ( ! defined( 'WP_CONTENT_DIR' ) )
      define( 'WP_CONTENT_DIR', ABSPATH . 'wp-content' );
if ( ! defined( 'WP_PLUGIN_URL' ) )
      define( 'WP_PLUGIN_URL', WP_CONTENT_URL. '/plugins' );
if ( ! defined( 'WP_PLUGIN_DIR' ) )
      define( 'WP_PLUGIN_DIR', WP_CONTENT_DIR . '/plugins' );

// This guards against unintentional installation of the plugin in a
// chess-by-blog subdirectory
if (file_exists(WP_PLUGIN_DIR . '/chess-by-blog/chess-by-blog.php')) {
    define('CBB_URL', WP_PLUGIN_URL . '/chess-by-blog/chess-by-blog');
    define('CBB_THEME_ROOT', WP_PLUGIN_DIR . '/chess-by-blog/chess-by-blog/themes');
} else {
    define('CBB_URL', WP_PLUGIN_URL . '/chess-by-blog');
    define('CBB_THEME_ROOT', WP_PLUGIN_DIR . '/chess-by-blog/themes');
}

add_action('admin_footer', 'cbb_admin_footer');
add_action('wp_footer', 'cbb_footer');
add_filter('the_content', 'cbb_content', 0);
// Whether a reference to CSS and bootstrap javascript needs to be
// emitted in the content
$cbb_gwt_needs_load = false;

/**
 * Adds management page for cbb options
 */
add_action('admin_menu', 'cbb_menu');
function cbb_menu() {
    if (function_exists('add_options_page')) {
	add_options_page(__('Chess By Blog', 'cbb'), __('Chess By Blog', 'cbb'), 'manage_options', basename(__FILE__),	 'cbb_options');
    }
}

/**
 * Initializes default options for plugin
 */
function cbb_options_init() {
    add_option('cbb_leftranklabel', 1);
    add_option('cbb_topfilelabel', 1);
    add_option('cbb_rightranklabel', 1);
    add_option('cbb_bottomfilelabel', 1);
    add_option('cbb_expandgamelog', 1);
    add_option('cbb_paragraphlog', 0);
    add_option('cbb_animate', 1);
    add_option('cbb_eventlabel', 1);
    add_option('cbb_sitelabel', 1);
    add_option('cbb_theme', 'fselch-32');
    add_option('cbb_board_theme', 'wikipedia-32');    
    add_option('cbb_updateinterval', 60);
}

/** 
 * Draws the management page for cbb options
 */
function cbb_options() {

    cbb_load_theme(FALSE);

    // Initialize default values if necessary
    if (get_option('cbb_leftranklabel') == '') {
	cbb_options_init();
    }
    
    echo '<div class="wrap">';
    echo '<h2>' . __('Chess By Blog', 'cbb') . '</h2>';

    // Update options upon form submission
    if (isset($_POST["cbb_options"])) {
	$checked = 0;
	if($_POST["leftranklabel"] == 1)
	    $checked = 1;
	update_option('cbb_leftranklabel', $checked);
	$checked = 0;
	if($_POST["topfilelabel"] == 1)
	    $checked = 1;
	update_option('cbb_topfilelabel', $checked);
	$checked = 0;
	if($_POST["rightranklabel"] == 1)
	    $checked = 1;
	update_option('cbb_rightranklabel', $checked);
	$checked = 0;
	if($_POST["bottomfilelabel"] == 1)
	    $checked = 1;
	update_option('cbb_bottomfilelabel', $checked);
	$checked = 0;
	if($_POST["expandgamelog"] == 1)
	    $checked = 1;
	update_option('cbb_expandgamelog', $checked);
	$checked = 0;
	if($_POST["paragraphlog"] == 1)
	    $checked = 1;
	update_option('cbb_paragraphlog', $checked);
	$checked = 0;
	if($_POST["animate"] == 1)
	    $checked = 1;
	update_option('cbb_animate', $checked);
	$checked = 0;
	if($_POST["eventlabel"] == 1)
	    $checked = 1;
	update_option('cbb_eventlabel', $checked);
	$checked = 0;
	if($_POST["sitelabel"] == 1)
	    $checked = 1;
	update_option('cbb_sitelabel', $checked);
	update_option('cbb_theme', $_POST["theme"]);
        update_option('cbb_board_theme', $_POST["boardtheme"]);
        update_option('cbb_updateinterval', intval($_POST["updateinterval"]));
	echo '<div class="updated"><p><strong>' . __('Options saved.', 'cbb') . '</strong></p></div>';
    }
    
    echo '<form method="post">';
    echo '<fieldset class="options"><h3>' . __('Theme', 'cbb') . '</h3>';

    $themes_dir = @ opendir(CBB_THEME_ROOT);

    if ($themes_dir) {
	echo '<script type="text/javascript">cbbLoadTheme(\'' . get_option('cbb_theme') . '\');</script>';
	echo '<script type="text/javascript">cbbLoadBoardTheme(\'' . get_option('cbb_board_theme') . '\');</script>';
	?>
	<script type="text/javascript">
	{
	    function cbbFixWidthsAfterCSSLoad() {
		var square = document.getElementById("cbb_testsquare");
		var tester = document.createElement("div");
		square.appendChild(tester);
		tester.className = "CBB-square";
		tester.id = "cbb_styletester";
		cbbPollTester();
	    }
	    function cbbPollTester() {
		var tester = document.getElementById("cbb_styletester");
		if (tester.currentStyle)
		    h_style = tester.currentStyle["minHeight"];
		else if (document.defaultView && document.defaultView.getComputedStyle)
		    h_style = document.defaultView.getComputedStyle(tester, null).getPropertyValue("min-height");
		if (h_style == '0px')
		    setTimeout(cbbPollTester, 10);
		else {
		    var square = document.getElementById("cbb_testsquare");
		    square.removeChild(tester);
		    cbbFixWidths();
		}
	    }
	    function cbbFixElementSize(name, size) {
		var element = document.getElementById(name);
		if (element) {
		    element.height = size;
		    element.width = size;
		}
	    }
	    function cbbFixElementWidth(name, size) {
		var element = document.getElementById(name);
		if (element) {
		    element.width = size;
		}
	    }
	    function cbbFixElementHeight(name, size) {
		var element = document.getElementById(name);
		if (element) {
		    element.height = size;
		}
	    }
	    function cbbFixWidths() {
		var square = document.getElementById("cbb_testsquare");
		var h = square.clientHeight;
		var hh = square.offsetHeight;
		var h_style;
		if (square.currentStyle)
		    h_style = square.currentStyle["minHeight"];
		else if (document.defaultView && document.defaultView.getComputedStyle)
		    h_style = document.defaultView.getComputedStyle(square, null).getPropertyValue("min-height");
		if (h_style) {
		    cbbFixElementSize("cbb_demosquare1", h_style);
		    cbbFixElementSize("cbb_demosquare2", h_style);
		    cbbFixElementSize("cbb_demosquare3", h_style);
		    cbbFixElementSize("cbb_demosquare4", h_style);
		    cbbFixElementHeight("cbb_ranksquare1", h_style);
		    cbbFixElementHeight("cbb_ranksquare2", h_style);
		    cbbFixElementWidth("cbb_filesquarea", h_style);
		    cbbFixElementWidth("cbb_filesquareb", h_style);
		}
	    }
	    if (window.addEventListener) {
		window.addEventListener('load', cbbFixWidths, false);
	    } else if (window.attachEvent) {
		window.attachEvent('onload', cbbFixWidths);
	    }
	}
	</script>
              <table><tr><td>
<?php
	$current_theme = get_option('cbb_theme');
	$current_theme_file = CBB_THEME_ROOT . '/' . $current_theme . '/Pieces.css';
	if (is_readable($current_theme_file)) {
	    $ct = get_theme_data($current_theme_file);
	    if ($ct) {
		echo '<h4>' . __('Current Piece Theme', 'cbb') . '</h4>';
		echo '<b>' . $ct['Title'] . ' ' . $ct['Version'] . '</b> ' . __("by", 'cbb') . ' ' . $ct['Author'];
		echo '<p>' . $ct['Description'] . '</p>';
		echo '<p>' . __('All of this theme&#8217;s files are located in', 'cbb') . '<code>' . dirname($current_theme_file) . '.</code>';
	    } else {
		echo '<h4>' . __('Current Theme', 'cbb') . ':' . $current_theme . '</h4>';
	    }
	} else {
	    echo '<h4>' . __('Current Theme', 'cbb') . ':' . $current_theme . '</h4>';
	}
?></td><td><?php
	$current_theme = get_option('cbb_board_theme');
	$current_theme_file = CBB_THEME_ROOT . '/' . $current_theme . '/Board.css';
	if (is_readable($current_theme_file)) {
	    $ct = get_theme_data($current_theme_file);
	    if ($ct) {
		echo '<h4>' . __('Current Board Theme', 'cbb') . '</h4>';
		echo '<b>' . $ct['Title'] . ' ' . $ct['Version'] . '</b> ' . __("by", 'cbb') . ' ' . $ct['Author'];
		echo '<p>' . $ct['Description'] . '</p>';
		echo '<p>' . __('All of this theme&#8217;s files are located in', 'cbb') . '<code>' . dirname($current_theme_file) . '.</code>';
	    } else {
		echo '<h4>' . __('Current Theme', 'cbb') . ':' . $current_theme . '</h4>';
	    }
	} else {
	    echo '<h4>' . __('Current Theme', 'cbb') . ':' . $current_theme . '</h4>';
	}
?></td></tr><tr><td><?php        

	while (($theme_dir = readdir($themes_dir)) !== false) {
	    if ( is_dir(CBB_THEME_ROOT . '/' . $theme_dir) && is_readable(CBB_THEME_ROOT . '/' . $theme_dir) ) {
		if ( $theme_dir{0} == '.' || $theme_dir == '..' || $theme_dir == 'CVS' )				continue;
		$stylish_dir = @ opendir(CBB_THEME_ROOT . '/' . $theme_dir);
		while ( ($theme_file = readdir($stylish_dir)) !== false ) {
		    if ( $theme_file == 'Pieces.css' ) {
			$theme_files[] = $theme_dir . '/' . $theme_file;
		    }
		    if ( $theme_file == 'Board.css' ) {
			$board_theme_files[] = $theme_dir . '/' . $theme_file;
		    }
                    
		}
		@closedir($stylish_dir);
	    }
	}
	@closedir($themes_dir);

	sort($theme_files);
	sort($board_theme_files);

        echo '<h4>' . __('Available piece themes', 'cbb') . '</h4>';
	echo '<select id="cbb_theme" name="theme" onchange="cbbLoadTheme(this.options[this.selectedIndex].value);cbbFixWidthsAfterCSSLoad()">';
	foreach ( (array) $theme_files as $theme_file ) {
	    if ( !is_readable(CBB_THEME_ROOT . "/$theme_file") ) {
		continue;
	    }
	    $theme_data = get_theme_data(CBB_THEME_ROOT . "/$theme_file");
	    $name	 = $theme_data['Name'];
	    $title	 = $theme_data['Title'];
	    $description = wptexturize($theme_data['Description']);
	    $version	 = $theme_data['Version'];
	    $author	 = $theme_data['Author'];
	    $template	 = $theme_data['Template'];
	    $stylesheet	 = dirname($theme_file);
	    
	    echo '<option id="cbb_theme" name="theme" value="' . $stylesheet . '" '. (get_option('cbb_theme') == $stylesheet ? 'selected' : '') . '/>' . $name . '</option></p>';	    
	}
	echo '</select>';
?></td><td><?php
        echo '<h4>' . __('Available board themes', 'cbb') . '</h4>';
	echo '<select id="cbb_board_theme" name="boardtheme" onchange="cbbLoadBoardTheme(this.options[this.selectedIndex].value);cbbFixWidthsAfterCSSLoad()">';
	foreach ( (array) $board_theme_files as $theme_file ) {
	    if ( !is_readable(CBB_THEME_ROOT . "/$theme_file") ) {
		continue;
	    }
	    $theme_data = get_theme_data(CBB_THEME_ROOT . "/$theme_file");
	    $name	 = $theme_data['Name'];
	    $title	 = $theme_data['Title'];
	    $description = wptexturize($theme_data['Description']);
	    $version	 = $theme_data['Version'];
	    $author	 = $theme_data['Author'];
	    $template	 = $theme_data['Template'];
	    $stylesheet	 = dirname($theme_file);
	    
	    echo '<option id="cbb_board_theme" name="boardtheme" value="' . $stylesheet . '" '. (get_option('cbb_board_theme') == $stylesheet ? 'selected' : '') . '/>' . $name . '</option></p>';	    
	}
	echo '</select>';
        
    }
    ?></td></tr></table>
	<p><?php echo(__('Sample', 'cbb'));?>:</p>
        <table>
	   <tr><td colspan="2" class="CBB-side-label">Mayer/Black</td></tr>
	   <tr><td colspan="2" class="CBB-file-label">&nbsp;</td></tr>	     
	   <tr>
  	     <td>
	       <table cellspacing="0">
	         <tr><td id="cbb_ranksquare2" class="CBB-rank-label">2</td></tr>
	         <tr><td id="cbb_ranksquare1" class="CBB-rank-label">1</td></tr>
  	       </table>
	     </td>
	     <td>
	       <table class="CBB-chessboard" cellspacing="0">
	         <tr>
	           <td id="cbb_demosquare1" class="CBB-whiteSquare CBB-moveableSquare CBB-blackB"></td>
	           <td id="cbb_demosquare2" class="CBB-blackP CBB-blackSquare"></td>
	         </tr>
	         <tr>
	           <td id="cbb_demosquare3" class="CBB-whiteR CBB-blackSquare CBB-selectedSquare"></td>
	           <td id="cbb_demosquare4" class="CBB-whiteQ CBB-whiteSquare"></td>
	         </tr>
	       </table>
	     </td>
	     <td>
	       <div id="cbb_testsquare" style="visibility:hidden" class="CBB-square"></div>
	     </td>
	   </tr>
	   <tr>
	     <td>&nbsp;</td>
	     <td>
	       <table cellspacing="0">
	         <tr>
	           <td id="cbb_filesquarea" class="CBB-file-label">a</td>
	           <td id="cbb_filesquareb" class="CBB-file-label">b</td>
	         </tr>
	       </table>
	     </td>
	   </tr>
	   <tr><td colspan="2" class="CBB-side-label">Fong/White</td></tr>
	 </table>
       </fieldset>
       <fieldset class="options"><h3><?php echo(__('Board display', 'cbb'));?></h3>
	 <table>
	 <tr>
	 <td>
	   <input type="checkbox" id="cbb_leftranklabel" name="leftranklabel" value="1"
	     <?php echo(get_option('cbb_leftranklabel') == 1 ? 'checked="checked"' : '');?>/>
	   <label for="cbb_leftranklabel">
	     <?php echo(__('Show rank labels on left', 'cbb'));?></label>
         </td>
         <td>
	   <input type="checkbox" id="cbb_rightranklabel" name="rightranklabel" value="1"
	     <?php echo(get_option('cbb_rightranklabel') == 1 ? 'checked="checked"' : '');?>/>
	   <label for="cbb_rightranklabel">
	     <?php echo(__('Show rank labels on right', 'cbb'));?></label>
         </td>
	 </tr>
         <tr>
         <td>
	   <input type="checkbox" id="cbb_topfilelabel" name="topfilelabel" value="1"
	     <?php echo(get_option('cbb_topfilelabel') == 1 ? 'checked="checked"' : '');?>/>
	   <label for="cbb_topfilelabel">
	     <?php echo(__('Show file labels on top', 'cbb'));?></label>
         </td>
         <td>
	   <input type="checkbox" id="cbb_bottomfilelabel" name="bottomfilelabel" value="1"
	     <?php echo(get_option('cbb_bottomfilelabel') == 1 ? 'checked="checked"' : '');?>/>
	   <label for="cbb_bottomfilelabel">
	     <?php echo(__('Show file labels on bottom', 'cbb'));?></label>
	 </td>
         </tr>
	 <tr>
         <td>								 
           <input type="checkbox" id="cbb_expandgamelog" name="expandgamelog" value="1"
	     <?php echo(get_option('cbb_expandgamelog') == true ? 'checked="checked"' : '')?>/>
	   <label for="cbb_expandgamelog">
             <?php echo(__('Expand game log by default', 'cbb'));?></label>
         </td>
         <td>								 
           <input type="checkbox" id="cbb_paragraphlog" name="paragraphlog" value="1"
	     <?php echo(get_option('cbb_paragraphlog') == true ? 'checked="checked"' : '')?>/>
	   <label for="cbb_paragraphlog">
             <?php echo(__('Show game log as paragraph', 'cbb'));?></label>
         </td>
         </tr>
         <tr>
         <td>
	   <input type="checkbox" id="cbb_eventlabel" name="eventlabel" value="1"
	     <?php echo(get_option('cbb_eventlabel') == 1 ? 'checked="checked"' : '');?>/>
	   <label for="cbb_eventlabel">
	     <?php echo(__('Show event and round', 'cbb'));?></label>
         </td>
         <td>
	   <input type="checkbox" id="cbb_sitelabel" name="sitelabel" value="1"
	     <?php echo(get_option('cbb_sitelabel') == 1 ? 'checked="checked"' : '');?>/>
	   <label for="cbb_sitelabel">
	     <?php echo(__('Show site and date', 'cbb'));?></label>
	 </td>
         </tr>
	 <tr>
         <td>								 
           <input type="checkbox" id="cbb_animate" name="animate" value="1"
	     <?php echo(get_option('cbb_animate') == true ? 'checked="checked"' : '')?>/>
	   <label for="cbb_animate">
             <?php echo(__('Animate pieces', 'cbb'));?></label>
         </td>
         <td>
	   &nbsp;
         </td>
         </tr>
								 
	</table>							 
       </fieldset>
       <fieldset class="options"><h3><?php echo(__('Automatic Update', 'cbb'));?></h3>
	<table>
	  <tr>
	    <td>
	      <?php echo(__('Live games can poll for an update and automatically refresh the board (i.e. after an opponent has moved). This is only in effect for logged in users who have permission to play on the board. Specify the refresh interval in seconds, or zero to disable refresh completely. Note: although the poll is low bandwidth, setting this refresh interval to a low number of seconds is still not advisable if you have many simultaneous games.', 'cbb'));?>
	    </td>
	  </tr>
	  <tr>
	    <td>
	      <label for="cbb_updateinterval">
		<?php echo(__('Refresh interval (in seconds)', 'cbb'));?></label>
	      <input type="text" id="cbb_updateinterval" name="updateinterval" value="<?php echo(get_option('cbb_updateinterval'));?>"/>
	    </td>
	  </tr>
	</table>
      </fieldset>
       <p class="submit"><input type="submit" name="Submit" value="<?php echo(__('Update Options', 'cbb'));?>"/></p>
       <input type="hidden" name="cbb_options" value="SET" />
      </form>
    </div>
<?php
}

/**
 * Adds a "Chess" button to the toolbar in edit mode, which pops up a
 * dialog box allowing you to insert the correctly formatted chess tags
 * into your blog entry.  Based on the Edit Button Framework by Owen
 * Winkler, http://www.asymptomatic.net/wp-hacks
 */
function cbb_admin_footer() {
    if (strpos($_SERVER['REQUEST_URI'], 'post-new.php') || 
	strpos($_SERVER['REQUEST_URI'], 'post.php')) {
?>
<script language="JavaScript" type="text/javascript">
function cbb_button_handler(URL) {
    var url = '<?php echo CBB_URL . "/dialog.php" ?>';
    /* Use thickbox if it exists */
    if (typeof tb_show=='function') {
        tb_show("Chess By Blog", url + '?TB_iframe=true&amp;height=700&amp;width=500');
    } else {
        var chessDialog = window.open(url,"chessDialog", "toolbar=0,scrollbars=1,location=0,statusbar=0,menubar=0,resizable=1,width=500,height=500");
        if(chessDialog.opener == null) chessDialog.opener = self;
        chessDialog.opener = self;
        chessDialog.focus();
    }
}
function cbb_insert_text(pgn) {
    edInsertContent(edCanvas, '<div class="CBB-board" style="display:none;">\n' + pgn + '</div>');
}
var toolbar = document.getElementById("ed_toolbar");
<?php
    edit_insert_button("Chess By Blog", "cbb_button_handler", "cbb_admin");
?>
</script>
<?php
      }
}

if (!function_exists('edit_insert_button')) {
    function edit_insert_button($caption, $js_onclick, $title = '') {
?>
	if(toolbar) {
	    var theButton = document.createElement('input');
	    theButton.type = 'button';
	    theButton.value = '<?php echo $caption; ?>';
	    theButton.onclick = <?php echo $js_onclick; ?>;
	    theButton.className = 'ed_button';
	    theButton.title = "<?php echo $title; ?>";
	    theButton.id = "<?php echo "ed_{$caption}"; ?>";
	    toolbar.appendChild(theButton);
	}
<?php
      }
}

/**
 * Inserts javascript into current page to load the theme.
 * Either the theme is loaded immediately once, or a javascript
 * function is inserted for deferred load. The immediate version
 * is used on pages with boards, so it needs to be minified.
 */
function cbb_load_theme($immediate) {
    if ($immediate == TRUE) {
?>
<script type="text/javascript">
    var c = document.createElement('link');
    c.id='cbbthemesheet';
    c.type='text/css';
    c.rel ='stylesheet';
    c.href = '<?php echo(CBB_URL); ?>/themes/<?php echo get_option('cbb_theme');?>/Pieces.css';
    document.getElementsByTagName("head")[0].appendChild(c);
    c = document.createElement('link');
    c.id='cbbboardthemesheet';
    c.type='text/css';
    c.rel ='stylesheet';
    c.href = '<?php echo(CBB_URL); ?>/themes/<?php echo get_option('cbb_board_theme');?>/Board.css';
    document.getElementsByTagName("head")[0].appendChild(c);
    c = document.createElement('link');
    c.id='cbbmainthemesheet';
    c.type='text/css';
    c.rel ='stylesheet';
    c.href = '<?php echo(CBB_URL); ?>/themes/ChessByBlog.css';
    document.getElementsByTagName("head")[0].appendChild(c);   
</script>
<?php
    } else {
?>
    <script type="text/javascript">
	function cbbLoadTheme(themeName) {
	    var oldCssNode = document.getElementById('cbbthemesheet');
	    var cssNode = document.createElement('link');
	    cssNode.id='cbbthemesheet';
	    cssNode.type='text/css';
	    cssNode.rel ='stylesheet';
	    cssNode.href = '<?php echo(CBB_URL); ?>/themes/' + themeName + '/Pieces.css';
	    if (oldCssNode) {
		document.getElementsByTagName("head")[0].replaceChild(cssNode, oldCssNode);
	    } else {
		document.getElementsByTagName("head")[0].appendChild(cssNode);
	    };
	}
	function cbbLoadBoardTheme(themeName) {
	    var oldCssNode = document.getElementById('cbbboardthemesheet');
	    var cssNode = document.createElement('link');
	    cssNode.id='cbbboardthemesheet';
	    cssNode.type='text/css';
	    cssNode.rel ='stylesheet';
	    cssNode.href = '<?php echo(CBB_URL); ?>/themes/' + themeName + '/Board.css';
	    if (oldCssNode) {
		document.getElementsByTagName("head")[0].replaceChild(cssNode, oldCssNode);
	    } else {
		document.getElementsByTagName("head")[0].appendChild(cssNode);
	    };
	}
    </script>
<?php
    }
}

/**
 * Inserts a reference to the ChessByBlog.js bootstrap script if needed at the
 * end of the HTML.
 */
function cbb_footer() {
    global $cbb_gwt_needs_load;
    if ($cbb_gwt_needs_load) {
	// Initialize default values if necessary
	if (get_option('cbb_leftranklabel') == '') {
	    cbb_options_init();
	}
	echo '<script type="text/javascript" src="' . CBB_URL . '/chessbyblog.nocache.js"></script>';
    }
}

/**
 * Computes the bitmask of options controlling client side behaviour
 */
function cbb_compute_optflags() {
    $optflags = 0;
    if (get_option('cbb_leftranklabel') == 1)	$optflags += 1;
    if (get_option('cbb_topfilelabel') == 1)	$optflags += 2;
    if (get_option('cbb_rightranklabel') == 1)	$optflags += 4;
    if (get_option('cbb_bottomfilelabel') == 1)	$optflags += 8;
    if (get_option('cbb_expandgamelog') == 1)	$optflags += 16;
    if (get_option('cbb_eventlabel') == 1)	$optflags += 32;
    if (get_option('cbb_sitelabel') == 1)	$optflags += 64;
    if (get_option('cbb_paragraphlog') == 1)	$optflags += 128;    
    if (get_option('cbb_animate') == 1)		$optflags += 256;
    return $optflags;
}

/**
 * Filters the post content by looking for chess marker tags and
 * altering the content if those tags are found.
 *
 * @param string $content The post content
 * @return string The filtered post content
 */
function cbb_content($content) {

    global $post, $cbb_gwt_needs_load, $user_ID, $user_identity, $user_login, $wp_version;
    
    if (is_feed()) {
	return $content;
    }

    $oldContent = $content;
    $newContent = "";
    $subboard = 0;
    while (true) {
	$chessStart = strpos($oldContent, '<div class="CBB-board"'); // Find the begin tag
	if ($chessStart === false) {
	    $newContent .= $oldContent;
	    return $newContent;
	}
	$chessEnd = strpos($oldContent, '</div', $chessStart); // Find the end tag
	if ($chessEnd === false) {
	    $newContent .= $oldContent;
	    return $newContent;
	}
	
	// Ensure the reference to gwt.js bootstrap script is emitted
	if (!$cbb_gwt_needs_load) {
	    cbb_load_theme(TRUE);
	    $cbb_gwt_needs_load = true;
	}

	$indivContentStart = strpos($oldContent, '>', $chessStart) + 1;
	$indiv = substr($oldContent, $indivContentStart, $chessEnd - $indivContentStart);
	
	// Inject a unique ID - this is used by the client side javascript
	// code. Add <pre> to avoid problems with the wpautop function.
	// The client will strip the pre tags
	$newContent .= substr($oldContent, 0, $chessStart + 22) .
	    ' id="CBB-board' . $post->ID . '-' . $subboard . '"' .
	    substr($oldContent, $chessStart + 22, $indivContentStart - ($chessStart + 22)) .
	    '<pre>' .
	    $indiv;
	
	// If the current user is able to play the game (by virtue of
	// being able to edit the post, and having their username appear
	// in the PGN block), we modify the div tag by injecting an ID
	// containing the post ID, and inserting three hidden input
	// fields: the first being a MD5 based authkey checked using
	// WordPress' nonce mechanism by postmove.php; the second being
	// the user identity, checked by the client code; and the last
	// being the MD5 of the current post, used to check the post for
	// alteration while posting
	get_currentuserinfo();
	
	if (current_user_can('read')) {
	    $authkey = wp_create_nonce('ChessByBlog' . $post->ID . $user_login); 
	    $newContent .= ' <input name="authkey" type="hidden" value="' . $authkey . '" />';
	    $newContent .= ' <input name="userid" type="hidden" value="' . $user_login . '" />';
	    $newContent .= ' <input name="posthash" type="hidden" value="' . md5($indiv) . '" />';	
	}
	// Options controlling client side behaviour
	$newContent .= ' <input name="cbbflags" type="hidden" value="' . cbb_compute_optflags() . '" />';
	$interval = get_option('cbb_updateinterval');
        if ($interval != 0) {
            $newContent .= ' <input name="cbbinterval" type="hidden" value="' . $interval . '" />';
        }
	$newContent .= "\n</pre>";
	$oldContent = substr($oldContent, $chessEnd);
	$subboard++;
    }
    return $newContent;
}

?>
