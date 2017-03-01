//+-- File MultiLineLable.java -----------------------------------------------+
//|                                                                           |
//| Description:  This file contains the code necessary to implement a        |
//|               MultiLineLabel control for dialog boxes.  This control      |
//|               takes a string label that can contain '\n' characters and   |
//|               'mailto',  'http' and 'ftp' URLs.  The '\n' causes a line   |
//|               break, the URLs are displayed in blue and are sent to the   |
//|               browser when clicked on.                                    |
//|                                                                           |
//|               Currently, there is a limit of 10 lines and 2 URLs per line.|
//|               Also, the URLs do not change color after being clicked.     |
//|                                                                           |
//| Classes:      class MultiLineLabel extends Canvas                         |
//|               class UrlInfo                                               |
//|               class SubstringInfo                                         |
//|               class LineInfo                                              |
//|                                                                           |
//|---------------------------------------------------------------------------+

package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;

//+-- Class MultiLineLabel ---------------------------------------------------+
//|                                                                           |
//| Syntax:       class MultiLineLabel extends Canvas                         |
//|                                                                           |
//| Description:  MultiLineLabel defines a Label-like component for use in    |
//|               dialog boxes.  It acts like a Label component except that   |
//|               it interprets the new-line charachter to display the        |
//|               remaining text on a new line.                               |
//|                                                                           |
//|               At some future point, the MultiLineLabel may also           |
//|               recognize the mailto, http and ftp URLs and direct the      |
//|               container application to go to that URL.                    |
//|                                                                           |
//|               The code for this class came originally from the book:      |
//|                                                                           |
//|                  "Java in a Nutshell"                                     |
//|                                                                           |
//|               by David Flanagan and published by O'Reilly & Associates.   |
//|                                                                           |
//| Methods:      public    MultiLineLabel       (String   label,             |
//|                                               int      inMarginWidth,     |
//|                                               int      inMarginHeight,    |
//|                                               int      inAlignment)       |
//|                                                                           |
//|               public    MultiLineLabel       (String   label,             |
//|                                               int      inMarginWidth,     |
//|                                               int      inMarginHeight)    |
//|                                                                           |
//|               public    MultiLineLabel       (String   label,             |
//|                                               int      inAlignment)       |
//|                                                                           |
//|               public    MultiLineLabel       (String   label)             |
//|                                                                           |
//|               protected void   newLabel      (String   label)             |
//|                                                                           |
//|               protected void   measure       ()                           |
//|                                                                           |
//|               public    void   setLabel      (String   label)             |
//|                                                                           |
//|               public    void   setFont       (Font     f)                 |
//|                                                                           |
//|               public    void   setForeground (Color    c)                 |
//|                                                                           |
//|               public    void   addNotify     ()                           |
//|                                                                           |
//|               public Dimension preferredSize ()                           |
//|                                                                           |
//|               public Dimension minimumSize   ()                           |
//|                                                                           |
//|               public    void   paint         (Graphics g)                 |
//|                                                                           |
//|               public   boolean mouseDown     (Event    e,                 |
//|                                               int      x,                 |
//|                                               int      y)                 |
//|                                                                           |
//|---------------------------------------------------------------------------+

