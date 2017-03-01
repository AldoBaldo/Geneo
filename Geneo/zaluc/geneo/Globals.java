//+-- File Globals.java ------------------------------------------------------+
//|                                                                           |
//| Description:  This file contains the Globals class which is responsible   |
//|               for housing global information                              |
//|                                                                           |
//| Classes:      class Globals                                               |
//|                                                                           |
//|---------------------------------------------------------------------------+

package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;


//+-- Class Globals ----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class Globals                                               |
//|                                                                           |
//| Description:  Globals houses global information                           |
//|                                                                           |
//| Methods:      public Globals (Applet applet)                              |
//|                                                                           |
//|---------------------------------------------------------------------------+

class Globals
{
  //+-----------------------+
  //| Parameter Information |
  //+-----------------------+
  public static final String[][] paramInfo =
  {
    // An array of strings describing each parameter
    // Format: parameter name, parameter type, parameter description
    {"Source",          "filename",                "(required) The name of the gedcom file to display"},
    {"Width",           "number",                  "(required) The width of the window to create"},
    {"Height",          "number",                  "(required) The height of the window to create"},
    {"Foreground",      "hexadecimal color value", "(optional) The color to display as the foreground"},
    {"Background",      "hexadecimal color value", "(optional) The color to display as the background"},
    {"PeopleBoxBkg",    "hexadecimal color value", "(optional) The color to display in the background of a people box"},
    {"PBoxBorderWidth", "number",                  "(optional) The width of the people box border"},
    {"BkgImage",        "filename",                "(optional) The image to display as the background"},
    {"BkgImgLayout",    "number",                  "(optional) How to layout background image (0 => center, 1 => tile), default is center"},
    {"ClearBkg",        "boolean (0,1)",           "(optional) Force clearing the background (for transparent GIFs as BkgImage)"},
    {"BorderWidth",     "number",                  "(optional) The width of the border"},
    {"EmbedInPage",     "boolean (true,false)",    "(optional) Embed Geneo in web page, default = false"},
    {"Primary",         "number",                  "(optional) Specify individual to start with as primary, default taken from data file"},
    {"DetailLoc",       "Partial URL",             "(optional) Specify the base URL for an individuals detailed information"},
    {"HtmlTarget",      "String",                  "(optional) Specify the frame target for the html pages such as 'help' or 'details'"},
    {"InitialZoom",     "number",                  "(optional) Specify the initial zoom level at which the tree will be displayed (default = 9)"},
    {"HelpUrl",         "URL",                     "(optional) Specify the URL of the help page"},
    {"DumpStats",       "boolean (0,1)",           "(optional) Dump memory and tree statistics to the java console"},
  };
  public static final String paramHeadings[] =
     {"Parameter:", "Type:", "Description:"};

  public static final int imgCenter = 0;
  public static final int imgTile   = 1;

  public              Frame         appletFrameParent;
  public              AppletContext context;
  public              URL           documentBase;
  public              URL           codeBase;
  public              String        sourceFile;
  public              Color         foregroundColor;
  public              Color         backgroundColor;
  public              Color         peopleBoxBkg;
  public              int           peopleBoxBorderWidth;
  public              Image         backgroundImage;
  public              int           bkgImageLayout;
  public              boolean       clearBackground;
  public              int           primary;
  public              String        detailLoc;
  public              String        htmlTarget;
  public              int           initialZoom;
  public              URL           helpUrl;
  public              boolean       dumpStats;
  public              boolean       doAbsoluteScrolling = true;
  public static final boolean       groupSpouses = false;

  //+-------------------+
  //| Error Information |
  //+-------------------+
  public static final int statusOK       = 0;
  public static final int statusError    = 1;
  public static final int statusBadParam = 2;

  public int    statusCode;
  public String statusDesc;

