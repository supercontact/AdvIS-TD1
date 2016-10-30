PhotoMagician is a photo browser that let you easily organize and annotate photos.
Select PhotoApplication.java to build the photo browser application. (TestNodes.java is used to test scene-graph structure.) 

Here is the complete feature lists:

Load multiple photos.
Files showed in the fileChooser are filtered by extensions, limit only to image files.
Add a photo frame to the photo.
Show a background image that stays fixed when scrolling.

In browse mode:
Show a table of photo thumbnails.
Resizing will rearrange icons.
Drag to select/deselect all touched photos.
Double click on a photo icon to enter photo viewer mode showing that photo.
Hover / Pressed / Selected states of photo icons show different appearances.
Select a group of photos and delete them all at once (with a warning popup).
Click +Tag button to enter tag management menu, where you can create / rename / remove tags.
When Images are selected, enter tag management menu to add / remove tags to these photos by using the combo boxes.
Click on the tags on the tool bar to only show photos containing selected tags.
In this mode, some menu items are disabled.

In photo viewer mode:
Navigate by left/right keys or the two buttons (which appears when moving the cursor to the bottom of the screen).
Enable scrolling if image is too big.
Drag (left/right mouse key in view mode, right mouse key in flipped mode) to scroll.
Mouse wheel to navigate in album.
Hold ctrl and use mouse wheel to scale photo. The position where the mouse is pointing will stay fixed.
Resizing will be handled correctly.
4 Scale modes become available in menu (plus one option to scale up). Will persist when resizing unless mouse wheel scaling is used.
Double click to flip photo (+animation).

In flipped state:
A control bar is at the bottom, which will appear when moving the mouse to the bottom.
Drag to draw strokes.
Drag to draw a straight line (if straight line tool is selected).
Drag to draw a rectangle (if rectangle tool is selected).
Drag to draw an ellipse (if ellipse tool is selected).
The control bar becomes more transparent when drawing.
Click one time, and type to add annotation.
Automatic line break when reaching the right end.
Backspace to delete text.
Enter to jump line.
When editing annotation, use left and right key to navigate the insertion point. Text can be inserted in the middle.
Color / stroke width / text size / font options in the control bar. Influence individual newly created element.
Color selection button has a preview of the selected color.
Clean option in the menu to remove all annotations / strokes (with a warning popup).
Ctrl + z to undo (remove last created element).

***** All the paintings of PhotoComponent and PhotoIcon are done using scene-graph nodes.

Save the path in fileChooser where images were previously loaded.
Save the album (all imported images and annotations / strokes) automatically when importing, and every 5 seconds if changes are made.
Automatically load the saved album when starting the application.
If external photo is moved or deleted, show a replacement image along with the original path to the missing photo.