public class MultiLineLabel extends    Canvas
                            implements MouseListener
{
  // Alignment constants

  public static final int LEFT   = 0;
  public static final int CENTER = 1;
  public static final int RIGHT  = 2;

  protected LineInfo      lines = null;         // The lines of text to display
  protected int           numLines;             // The number of lines to display
  protected UrlInfo       urls  = null;         // The URLs contained in the text
  protected int           numUrls;              // The number of URLs in the text
  protected AppletContext context;              // The context to which to send a URL
  protected String        htmlTarget;           // Target frame to send a URL to
  protected int           marginWidth;          // Left and right margins
  protected int           marginHeight;         // Top and bottom margins
  protected int           lineHeight;           // Total height of the font
  protected int           lineAscent;           // Font height above baseline
  protected int           maxWidth;             // The width of the widest line
  protected int           alignment = LEFT;     // The alignment of the text
  protected int           fixedWidth = -1;      // Fixed width set by client.  -1 means not set.

  // Here are four versions of the constructor.
  // Break the label up into separate lines, and save the other info.

  public MultiLineLabel(String        label,
                        AppletContext inContext,
                        String        inHtmlTarget,
                        int           inMarginWidth,
                        int           inMarginHeight,
                        int           inAlignment)
  {
    addMouseListener (this);
    newLabel(label);
    context      = inContext;
    htmlTarget   = inHtmlTarget;
    marginWidth  = inMarginWidth;
    marginHeight = inMarginHeight;
    alignment    = inAlignment;
  }

  public MultiLineLabel(String        label,
                        AppletContext inContext,
                        String        inHtmlTarget,
                        int           inMarginWidth,
                        int           inMarginHeight)
  {
    this(label, inContext, inHtmlTarget, inMarginWidth, inMarginHeight, LEFT);
  }

  public MultiLineLabel(String        label,
                        AppletContext inContext,
                        String        inHtmlTarget,
                        int           inAlignment)
  {
    this(label, inContext, inHtmlTarget, 10, 10, inAlignment);
  }

  public MultiLineLabel(String        label,
                        AppletContext inContext,
                        String        inHtmlTarget)
  {
    this(label, inContext, inHtmlTarget, 10, 10, LEFT);
  }

  public MultiLineLabel(String label)
  {
    this(label, null, null, 10, 10, LEFT);
  }

  // This method breaks a specified label up into an array of lines.

  protected void newLabel(String label)
  {
    StringTokenizer t;    // String tokenizer
    String          curToken;
    LineInfo        l;    // current line info
    SubstringInfo   ss;   // current string info
    UrlInfo         u;    // current URL info
    boolean         startNewSubstring;
    Color           normalColor;

    if (label != null)
    {
      normalColor = getForeground();
      if (normalColor == null)
        normalColor = Color.black;

      t  = new StringTokenizer(label, "\n ", true);
      if (t.hasMoreTokens())
      {
        numLines = 1;
        lines = l = new LineInfo();
      }
      else
      {
        numLines = 0;
        lines = l  = null;
      }
      urls = u = null;
      numUrls = 0;
      ss = null;
      startNewSubstring = true;

      try
      {
        while (t.hasMoreTokens())
        {
          curToken = t.nextToken();

          if (curToken.startsWith("\n"))
          {
            l.next = new LineInfo();
            l = l.next;
            numLines++;
            startNewSubstring = true;
            ss = null;
          }
          else
          {
            try
            {
              // This will cause an exception if it's not a real URL
              URL url = new URL (curToken);

              if (url != null)
              {
                // If that succeeded, it must be a URL
                if (u == null)      // First time?
                  urls = u = new UrlInfo(url);
                else
                {
                  u.next = new UrlInfo(url);
                  u = u.next;
                }
                numUrls++;
                if (ss == null)   // First time for this line?
                {
                  l.substrings = ss = new SubstringInfo(Color.blue, curToken);
                }
                else
                {
                  ss.next = new SubstringInfo(Color.blue, curToken);
                  ss = ss.next;
                }
                ss.url = u;
                startNewSubstring = true;
                l.numSubstrings++;
              }
            }
            catch (MalformedURLException e)
            {
              if (startNewSubstring)
              {
                if (ss == null)   // First time for this line?
                  l.substrings = ss = new SubstringInfo(normalColor, curToken);
                else
                {
                  ss.next = new SubstringInfo(normalColor, curToken);
                  ss = ss.next;
                }
                l.numSubstrings++;
              }
              else
                ss.text += curToken;
              startNewSubstring = false;
            }
          }
        }
      }
      catch (NoSuchElementException e)
      {
      }
    }
  }

  // This method figures out how large the font is, and how wide each
  // line of the label is, and how wide the widest line is.

  protected void measure()
  {
    FontMetrics fm = getFontMetrics(getFont());

    if (fm == null)
      return;

    lineHeight = fm.getHeight();
    lineAscent = fm.getAscent();
    maxWidth   = 0;
    for (LineInfo l = lines; l != null; l = l.next)
    {
      for (SubstringInfo ss = l.substrings; ss != null; ss = ss.next)
      {
        ss.width = fm.stringWidth(ss.text);
        l.width += ss.width;
      }
      if (l.width > maxWidth)
        maxWidth = l.width;
    }
  }

  // Methods to set the various attributes of the component

  public void setLabel(String label)
  {
    newLabel(label);
    measure();
    repaint();
  }

  public void setFont(Font f)
  {
    super.setFont(f);
    measure();
    repaint();
  }

  public void setForeground(Color c)
  {
    super.setForeground(c);
    repaint();
  }

  public void setEnvironment(AppletContext inContext,
                             String        inHtmlTarget,
                             int           inMarginWidth,
                             int           inMarginHeight,
                             int           inAlignment)
  {
    context      = inContext;
    htmlTarget   = inHtmlTarget;
    marginWidth  = inMarginWidth;
    marginHeight = inMarginHeight;
    alignment    = inAlignment;
  }

  public void setFixedWidth(int inFixedWidth)
  {
    fixedWidth = inFixedWidth;
  }

  // This method is invoked after our canvas is first created
  // but before it can actually be displayed.  After we've
  // invoked our superclass's addNotify() method, we have font
  // metrics and can successfully call measure() to figure out
  // how big the label is.

  public void addNotify()
  {
    super.addNotify();
    measure();
  }

  // This method is called by a layout manager when it wants to
  // know how big we'd like to be.

  public Dimension getPreferredSize()
  {
    return new Dimension(maxWidth + 2*marginWidth,
                         numLines * lineHeight + 2 * marginHeight);
  }

  // This method is called when the layout manager wants to know
  // the bare minimum amount of space we need to get by.

  public Dimension getMinimumSize()
  {
    return new Dimension(maxWidth, numLines * lineHeight);
  }

  // This method draws the label.
  // Note that it handles the margins and the alignment, but that
  // it doesn't have to worry about the color or font--the superclass
  // takes care of setting those in the Graphics object we're passed.

  public void paint(Graphics g)
  {
    int       x,y;
    Dimension d = this.getSize();

    y = lineAscent + (d.height - numLines * lineHeight)/2;

    for (LineInfo l = lines; l != null; l = l.next, y += lineHeight)
    {
      switch (alignment)
      {
        case LEFT:
        default:
          x = marginWidth;
          break;
        case CENTER:
          x = (d.width - l.width)/2;
          break;
        case RIGHT:
          x = d.width = marginWidth = l.width;
          break;
      }
      for (SubstringInfo ss = l.substrings; ss != null; ss = ss.next)
      {
        g.setColor  (ss.color);
        g.drawString(ss.text, x, y);
        if (ss.url != null)
        {
          ss.url.rect.x = x;
          ss.url.rect.y = y - lineHeight;
          ss.url.rect.width = ss.width;
          ss.url.rect.height = lineHeight;
        }
        x += ss.width;
      }
    }
  }


  //----------------------------------------
  // Implements MouseListener::mouseClicked
  //----------------------------------------
  public void mouseClicked (MouseEvent e)
  {
    int mods = e.getModifiers();
    int x    = e.getX();
    int y    = e.getY();

    if ((mods & InputEvent.BUTTON3_MASK) != 0)
    {
      // Do right-mouse button action
    }
    else if ((mods & InputEvent.BUTTON2_MASK) != 0)
    {
      // Do middle-mouse button action
    }
    else
    {
      // Do left-mouse button action
      // Find the URL that is under the mouse
      for (UrlInfo u = urls; u != null; u = u.next)
      {
        if (u.rect.contains(x,y))
        {
          if (context != null)
          {
            if ((htmlTarget != null) &&
                (htmlTarget.length() > 1))
              context.showDocument(u.url, htmlTarget);
            else
              context.showDocument(u.url);
          }
          break;
        }
      }
    }
  } // MouseListener::mouseClicked

  //------------------------------
  // Unused mouseListener methods
  //------------------------------
  public void mousePressed  (MouseEvent e) {;}
  public void mouseReleased (MouseEvent e) {;}
  public void mouseEntered  (MouseEvent e) {;}
  public void mouseExited   (MouseEvent e) {;}

}

