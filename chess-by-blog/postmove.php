<?php
error_reporting(E_ALL);
if (substr(__FILE__, -40) == "chess-by-blog/chess-by-blog/postmove.php") {
    require_once('../../../../wp-config.php');
    require_once('../../../../wp-admin/admin.php');
} else {
    require_once('../../../wp-config.php');
    require_once('../../../wp-admin/admin.php');
}    

global $wpdb;

function moveerror($reason) {
    header("HTTP/1.1 403 Forbidden");
    echo($reason);
    exit(403);
}

// Legitimate input should look like this:
//
// move=e4&postid=CBB-board11&authkey=3fbcef11ab6d7d9f8373067a6858f9c0

parse_str($HTTP_RAW_POST_DATA, $postargs);

if (count($postargs) != 4 || !isset($postargs['postid']) || !isset($postargs['authkey']) || !isset($postargs['move']) || !isset($postargs['posthash'])) {
    moveerror('Invalid post syntax. Are you using the correct client?');
}

$postid = $postargs['postid'];

// Make sure postid starts with CBB-board, and strip that off to
// derive the actual wordpress post id
if (strpos($postid, 'CBB-board') != 0) {
    moveerror('Invalid post - perhaps the post has been removed from the server.');
}
$postid = substr($postid, 9);
$dash = strpos($postid, '-');
if ($dash === false) {
    $id = (int) $postid;
    $subboard = 0;
} else {
    $id = (int) substr($postid, 0, $dash);
    $subboard = (int) substr($postid, $dash + 1);
}

// Regenerate the authorization key and compare it to the one sent
$session_authkey = 'ChessByBlog' . $id . $user_login;
$authkey = $postargs['authkey'];
if (!wp_verify_nonce($authkey, $session_authkey)) {
    moveerror('Failed authentication.');
}

$move = $postargs['move'];

// Check the move for validity. Any attempt to post HTML or a long
// move is an error
if (preg_match('/[<>\\:]/', $move) || strlen($move) > 16) {
    moveerror('Invalid move (possibly due to internal client error).');
}

$post = get_post($id);
if (empty($post)) {
    moveerror('Invalid post - perhaps the post has been removed from the server.');
}
$content = $post->post_content;
$curboard = 0;
$curoffset = 0;
while (true) {
    $chessStart = strpos($content, '<div class="CBB-board"', $curoffset); // Find the begin tag
    $chessEnd = strpos($content, '</div', $chessStart); // Find the end tag
    if ($chessStart === false || $chessEnd === false) {
	moveerror('Post exists, but without chess tags. Try reloading your page.');
    } else if ($curboard >= $subboard) {
	break;
    } else {
	$curboard++;
	$curoffset = strpos($content, '>', $chessEnd);
	if ($curoffset === false) break;
	$curoffset++;
    }
}
if ($curboard > $subboard) {
    moveerror('Subboard does not exist within post.');
}

// Check whether board has changed by computing a hash on it
$indivContentStart = strpos($content, '>', $chessStart) + 1;
$indiv = substr($content, $indivContentStart, $chessEnd - $indivContentStart);
$posthash = $postargs['posthash'];
if (strcmp($posthash, md5($indiv)) !== 0) {
    moveerror('Post has been changed recently. Perhaps another move has taken place? Please reload this page and try again.');
}

// If the move is a draw, draw request, or draw refusal, update the
// Result tag
if (strcmp($move, "1/2-") == 0 ||
    strcmp($move, "-1/2") == 0 ||
    strcmp($move, "*") == 0) {
    $indiv = ereg_replace("\\[Result[^]]*\\]", "[Result &#34;" . $move . "&#34;]", $indiv);
 } else if (strcmp($move, "1/2 - 1/2") == 0) {
    $indiv .= ' ' . $move;
    $indiv = ereg_replace("\\[Result[^]]*\\]", "[Result &#34;" . $move . "&#34;]", $indiv);
} else {
    // Append move
    if (strncmp($move, "1.", 2) == 0) {
	$indiv .= $move;
    } else {
	$indiv .= ' ' . $move;
    }
    // Check for resignation or check mate. If that happens update the
    // result tag
    if (strpos($move, "Resigns") !== false) {
	// White resigned
	if (strpos($move, ".") !== false) {
	    $indiv = ereg_replace("\\[Result[^]]*\\]", "[Result &#34;0-1&#34;]", $indiv);
	} else {
	    $indiv = ereg_replace("\\[Result[^]]*\\]", "[Result &#34;1-0&#34;]", $indiv);
	}
    }
    else if (strpos($move, "++") !== false) {
	// White wins
	if (strpos($move, ".") !== false) {
	    $indiv = ereg_replace("\\[Result[^]]*\\]", "[Result &#34;1-0&#34;]", $indiv);
	} else {
	    $indiv = ereg_replace("\\[Result[^]]*\\]", "[Result &#34;0-1&#34;]", $indiv);
	}
    }
    // Check for stalemate, update result tag
    else if ($move{strlen($move) - 1} == '=') {
	$indiv = ereg_replace("\\[Result[^]]*\\]", "[Result &#34;1/2 - 1/2&#34;]", $indiv);
    }
}

$newContent = substr($content, 0, $indivContentStart) . $indiv . substr($content, $chessEnd);

$newpost = array();
$newpost['ID'] = (int) $id;
$newpost['post_content'] = $wpdb->escape($newContent);
$newpost['no_filter'] = true;

get_currentuserinfo();

// Check to see if user has capability to post unfiltered html. If
// not, disable the kses filter temporarily so that our div tags don't
// get mangled.
if (!current_user_can('unfiltered_html')) {
    kses_remove_filters();
}
if (wp_update_post($newpost) != $id) {
    // Restore kses filter
    kses_init();
    moveerror('Unable to update post in database');
} else {
    kses_init();
}    

// Return the newly updated md5 hash to the client plus the new
// content
echo md5($indiv) . $indiv;
?>
