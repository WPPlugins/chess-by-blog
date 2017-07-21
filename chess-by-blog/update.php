<?php
error_reporting(E_ALL);
if (substr(__FILE__, -38) == "chess-by-blog/chess-by-blog/update.php") {
    require_once('../../../../wp-config.php');
} else {
    require_once('../../../wp-config.php');
}    

global $wpdb;

function moveerror($reason) {
    header("HTTP/1.1 403 Forbidden");
    error_log($reason);
    echo($reason);
    exit(403);
}

parse_str($HTTP_RAW_POST_DATA, $postargs);

if (!isset($postargs['postid']) || !isset($postargs['posthash'])) {
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

$post = get_post($id);
if (empty($post)) {
    moveerror('Invalid post - perhaps the post has been removed from the server.');
}

$content = $post->post_content;
$curboard = 0;
$curoffset = 0;
while (true) {
    $chessStart = strpos($content, '<div class="CBB-board"', $curoffset); // Find the begin tag
    if ($chessStart !== false) {
	$chessEnd = strpos($content, '</div', $chessStart); // Find the end tag
    }
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
if (strcmp($posthash, md5($indiv)) != 0) {
    echo md5($indiv) . $indiv;
}
?>
