//+-- File Geneo.java --------------------------------------------------------+
//|                                                                           |
//| Description:  This is the main file for geneo applet.  It contains the    |
//|               Geneo class that extends Applet and most of the user        |
//|               interface components like the TreeCanvas and TreeControls.  |
//|                                                                           |
//| Classes:      class Geneo extends Applet                                  |
//|                                                                           |
//|               class MainPanel extends Panel implements Runnable           |
//|                                                                           |
//|               interface ImageUser                                         |
//|                                                                           |
//|               class ImageLoader implements Runnable                       |
//|                                                                           |
//|               class TreeCanvas extends Panel implements ImageUser         |
//|                                                                           |
//|---------------------------------------------------------------------------+

package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.lang.*;
import java.io.*;


//+-- Class Geneo ------------------------------------------------------------+
//|                                                                           |
//| Syntax:       public class Geneo extends Applet implements Runnable       |
//|                                                                           |
//| Description:  Geneo is the main class for this applet.  It paints the     |
//|               border around the window and creates a Panel control to     |
//|               paint the interior of the window.  It also starts the       |
//|               parser.                                                     |
//|                                                                           |
//| Methods:      public void       init             ()                       |
//|                                                                           |
//|               public void       run              ()                       |
//|                                                                           |
//|               public void       update           (Graphics g)             |
//|                                                                           |
//|               public void       paint            (Graphics g)             |
//|                                                                           |
//|               public String     getAppletInfo    ()                       |
//|                                                                           |
//|               public String[][] getParameterInfo ()                       |
//|                                                                           |
//|---------------------------------------------------------------------------+

public class Geneo extends Applet
{
  static int instanceCount = 0;
  static MainFrame frame = null;  // Make this static so that it can be seen if reloaded.

  Globals   globals;
  Rectangle innerRect;
  MainPanel panel = null;
  boolean   embedInPage = false;
  int       borderWidth = 0;

  public void init ()
  {
    String  paramSourceFile;
    String  paramWidth;
    String  paramHeight;
    String  paramForeground;
    String  paramBackground;
    String  paramPeopleBoxBkg;
    String  paramPeopleBoxBorderWidth;
    String  paramImageFile;
    String  paramBkgImgLayout;
    String  paramClearBkg;
    String  paramBorderWidth;
    String  paramEmbedInPage;
    String  paramPrimary;
    String  paramDetailLoc;
    String  paramHtmlTarget;
    String  paramInitialZoom;
    String  paramHelpUrl;
    String  paramDumpStats;

//zombie    if (frame == null)
//zombie    {
      paramSourceFile           = getParameter("Source");
      paramWidth                = getParameter("Width");
      paramHeight               = getParameter("Height");
      paramForeground           = getParameter("Foreground");
      paramBackground           = getParameter("Background");
      paramPeopleBoxBkg         = getParameter("PeopleBoxBkg");
      paramPeopleBoxBorderWidth = getParameter("PBoxBorderWidth");
      paramImageFile            = getParameter("BkgImage");
      paramBkgImgLayout         = getParameter("BkgImgLayout");
      paramClearBkg             = getParameter("ClearBkg");
      paramBorderWidth          = getParameter("BorderWidth");
      paramEmbedInPage          = getParameter("EmbedInPage");
      paramPrimary              = getParameter("Primary");
      paramDetailLoc            = getParameter("DetailLoc");
      paramHtmlTarget           = getParameter("HtmlTarget");
      paramInitialZoom          = getParameter("InitialZoom");
      paramHelpUrl              = getParameter("HelpUrl");
      paramDumpStats            = getParameter("DumpStats");

      if (paramSourceFile != null)
      {
        if (paramEmbedInPage != null)
        {
          if (paramEmbedInPage.equalsIgnoreCase("true") ||
              paramEmbedInPage.equalsIgnoreCase("yes")  ||
              paramEmbedInPage.equals("1"))
          {
            embedInPage = true;
          }
        }
        if (embedInPage)
        {
          if (paramBorderWidth != null)
          {
            borderWidth = Integer.parseInt(paramBorderWidth);
          }
          else
          {
            borderWidth = 3;
          }
        }
        internalBegin (paramSourceFile,
                       Integer.parseInt(paramWidth),
                       Integer.parseInt(paramHeight),
                       paramForeground,
                       paramBackground,
                       paramPeopleBoxBkg,
                       paramPeopleBoxBorderWidth,
                       paramImageFile,
                       paramBkgImgLayout,
                       paramClearBkg,
                       paramPrimary,
                       paramDetailLoc,
                       paramHtmlTarget,
                       paramInitialZoom,
                       paramHelpUrl,
                       paramDumpStats,
                       embedInPage);
      } // if paramSourceFile != null
//zombie    } // if frame != null
//zombie    else
//zombie    {
//zombie      frame.updateContext (getAppletContext());
//zombie    }
  }