  public Globals (Applet applet,
                  String paramSourceFile,
                  String paramForeground,
                  String paramBackground,
                  String paramPeopleBoxBkg,
                  String paramPeopleBoxBorderWidth,
                  String paramImageFile,
                  String paramBkgImgLayout,
                  String paramClearBkg,
                  String paramPrimary,
                  String paramDetailLoc,
                  String paramHtmlTarget,
                  String paramInitialZoom,
                  String paramHelpUrl,
                  String paramDumpStats)
  {
    // Initalize status
    statusCode = statusOK;
    statusDesc = "";

    // Get our parameters

    appletFrameParent         = getFrameParent(applet);
    context                   = applet.getAppletContext();
    documentBase              = applet.getDocumentBase();
    codeBase                  = applet.getCodeBase();

//    OutputProperties();

    sourceFile = paramSourceFile;
    if (sourceFile == null)
    {
      statusCode = statusBadParam;
      statusDesc = "Missing source file";
    }

    try
    {
      // Debug parameters
//      System.out.println ("System Settings are:");
//      System.out.println ("   documentBase    = " + documentBase.toString()  );
//      System.out.println ("   codeBase        = " + codeBase.toString()      );
//      System.out.println ("Parameters are:");
//      System.out.println ("   Source          = " + sourceFile               );
//      System.out.println ("   Foreground      = " + paramForeground          );
//      System.out.println ("   Background      = " + paramBackground          );
//      System.out.println ("   PeopleBoxBkg    = " + paramPeopleBoxBkg        );
//      System.out.println ("   PBoxBorderWidth = " + paramPeopleBoxBorderWidth);
//      System.out.println ("   BkgImage        = " + paramImageFile           );
//      System.out.println ("   BkgImgLayout    = " + paramBkgImgLayout        );
//      System.out.println ("   ClearBkg        = " + paramClearBkg            );
//      System.out.println ("   Primary         = " + paramPrimary             );
//      System.out.println ("   DetailLoc       = " + paramDetailLoc           );
//      System.out.println ("   HtmlTarget      = " + paramHtmlTarget        );
//      System.out.println ("   InitialZoom     = " + paramInitialZoom         );
//      System.out.println ("   HelpUrl         = " + paramHelpUrl             );
//      System.out.println ("   DumpStats       = " + paramDumpStats           );

      if (paramForeground != null)
        foregroundColor = new Color(Integer.parseInt(paramForeground,16));
      else
        foregroundColor = Color.black;

      if (paramBackground != null)
        backgroundColor = new Color(Integer.parseInt(paramBackground,16));
      else
        backgroundColor = Color.lightGray;

      if (paramPeopleBoxBkg != null)
        peopleBoxBkg = new Color(Integer.parseInt(paramPeopleBoxBkg,16));
      else
        peopleBoxBkg = null;

      if (paramPeopleBoxBorderWidth != null)
        peopleBoxBorderWidth = Integer.parseInt(paramPeopleBoxBorderWidth);
      else
      {
        if ((paramPeopleBoxBkg == null) &&  // For transparent boxes that let an image show
            (paramImageFile    != null))    // through, use a simple line for the border
          peopleBoxBorderWidth = 1;
        else
          peopleBoxBorderWidth = 2;
      }

      if (paramImageFile != null)
      {
        backgroundImage = applet.getImage(documentBase, paramImageFile);
        if (backgroundImage == null)
        {
          System.out.println("Could not find background image: " + paramImageFile);
        }
      }

      if (paramBkgImgLayout != null)
      {
        switch (Integer.parseInt(paramBkgImgLayout))
        {
          case imgTile:
            bkgImageLayout = imgTile;
            break;
          default:
            bkgImageLayout = imgCenter;
        }
      }
      else
        bkgImageLayout = imgCenter;

      if ((paramClearBkg != null) &&
          (Integer.parseInt(paramClearBkg) == 1))
        clearBackground = true;
      else
        clearBackground = false;

      if (paramPrimary != null)
      {
        primary = Integer.parseInt(paramPrimary);
      }
      else
      {
        primary = -1;
      }

      if ((paramDetailLoc != null) &&
          (paramDetailLoc.length() == 0))
      {
        paramDetailLoc = null;
      }
      detailLoc  = paramDetailLoc;

      if ((paramHtmlTarget != null) &&
          (paramHtmlTarget.length() == 0))
      {
        paramHtmlTarget = null;
      }
      htmlTarget = paramHtmlTarget;

      if (paramInitialZoom != null)
      {
        initialZoom = Integer.parseInt(paramInitialZoom);
      }
      else
      {
        initialZoom = 9;
      }

      try
      {
        helpUrl = null;
        if (paramHelpUrl != null)
        {
          helpUrl = new URL (paramHelpUrl);
        }
      }
      catch (MalformedURLException e)
      {
        // Do nothing and catch it in the finally block
      }
      finally
      {
        if (helpUrl == null)
        {
          try
          {
            helpUrl = new URL (codeBase, "zaluc/geneo/help.html");
          }
          catch (MalformedURLException e)
          {
            // We're hosed, just do nothing
          }
        }
      }

      if ((paramDumpStats != null) &&
          (Integer.parseInt(paramDumpStats) == 1))
        dumpStats = true;
      else
        dumpStats = false;

      // Don't do absolute scrolling for Internet Explorer because
      // we receive an absolute scroll right after a page or increment
      // that puts us at the wrong spot.
//    (new InfoDlg(appletFrameParent, context, null,
//                 "java.vendor Property",
//                 "<" + System.getProperty("java.vendor") + ">",
//                 false)).show();
//    (new InfoDlg(appletFrameParent, context, null,
//                 "java.veersion Property",
//                 "<" + System.getProperty("java.version") + ">",
//                 false)).show();
//      if (System.getProperty("java.vendor").
//                equalsIgnoreCase("Microsoft Corp."))
//      {
//        (new InfoDlg(appletFrameParent, context, null,
//                     "globals",
//                     "doAbsoluteScrolling = false",
//                     false)).show();
//        doAbsoluteScrolling = false;
//      }
//      else
//      {
//        (new InfoDlg(appletFrameParent, context, null,
//                     "globals",
//                     "doAbsoluteScrolling = true",
//                     false)).show();
//        doAbsoluteScrolling = true;
//      }
    }
    catch (NumberFormatException e)
    {
      statusCode = statusBadParam;
      statusDesc = "NumberFormatException: " + e.getMessage();
    }
  }

  public void updateContext (AppletContext newContext)
  {
    context = newContext;
  }

  private Frame getFrameParent(Applet applet)
  {
    Container parent;

    for (parent=applet.getParent();
         (parent != null) && !(parent instanceof Frame);
         parent = parent.getParent())
      ;
//      System.out.println("getFrameParent: " + parent.toString());

//    System.out.println("getFrameParent: returning: " + ((parent != null) ? parent.toString() : "null"));
    return (Frame) parent;
  }

//  private void OutputProperties ()
//  {
//    Properties  props = System.getProperties();
//    Enumeration enum  = props.propertyNames();
//    String      propName;
//    String      strProps = "System Properties:";
//
//    while (enum.hasMoreElements())
//    {
//      propName = (String)enum.nextElement();
//      if (!propName.equalsIgnoreCase("java.class.path"))
//      {
//        strProps += "\n   " + propName + " = " + props.getProperty(propName);
//      }
//    }
//    (new InfoDlg(appletFrameParent, context, null,
//                 "System Properties",
//                 strProps,
//                 false)).show();
//  }
}
