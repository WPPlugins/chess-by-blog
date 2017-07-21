=== Chess By Blog ===
Contributors: levork
Tags: chess post game pgn fen
Requires at least: 2.0.4
Tested up to: 3.0
Stable tag: 1.1.5

Chess By Blog is a plugin for WordPress 2.0 that adds graphical chess
functionality to your WordPress blog. The plugin supports PGN
(Portable Game Notation), and allows interactive, non-persistent play
of chess problems. It also supports persistent play between users of
the blog.

Only Javascript is required in the client. The appearance of the chess
board is themable and controllable by a management panel.

Chess variants (including Fischer Random and Capablanca) are also
supported.

== Description ==
Chess By Blog is a plugin for WordPress 2.0 that adds graphical chess
functionality to your WordPress blog. It lets you do several things:

* PGN (Portable Game Notation) inserted into your post body will be
displayed as a graphical board, inline in the browser. The client side
code was written using the Google Web Toolkit (GWT), resulting in
Javascript (no Java required) that has been tested in Internet
Explorer 6-9, Mozilla/Firefox, and Safari. The graphical look of
the chessboard is controlled by themes, and can be easily controlled
in the management panel for the plugin.
* The board supports interactive play directly in the browser, in a
mode which does not update the post body. All rules of chess are
supported, include en passant, castling, and pawn promotion. This can
be used to present chess problems for readers to work through.
* Chess By Blog also supports a persistent game played between two
players, hence the name of the plugin. Readers that have edit
permissions on a post can take turns moving pieces, with the state of
the board saving to the WordPress database between moves. The plugin
uses WordPress and PHP security features to ensure that only moves
that come from the client chessboard code can update the database.

Chess By Blog is free software, distributed under the Expat license (a
copy of which is in chess-by-blog.php).

== Installation == 

Chess By Blog has been tested on WordPress 2.0.4 to 3.0; it is not
supported for earlier versions.

Before installing, you should back up your WordPress blog,
particularly if you intend to use the persistent game feature since
this will make changes directly to your database. The changes should
only be limited to the body of the post which contains the chess
board, and I'm not aware of any issues with the code - but better safe
than sorry.

If you are upgrading, it is recommended that you first disable the
plugin, then remove the files in the old chess-by-blog
subdirectory. This is optional: however, due to the names of the
Javascript scripts created by the Google Web Toolkit, new files tend
to have different names and the old ones aren't replaced - they just
take up disk space.

The top level contents of the zip folder look like this:

 /chess-by-blog
 +--readme.txt (this file!)
 +--chess-by-blog.php
 +--/chess-by-blog
 +----/themes
 +----(support files)
 +--/src

The top level chess-by-blog directory should be copied to your WordPress 
wp-content plugins directory. Optionally, unless you are building the 
client side code yourself, you can delete the src directory.

You should now be able to activate the plugin from the WordPress
Plugins administration panel.

== Usage ==
The plugin adds a "Chess By Blog" to the Quicktag panel that appears
when you edit or write a new post. (Unfortunately, at this time there
is no corresponding button in the visual editor.) Clicking the button
will pop open a new window which allows you to create a new chess
board. The fields of this board correspond almost directly to the
seven required tags of Portable Game Notation. For the purposes of the
plugin, all fields are optional; accepting the defaults will result in
code inserted into your post which will create an empty, closed
board. There are three types of boards that can be set up:

- Static, non playable board

    A static, non playable board can be created by leaving the
    "Wordpress ID" blank and by inserting algebraic move notation in
    the Movelist field. Note that for speed reasons, the input
    (particularly pawn moves) isn't fully error checked by the
    client code.

- Playable, non-persistent board

    An interactive, non-persistent board can be created by leaving the
    White and Black players at the default (i.e. "*"). Algebraic move
    notation can be optionally inserted in the move text to set up the
    board. Note that when the board is played interactively, full
    validation of each move is performed. This mode is ideal for
    presenting chess problems that can be worked through by readers.