  public void begin (String  source,
                     int     width,
                     int     height,
                     String  foreground,
                     String  background,
                     String  peopleBoxBkg,
                     String  peopleBoxBorderWidth,
                     String  bkgImage,
                     String  bkgImgLayout,
                     String  clearBackground,
                     String  primary,
                     String  detailLoc,
                     String  htmlTarget,
                     String  initialZoom,
                     String  helpUrl)
//                     String  dumpStats)
  {
//zombie    if (instanceCount == 0)
//zombie    {
      internalBegin (source,
                     width,
                     height,
                     foreground,
                     background,
                     peopleBoxBkg,
                     peopleBoxBorderWidth,
                     bkgImage,
                     bkgImgLayout,
                     clearBackground,
                     primary,
                     detailLoc,
                     htmlTarget,
                     initialZoom,
                     helpUrl,
                     "0",  //dumpStats,
                     false);
//zombie    }
//zombie    else if (primary != null)
//zombie    {
//zombie      setPrimary (Integer.parseInt(primary));
//zombie      showWindow ();
//zombie    }
  }

  public int getInstanceCount()
  {
    return instanceCount;
  }

  public boolean isLoaded()
  {
    return instanceCount > 0;
  }

  public void closeInstance()
  {
    instanceCount--;
  }

  /**
  ** Load the FindPerson dialog box
  */  
  public void find()
  {
    if (panel != null)
    {
      panel.find ();
    }
    else if (frame != null)
    {
      frame.find ();
    }
  }

  /**
  ** Zoom In
  */
  public void zoomIn()
  {
    if (panel != null)
    {
      panel.zoomIn ();
    }
    else if (frame != null)
    {
      frame.zoomIn ();
    }
  }

  /**
  ** Zoom Out
  */
  public void zoomOut()
  {
    if (panel != null)
    {
      panel.zoomOut ();
    }
    else if (frame != null)
    {
      frame.zoomOut ();
    }
  }

  /**
  ** Re-center the tree around the primary person
  */
  public void home()
  {
    if (panel != null)
    {
      panel.home ();
    }
    else if (frame != null)
    {
      frame.home ();
    }
  }

  /**
  ** Set the specified individual as the primary person
  **
  ** @param newPrimary  the GEDCOM index of the person to set as primary
  */
  public void setPrimary (int newPrimary)
  {
    if (panel != null)
    {
      panel.setPrimary (newPrimary);
    }
    else if (frame != null)
    {
      frame.setPrimary (newPrimary);
    }
  }

  /**
  ** Set the selected individual as the primary person
  */
  public void setPrimary ()
  {
    if (panel != null)
    {
      panel.setPrimary ();
    }
    else if (frame != null)
    {
      frame.setPrimary ();
    }
  }

  /**
  ** Go back to previous primary individual
  */
  public void back()
  {
    if (panel != null)
    {
      panel.back ();
    }
    else if (frame != null)
    {
      frame.back ();
    }
  }

  /**
  ** Move forward to next primary individual
  */
  public void forward()
  {
    if (panel != null)
    {
      panel.forward ();
    }
    else if (frame != null)
    {
      frame.forward ();
    }
  }