//+-- Class UrlInfo ----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class UrlInfo                                               |
//|                                                                           |
//| Description:  UrlInfo contains information needed for finding an URL      |
//|               in a MultiLineLabel when a mouse click is received, for     |
//|               executing that URL.                                         |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class UrlInfo
{
  UrlInfo next;

  Rectangle rect;
  URL       url;

  UrlInfo(URL inUrl)
  {
    next = null;
    rect = new Rectangle();
    url  = inUrl;
  }
}

//+-- Class SubstringInfo ----------------------------------------------------+
//|                                                                           |
//| Syntax:       class SubstringInfo                                         |
//|                                                                           |
//| Description:  SubstringInfo contains this information needed to write     |
//|               a particular substring to the control.  The Y position      |
//|               is inherent in the line in which it is contained.           |
//|                                                                           |
//| Methods:      public SubstringInfo (Color  inColor,                       |
//|                                     String inText)                        |
//|                                                                           |
//|---------------------------------------------------------------------------+

class SubstringInfo
{
  SubstringInfo next;

  Color   color;
  UrlInfo url;
  int     x;
  int     width;
  String  text;

  SubstringInfo(Color  inColor,
                String inText)
  {
    next  = null;
    color = inColor;
    url   = null;
    x     = 0;
    width = 0;
    text  = inText;
  }
}

//+-- Class LineInfo ---------------------------------------------------------+
//|                                                                           |
//| Syntax:       class LineInfo                                              |
//|                                                                           |
//| Description:  LineInfo contains the information needed to write all of    |
//|               the substrings of a particular line.                        |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class LineInfo
{
  LineInfo next;

  int y;
  int width;

  int           numSubstrings;
  SubstringInfo substrings;

  LineInfo()
  {
    next = null;
    numSubstrings = 0;
    substrings = null;
  }
}