- Playable, persistent board

    An interactive and persistent board can be created by selecting
    blog users for the White and Black players. These should
    correspond to the usernames of users of your blog who have at
    least Subscriber Roles.

Alternately, you can forego the usage of the Quicktags button and
insert PGN directly, enclosed in "div class="CBB-board" tags. The
client code will treat standard PGN as a static, non playable
board. Playable boards can be created by inserting [CBBWhiteId] and
[CBBBlackId] PGN tags with values as described above.

== PGN support ==
Chess By Blog fully supports the seven standard PGN tags. Comments
enclosed with curly braces are preserved, and will be shown at the top
of the game log table when the corresponding move is highlighted. In
addition, the FEN and JsCom (only the "startply" command is
recognized) tags are both supported, allowing specification of the
initial board layout. As an example of the JsCom tag, specifying
 [JsCom "startply 5"]
will preposition the board after White's 3rd move.

== Chess variant support ==
Chess By Blog fully supports FEN (Forsythe-Edwards Notation) via the
"FEN" tag in PGN. This allows specification of the initial board
layout. It also allows for handling of some chess variants.

Fischer Random Chess (or Chess960) is directly supported. The initial
layout of the pieces must be specified via a FEN tag. Castling is
supported correctly for this variant.

Capablanca chess is directly supported. The layout of the board must
be specified via a FEN tag, using "A" or "a" for the Archbishop and
"C" or "c" for the Chancellor; a 8x10 piece layout must be specified
as well.

There is limited support for arbitrary board sizes. Castling is
handled by always moving the King to the 'c' file if castling on the a
side, or the file to the left of the rightmost file if castling on the
other side; the Rook is always moved to the inner square. This
supports Fischer Random Chess and Capablanca Random Chess, but does
not support other variants. In addition, en passant always assumes the
pawns start on the 3rd rank.

== Customization ==
The look of the client side code is controlled by themes, which live
under chess-by-blog/themes. A new theme can be created by adding a
subdirectory which contains at least a file named "ChessByBlog.css".
This style sheet should contain the same classes as shown in the
default theme. In addition, there is a requirement that the CBB-square
and CBB-button contain width, height, and min-height attributes with
explicit pixel sizes. The client side code needs these in order to
correctly infer the sizes of chessboard squares and buttons.

== Building the client side code == 
This step is optional, but if you need to make fundamental changes to
the code that can't covered by changes to CSS, you'll need to edit
the Java source files in src/org/levork/gwt.

The client side code is Javascript which lives in the chess-by-blog
subdirectory, compiled from Java using the Google Web Toolkit. To
build this yourself, you will need to have the Google Web Toolkit
installed (at least version 1.7), after which the entire src directory
can be copied into your Google Web Toolkit folder. It is then
recommended that you run

webAppCreator -ignore org.levork.gwt.client.ChessByBlog

This will create the build.xml script necessary to compile using
ant. Running 'ant build' will compile the project into the
subdirectory war/chessbyblog. The files with cache in their name are
the Javascript files; these go in wp-content/plugins/chess-by-blog
(and the old ones should be deleted, most of them will have different
names).

== Known issues ==
- Switching from the code to visual editor may wreak havoc on the
div tags, despite the tags being protected by pre tags. This is an
issue with the visual editor.
- Internet Explorer may have issues with the preview of new themes in 
the management panel.
- There are some broken themes out there that will not work with this
plugin. In particular, if the theme does not make a call to
wp_footer() in footer.php, the Chess By Blog plugin will not be able
to emit the necessary lines of HTML that invoke the Javascript
code. (That theme may also break other plugins, and is definitely a
theme bug.) You may be able to fix this by inserting the necessary
wp_footer() call in the theme directory’s footer.php yourself – try
inserting:
 <?wp_footer();?>
at the beginning of the file. 
