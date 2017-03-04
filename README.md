# SVG2SCR
A Java Program to Create Eagle Scripts from SVGs. The Eagle Scripts draw polygons or wires from the paths in the SVG.

This only works on a specific kind of SVG. Here's how to produce it

1. Set Inkscape to output absolute coordinates
     (Shift+Ctrl+P >> Input/Output >> SVG Output >> Path string format: Absolute)
2. Turn both objects and strokes to paths
     (Ctrl+Alt+A) >> (Shift+Ctrl+C) >> (Ctrl+Alt+C)
3. Approximate curved paths with many small straight ones
     (Ctrl+Alt+A)
     (Extensions >> Modify Path >> Add Nodes)
     (Extensions >> Modify Path >> Flatten Beziers)
4. Polygons can't have holes in them, so they need to be split in parts.
     Draw a rectangle dividing the hole, like this: () => ([)]
     Select the rectangle and the polygon
     Use the Division command to split the polygon (Ctrl+/)
5. Jiggle all objects to make sure they're saved with absolute coordinates
     (Ctrl+Alt+A) >> move everything left then right using the arrow keys
6. Break apart paths
     (Ctrl+Shift+K)
7. Ungroup all objects
     (Ctrl+Alt+A) >> (Shift+Ctrl+G) repeatedly, until there are no changes
8. Save As >> Plain SVG

When you run the SCR file, 1px in the SVG will be equivalent to 1 of whatever
unit your EAGLE display is set to (mic, mm, mil, inch) 