  public void showWindow ()
  {
    if (frame != null)
      frame.setVisible(true);
  }

  private void internalBegin (String  source,
                              int     width,
                              int     height,
                              String  foreground,
                              String  background,
                              String  peopleBoxBkg,
                              String  peopleBoxBorderWidth,
                              String  bkgImage,
                              String  bkgImgLayout,
                              String  clearBackground,
                              String  primary,
                              String  detailLoc,
                              String  htmlTarget,
                              String  initialZoom,
                              String  helpUrl,
                              String  dumpStats,
                              boolean embedInPage)
  {
    try
    {
      globals = new Globals(this,
                            source,
                            foreground,
                            background,
                            peopleBoxBkg,
                            peopleBoxBorderWidth,
                            bkgImage,
                            bkgImgLayout,
                            clearBackground,
                            primary,
                            detailLoc,
                            htmlTarget,
                            initialZoom,
                            helpUrl,
                            dumpStats);

      if (embedInPage)
      {
        setForeground(globals.foregroundColor);
        setBackground(globals.backgroundColor);

        // Set up the panel position
        innerRect = this.getBounds();
        innerRect.x += borderWidth;
        innerRect.y += borderWidth;
        innerRect.width  -= 2*borderWidth;
        innerRect.height -= 2*borderWidth;
        panel = new MainPanel(this, globals);
        this.setLayout(null);
        this.add(panel);
        panel.setBounds(innerRect.x,
                        innerRect.y,
                        innerRect.width,
                        innerRect.height);

        if (globals.statusCode != Globals.statusOK)
          panel.notifyDone ();
      }
      else
      {
        // Create the main frame
        frame = new MainFrame(this, globals, width, height);
        globals.appletFrameParent = frame;
//        frame.requestFocus();

        if (globals.statusCode != Globals.statusOK)
          frame.notifyDone ();

        instanceCount++;
      }
    }
    catch (NullPointerException e)
    {
      globals.statusCode = Globals.statusError;
      globals.statusDesc = "geneo: NullPointerException: " + e.getMessage();
      if (panel != null)
        panel.notifyDone ();
      else if (frame != null)
        frame.notifyDone ();
    }
    catch (NumberFormatException e)
    {
      globals.statusCode = Globals.statusError;
      globals.statusDesc = "geneo: NullFormatException: " + e.getMessage();
      if (panel != null)
        panel.notifyDone ();
      else if (frame != null)
        frame.notifyDone ();
    }
  }

  // Geneo::update

  public void update (Graphics g)
  {
    if (embedInPage)
    {
      Rectangle r = this.getBounds();

      // Draw border
      for (int i = 0; i < borderWidth; i++)
      {
        g.drawLine (    r.x + i    ,      r.y + i    ,     r.x + i    , r.height - i - 1);
        g.drawLine (    r.x + i    , r.height - i - 1, r.width - i - 1, r.height - i - 1);
        g.drawLine (r.width - i - 1, r.height - i - 1, r.width - i - 1,      r.y + i    );
        g.drawLine (r.width - i - 1,      r.y + i    ,     r.x + i    ,      r.y + i    );
      }
    }
  }

  public void paint (Graphics g)
  {
    update(g);
  }

  public void PrintAll (Graphics g)
  {
    System.out.println("We should be printing now");
  }

  public synchronized void setBounds(int x,
                                     int y,
                                     int width,
                                     int height)
  {
    super.setBounds(x, y, width, height);

    if (embedInPage && (panel != null))
    {
      panel.setBounds(x      +   borderWidth,
                      y      +   borderWidth,
                      width  - 2*borderWidth,
                      height - 2*borderWidth);
    }
  }

  public String getAppletInfo()
  {
//    return "Geneo Version 1.0\nWritten by Don Baldwin";
    return "Geneo Version 2.0\nWritten by Don Baldwin\nNov. 1997";
  }

  public String[][] getParameterInfo()
  {
    return globals.paramInfo;
  }
}
