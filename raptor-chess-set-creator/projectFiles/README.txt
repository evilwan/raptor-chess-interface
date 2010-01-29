                         Raptor Set Creator README 
                 (c) 2010 Raptor Project (All rights reserved).
 
About
-----
    This is a program you can use to convert svg chess sets 
into pgn pieces of different sizes Raptor uses. First create a svg
set using a tool such as Inkscape. Create a directory for your new set.
Pieces should follow the naming convention: 
    wp.svg (White Pawn)
    bp.svg (Black Pawn)
    wb.svg (White BIshop)
    bb.svg (Black Bishop)
    wn.svg (White Knight)
    bn.svg (Black Knight)
    wr.svg (White Rook)
    br.svg (Black Rook)
    wq.svg (White Queen)
    bq.svg (Black Queen)
    wk.svg (White King)
    bk.svg (Black King)
You can view the existing sets in the set directory to get an idea of
how they were created. 

Guidelines
----------
	Make the size of the image fairly small. You can work on it in a large 
size like 500x500, but when you finally save it off scale it back. 
Inkscape has some options to scale proprtionally. Since its svg, you 
will not lose any detail when its scaled back. It just speeds up the 
conversion of svg to png greatly. Most of the sets use 100x100 or 
sizes close to that for this very reason. Anything below 200x200 will 
be pretty fast, but larger sizes really have speed issues in some 
operating systems. 

	Use square image sizes instead of rectangles. Also use an even number 
for the sizes. This allows for almost perfect scaling when the images 
are converted into pgn. 

	When I converted the svg sets in Raptor I tried to made all of the svg 
images in a particular set the same size. Then when I wanted a pawn to 
be smaller than another piece type I could just adjust it within the 
image and it came out scaled correctly in raptor. If you take this 
approach you can make pieces of different sizes, i.e. pawns smaller 
than other pieces and queens and kings a bit taller, etc. It will also 
prevent some pieces from taking up the entire square if that is not 
what you intended. In many of the sets the rooks were very prominent 
and I had to scale them back in size a bit so they looked nicer. You 
can also play around with the centering if you do this. In some sets I 
have pawns with a bit of margin underneath them. This has a nice 
effect in Raptor when the pieces are displayed. 

	If you do use images or effects in Inkscape you may need to embed the images
for them to appear. You can do this with Extensions -> Images -> Embed Images.
   
   
Usage
-----   
   After you have created your svg files you are now ready to convert them into
pgns. "./setCreator svgSourceDir" setName will begin the conversion. It may take
a few minutes to create your set. You must also be in the unzipped directory when
you run the program. It will not work if you path it.

   The following will create the Eyes chess set in Raptor.
   (Unix/OS X/Linux)
   1) open up a terminal
   2) cd to the RAPTOR_UNZIP_DIRECTORY
   3) ./setCreator set/Eyes Eyes
   (Windows)
   1) Open up a command promt. (windows key r type in cmd).
   2) cd to the RAPTOR_UNZIP_DIRECTORY
   3) setCreator set\Eyes Eyes.

Submitting a new set
--------------------
   After generation your set will appear in the target/setName directory. To test it
out you can copy this directory into your RAPTOR_APP/resources/set directory. Optionally
you can add a author.txt file containing your name/email and a license.txt file containing
the sets license. If you submit the set to the Raptor project zip up this directory and 
submit that. It will contain everything needed to add your set to the project.