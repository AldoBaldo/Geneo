package zaluc.geneo;

import java.awt.*;
import java.lang.*;
import java.util.*;

//+-- Class Box3D ------------------------------------------------------------+
//|                                                                           |      
//| Syntax:       class Box3D                                                 |
//|                                                                           |
//| Description:  Draws a 3-D box at the specified location.                  |
//|                                                                           |
//| Methods:      public      Box3D   (Globals inGlobals,                     |
//|                                    int x,                                 |
//|                                    int y,                                 |
//|                                    int height,                            |
//|                                    int width)                             |
//|                                                                           |
//|               public void draw    (Graphics g)                            |
//|                                                                           |
//|               public void fill    (Graphics g,                            |
//|                                    int      current,                      |
//|                                    int      total)                        |
//|                                                                           |
//|---------------------------------------------------------------------------+

class Box3D
{
  private Globals   globals;
  private Rectangle rect;
  private Rectangle fillRect;

  public Box3D (Globals inGlobals,
                int     x,
                int     y,
                int     width,
                int     height)
  {
    globals     = inGlobals;
    rect        = new Rectangle();
    rect.x      = x;
    rect.y      = y;
    rect.width  = width;
    rect.height = height;
    fillRect        = new Rectangle();
    fillRect.x      = rect.x      +   globals.peopleBoxBorderWidth;
    fillRect.y      = rect.y      +   globals.peopleBoxBorderWidth;
    fillRect.width  = rect.width  - 2*globals.peopleBoxBorderWidth + 1;
    fillRect.height = rect.height - 2*globals.peopleBoxBorderWidth + 1;
  }

  public void fill (Graphics g,
                    int      current,
                    int      total)
  {
    int barWidth = (fillRect.width * current) / total;

    g.fillRect(fillRect.x,
               fillRect.y,
               barWidth,
               fillRect.height);
  }

  public void draw (Graphics g)
  {
    if (globals.peopleBoxBkg != null)
    {
      // Fill the inside of the rectangle
      g.setColor (globals.peopleBoxBkg);
      g.fillRect (rect.x, rect.y, rect.width, rect.height);
    }

    if (globals.peopleBoxBorderWidth > 1)
    {
      int i;
      int topY    = rect.y;
      int leftX   = rect.x;
      int bottomY = rect.y + rect.height;
      int rightX  = rect.x + rect.width;
      int red;
      int green;
      int blue;

      // Figure out my border colors

      if ((globals.peopleBoxBkg    != null) &&
          (globals.backgroundImage != null))
      {
        red   = globals.peopleBoxBkg.getRed();
        green = globals.peopleBoxBkg.getGreen();
        blue  = globals.peopleBoxBkg.getBlue();
      }
      else
      {
        red   = globals.backgroundColor.getRed();
        green = globals.backgroundColor.getGreen();
        blue  = globals.backgroundColor.getBlue();
      }

      int redShift   = (255-red)   / 5;
      int greenShift = (255-green) / 5;
      int blueShift  = (255-blue)  / 5;

      Color lightOuter = new Color (red + 3*redShift, green + 3*greenShift, blue + 3*blueShift);

      redShift   = red   / 5;
      greenShift = green / 5;
      blueShift  = blue  / 5;

      Color darkInner  = new Color (3*redShift, 3*greenShift, 3*blueShift);
      Color darkOuter  = new Color (4*redShift, 4*greenShift, 4*blueShift);

      // Draw the right and bottom sides lightened

      g.setColor(lightOuter);
      for (i = 0; i < (globals.peopleBoxBorderWidth - 1); i++)
      {
        g.drawLine (rightX - i, bottomY - i, rightX - i, topY    + i);
        g.drawLine (rightX - i, bottomY - i, leftX  + i, bottomY - i);
      }

      g.setColor(darkOuter);
      g.drawLine (rightX - i, bottomY - i, rightX - i, topY    + i);
      g.drawLine (rightX - i, bottomY - i, leftX  + i, bottomY - i);

      // Draw the left and top sides darkened

      g.setColor(darkOuter);
      for (i = 0; i < (globals.peopleBoxBorderWidth - 1); i++)
      {
        g.drawLine (leftX + i, topY + i, rightX - i, topY    + i);
        g.drawLine (leftX + i, topY + i, leftX  + i, bottomY - i);
      }

      g.setColor(darkInner);
      g.drawLine (leftX + i, topY + i, rightX - i, topY    + i);
      g.drawLine (leftX + i, topY + i, leftX  + i, bottomY - i);

      g.setColor (globals.foregroundColor);
    }
    else
	  {
      g.setColor (globals.foregroundColor);
      g.drawRect (rect.x, rect.y, rect.width, rect.height);
	  }

  } // draw

} // Box3D
