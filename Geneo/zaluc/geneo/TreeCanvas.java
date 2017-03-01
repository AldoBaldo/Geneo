//+-- File TreeCanvas.java ---------------------------------------------------+
//|                                                                           |
//| Description:  This file contains the code for the TreeCanvas class that   |
//|               is responsible for displaying the genealogy tree.           |
//|                                                                           |
//| Classes:      class TreeCanvas extends Panel implements ImageUser         |
//|                                                       MyPopupMenuItemUser |
//|                                                       ParserUpdates       |
//|                                                       Runnable            |
//|                                                                           |
//|               interface ImageUser                                         |
//|                                                                           |
//|               class ImageLoader implements Runnable                       |
//|                                                                           |
//|---------------------------------------------------------------------------+

package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import zaluc.utils.*;


//+-- Class TreeCanvas -------------------------------------------------------+
//|                                                                           |
//| Syntax:       class TreeCanvas extends Panel implements ImageUser         |
//|                                                                           |
//| Description:  TreeCanvas is responsible for displaying the tree and       |
//|               controlling its shape.  It receives a setImage() call       |
//|               when the background image is ready to be displayed.         |
//|                                                                           |
//| Methods:      public          TreeCanvas        (Globals      inGlobals)  |
//|                                                                           |
//|               public  void    setImage          (Image        inImage)    |
//|                                                                           |
//|               public  void    test              ()                        |
//|                                                                           |
//|               public  void    update            (Graphics     g)          |
//|                                                                           |
//|               public  void    paint             (Graphics     g)          |
//|                                                                           |
//|               public  boolean mouseDown         (Event        e,          |
//|                                                  int          x,          |
//|                                                  int          y)          |
//|                                                                           |
//|               public  boolean handleEvent       (Event        e)          |
//|                                                                           |
//|               public  synchronized void reshape (int          x,          |
//|                                                  int          y,          |
//|                                                  int          width,      |
//|                                                  int          height)     |
//|                                                                           |
//|               private void    adjustScrollBars  ()                        |
//|                                                                           |
//|               public  void    zoomIn            ()                        |
//|                                                                           |
//|               public  void    zoomOut           ()                        |
//|                                                                           |
//|               public  void    showDetails       (Person person)           |
//|                                                                           |
//|               public  void    setCenterPerson   (Person newCenterPerson)  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class TreeCanvas extends    Panel
                 implements ImageUser,
                            ParserUpdates,
                            Callback,
                            Runnable,
                            ActionListener,
                            MouseListener,
                            KeyListener
{
  Geneo     geneo;
  Globals   globals;
  MainPanel panel = null;
  MainFrame frame = null;

  // Statistics to be output
  String  fileNameStr = "";
  int     totalRecordCount = 0;
  int     curRecordCount   = 0;

  // The following fields are used to paint the statistics while loading
  // the .GED file
  int    rowHeight = 0;
  int    firstRow;
  int    secondRow;
  int    thirdRow;
  int    statusRow;
  Box3D  loadBox;
  Box3D  loadBar;
  int    loadTextX;
  int    loadTextY;

  // PopupMenu Stuff
  Person   personUnderMenu;
  MenuItem find;
  MenuItem zoomIn;
  MenuItem zoomOut;
  //MenuItem redraw;
  MenuItem viewAll;
  MenuItem details;
  MenuItem center;
  //MenuItem back;
  //MenuItem forward;
  MenuItem help;
  //MenuItem about;

  boolean   parsing = true;

  PeopleList people;

  Image     offscreenImage;
  Graphics  offscreenGraphics;
  Color     backgroundColor;
  Color     foregroundColor;
  Image     bkgImage = null;
  Font      statusFont;     // Used for writing within the TreeCanvas class
  int       treeFontSize;   // Size of fonts passed to PeopleList

  int       canvasWidth, canvasHeight;

  // These task ID's are used in the callback function to
  // indentify which task has completed.
  private static final int TASK_VIEW_ALL            = 1;
  private static final int TASK_VERT_SCROLL_TIMEOUT = 2;
  private static final int TASK_HORZ_SCROLL_TIMEOUT = 3;
  private static final int TASK_NEW_HTML_TARGET     = 4;  //zombie

  // State Information
  private static final int STATE_LOADING          = 0;
  private static final int STATE_NORMAL           = 1;
//  private static final int STATE_PENDING_DETAILS  = 2;
  private              int state = STATE_NORMAL;

  // Setup Information (bit flags)
  // These flags are set when each stage of the setup process is completed.
  // The setup processes may be completed in any order.  When all are done,
  // we draw.
  private static final int SETUP_ADD_NOTIFY       = 1;
  private static final int SETUP_NOTIFY_SETUP     = 2;
  private static final int SETUP_SET_BOUNDS       = 4;
  private static final int SETUP_INIT_TREE        = 8;
  private static final int SETUP_NEED_INIT        = SETUP_ADD_NOTIFY   |
                                                    SETUP_NOTIFY_SETUP |
                                                    SETUP_SET_BOUNDS   ;
  private static final int SETUP_DONE             = SETUP_ADD_NOTIFY   |
                                                    SETUP_NOTIFY_SETUP |
                                                    SETUP_SET_BOUNDS   |
                                                    SETUP_INIT_TREE    ;
  private              int setupFlags = 0;  // Nothing completed yet.

  // Cursors:
  private static final Cursor loadingCursor = new Cursor (Cursor.WAIT_CURSOR);
  private static final Cursor normalCursor  = new Cursor (Cursor.DEFAULT_CURSOR);
  private static final Cursor pendingCursor = new Cursor (Cursor.CROSSHAIR_CURSOR);

  private String  password = null;

  // State Info
  boolean okToDraw        = false;
  boolean imageReady      = false;
  boolean keepRefreshing  = true;
  boolean keyboardEnabled = true;

  String status;

  Dimension bkgImageDim;

  private static final int SCROLL_TIMEOUT = 250;   // In Milli Seconds  
  private Timer            vertScrollTimer = null;
  private Timer            horzScrollTimer = null;
  private int              vertScrollPos   = 0;
  private int              horzScrollPos   = 0;

  // Should we print out debug messages?
  private boolean debug = false;

  public TreeCanvas (Geneo inGeneo, Globals inGlobals, MainPanel inPanel)
  {
    geneo   = inGeneo;
    globals = inGlobals;
    panel   = inPanel;
    frame   = null;

    addMouseListener (this);
    addKeyListener   (this);

    if (globals.statusCode == Globals.statusOK)
    {
      if (globals.backgroundImage != null)
        new ImageLoader (globals.backgroundImage, this, this);
      Thread t = new Thread(this, "Parser Thread");
      t.start();
    }
  }

  public TreeCanvas (Geneo inGeneo, Globals inGlobals, MainFrame inFrame)
  {
    geneo   = inGeneo;
    globals = inGlobals;
    panel   = null;
    frame   = inFrame;

    addMouseListener (this);
    addKeyListener   (this);

    if (globals.statusCode == Globals.statusOK)
    {
      if (globals.backgroundImage != null)
        new ImageLoader (globals.backgroundImage, this, this);
      Thread t = new Thread(this, "Parser Thread");
      t.start();
    }
  }

  /**
  ** This method will update the AppletContext for the tree.
  */
  public void updateContext (AppletContext newContext)
  {
    globals.updateContext (newContext);
  }

  public void setPrimary (int newPrimary)
  {
    setCenterPerson (people.getPersonFromId (newPrimary));
  }

  public void setPrimary ()
  {
    setCenterPerson (people.getSelectedPerson());
  }

  public void run ()
  {
    // Parse the tree
    state = STATE_LOADING;
    setCursor (loadingCursor);
    Parser.parseFile (globals, this);
  }

  public void setImage (Image inImage)
  {
    bkgImageDim = new Dimension (inImage.getWidth(null), inImage.getHeight(null));
    bkgImage = inImage;
//    update (getGraphics());
    redraw();
  }

  public synchronized void addNotify()
  {
    super.addNotify();

    FontMetrics fontMetrics;
    Graphics    g = getGraphics();
    Rectangle   r = this.getBounds();

    // Calculate the values needed to output statistics while loading
    // the .GED file

    statusFont  = g.getFont();
    fontMetrics = g.getFontMetrics();
    rowHeight   = fontMetrics.getHeight();
    firstRow    = rowHeight;
    secondRow   = rowHeight*2;
    thirdRow    = rowHeight*3;
    statusRow   = secondRow;

    treeFontSize = globals.initialZoom;

    if (debug)
      System.out.println("TreeCanvas::addNotify - setting setupFlags"); //zombie

    setupFlags |= SETUP_ADD_NOTIFY;

    if (setupFlags == SETUP_NEED_INIT)  // If all flags are set...
    {
      if (debug)
        System.out.println("TreeCanvas::addNotify - initializing tree"); //zombie
      initTree();
    }
  }

  public synchronized void notifyFileName(String fileName)
  {
    this.fileNameStr = "Parsing " + fileName;
  }

  public synchronized void notifySetup(PeopleList people,
                                       String     password,
                                       int        recordCount)
  {
    URL             url;
    DataInputStream stream;

    this.people           = people;
    this.password         = password;
    this.totalRecordCount = recordCount;

    if ((frame != null) &&
        (password != null))
    {
      // Notify the frame that a password can be entered.
      frame.passwordRequired (true);
    }

    setupFlags |= SETUP_NOTIFY_SETUP;

    if (setupFlags == SETUP_NEED_INIT)  // If all flags are set...
    {
      initTree();
    }
  }

  static final int loadBoxBorder = 5;
  static final int loadBoxSpacer = 2;

  private void calcLoadBoxSizes(Rectangle r)
  {

    FontMetrics fm            = getFontMetrics(statusFont);
    int         strWidth      = fm.stringWidth(fileNameStr);
    int         loadBoxWidth  = strWidth + 2*loadBoxBorder;
    int         loadBoxHeight = 2*rowHeight + 2*loadBoxBorder + loadBoxSpacer;
    int         loadBoxX      = (r.width - loadBoxWidth) / 2;
    int         loadBoxY      = 2;
    int         barX          = loadBoxX + loadBoxBorder;
    int         barY          = loadBoxY + loadBoxBorder + rowHeight + loadBoxSpacer;

    loadTextX = loadBoxX + loadBoxBorder;
    loadTextY = loadBoxY + loadBoxBorder + rowHeight - 2;

    loadBox = new Box3D(globals,
                        loadBoxX,
                        loadBoxY,
                        loadBoxWidth,
                        loadBoxHeight);

    loadBar = new Box3D(globals,
                        barX,
                        barY,
                        strWidth,
                        rowHeight);
  }

  public synchronized void updateRecordCount (int curCount)
  {
    curRecordCount = curCount;

    if (loadBar != null)
    {
      Graphics g = getGraphics();
      if (g != null)
        loadBar.fill (g, curRecordCount, totalRecordCount);
      if (keepRefreshing)
      {
        if ((curRecordCount % 10) == 1)  // Start with 1 so first person gets drawn
        {
          // We will keep updating the display every tenth record until
          // shouldDraw returns false at least once.  That's because even
          // calling shouldDraw every tenth record slows down our download
          // time by nearly 50%!  Also, make our last refresh when we've read
          // about 30 people!
          keepRefreshing = people.shouldDraw() && (curRecordCount < 32);

          // We must re-set the center person so
          // that PeopleList will re-build it's data
          // tree to include people who have been added
          // since the last time the tree was built.
          if ((setupFlags == SETUP_DONE) &&  // If all flags are set
              (frame != null) &&
              (people.getCenterPerson() != null) &&
              (people.getCenterPerson().fullName != null))
          {
            frame.setTitle ("InterneTree: " + people.getCenterPerson().fullName);
            people.setCenterPerson(people.getCenterPerson());
            people.calcTree();

            // This is a kludge to both validate that the old current position is
            // still valid.
            people.setHorz (people.getHorzCurPos());
            people.setVert (people.getVertCurPos());

            adjustScrollBars();
            redraw();
          }
        }
      }
    }
  }

  public void updateStatus(String newStatus)
  {
    if (status == null)
      status = newStatus;
    else
      status += ", " + newStatus;
  }

  public synchronized void notifyDone ()
  {
    parsing   = false;

    setCursor (normalCursor);
    state = STATE_NORMAL;
    if (globals.statusCode == Globals.statusOK)
    {
      setCenterPerson (people.getCenterPerson());
    }

    // If this flag hasn't already been set, it never will, and it means
    // an error occured.  Set it now so that we can draw our error.
    setupFlags |= SETUP_NOTIFY_SETUP;
    if (setupFlags == SETUP_NEED_INIT)  // If all flags are set except SETUP_DONE
    {
      initTree();
    }

    if (setupFlags == SETUP_DONE)  // If all flags are set...
    {
      redraw();
    }
  }

  // TreeCanvas#update

  public void update (Graphics g)
  {
    paint(g);
//    if (offscreenImage != null)
//    {
//      System.out.println ("TreeCanvas::update - calling g.drawImage");
//      g.drawImage (offscreenImage, 0, 0, this);
//    }
  }

  public void paint (Graphics g)
  {
    //zombie  update(g);
    super.paint(g);
    if (offscreenImage != null)
    {
      g.drawImage (offscreenImage, 0, 0, this);
    }
  }

  private void createOffscreenImage()
  {
    offscreenImage = createImage(canvasWidth, canvasHeight);
    if (offscreenImage != null)
    {
      offscreenGraphics = offscreenImage.getGraphics();

      if (offscreenGraphics != null)
      {
        // Calculate this stuff now that we have an offscreen image
        setTreeFonts();
        calcLoadBoxSizes(this.getBounds());
      }
      else
        System.out.println("TreeCanvas::createOffscreenImage - getGraphics failed");
    }
    else
      System.out.println("TreeCanvas::createOffscreenImage - createImage failed");
  }

  private void initTree()
  {
    if (setupFlags == SETUP_NEED_INIT)
    {
      createOffscreenImage();

      if (people != null)
      {
        people.setScreenSize(canvasHeight, canvasWidth);
        people.home();
        people.calcTree();

        // This is a kludge to both validate that the old current position is
        // still valid.
        people.setHorz (people.getHorzCurPos());
        people.setVert (people.getVertCurPos());
      }

      adjustScrollBars();
      redraw();

      setupFlags |= SETUP_INIT_TREE;
    }
  }

  private void redraw()
  {
    if ((offscreenGraphics != null))
    {
      Rectangle rect = this.getBounds();

      switch (globals.statusCode)
      {
        case Globals.statusOK:

          if ((bkgImage == null) || globals.clearBackground)
          {
            offscreenGraphics.setColor(globals.backgroundColor);
            offscreenGraphics.fillRect(0, 0, rect.width, rect.height);
            offscreenGraphics.setColor(globals.foregroundColor);
          }

          // Draw the image
          if (bkgImage != null)
          {
            switch (globals.bkgImageLayout)
            {
              case globals.imgTile:
                for (int curY = 0; curY < rect.height; curY += bkgImageDim.height)
                  for (int curX = 0; curX < rect.width; curX += bkgImageDim.width)
                    offscreenGraphics.drawImage (bkgImage, curX, curY, this);
                break;
              default:
                // Center the image
                offscreenGraphics.drawImage (bkgImage,
                                             0 + (rect.width  - bkgImageDim.width )/2,
                                             0 + (rect.height - bkgImageDim.height)/2,
                                             this);
            }
          }

          if (people != null)
          {
            people.drawTree (offscreenGraphics);
          }

          if (parsing)
          {
            // Draw status

            offscreenGraphics.setFont(statusFont);

            if (loadBar != null)
            {
              loadBox.draw (offscreenGraphics);
              offscreenGraphics.drawString (fileNameStr,
                                            loadTextX,
                                            loadTextY);
              loadBar.draw (offscreenGraphics);
              loadBar.fill (offscreenGraphics, curRecordCount, totalRecordCount);
            }
          }
          break;
        case Globals.statusError:
          offscreenGraphics.setColor(Color.black);
          offscreenGraphics.drawString (fileNameStr,        0, firstRow);
          offscreenGraphics.drawString (globals.statusDesc, 0, statusRow);
          break;
        case Globals.statusBadParam:
        {
          FontMetrics fontMetrics = offscreenGraphics.getFontMetrics();
          int i, x0, x1, x2, x3;
          int maxLen1 = 0, maxLen2 = 0, t;

          for (i = 0; i < globals.paramInfo.length; i++)
          {
            t = fontMetrics.stringWidth(globals.paramInfo[i][0]);
            if (t > maxLen1)
              maxLen1 = t;

            t = fontMetrics.stringWidth(globals.paramInfo[i][1]);
            if (t > maxLen2)
              maxLen2 = t;
          }

          x0 = rect.x + 2;
          x1 = x0 + fontMetrics.stringWidth("    ");
          x2 = x1 + maxLen1 + 10;
          x3 = x2 + maxLen2 + 10;

          offscreenGraphics.drawString (fileNameStr, 0, firstRow);
          offscreenGraphics.drawString ("Bad Parameter: " + globals.statusDesc, x0, statusRow);
          offscreenGraphics.drawString ("Valid parameters are as follows:", x0, statusRow + firstRow);

          offscreenGraphics.drawString (globals.paramHeadings[0], x1, statusRow + secondRow);
          offscreenGraphics.drawString (globals.paramHeadings[1], x2, statusRow + secondRow);
          offscreenGraphics.drawString (globals.paramHeadings[2], x3, statusRow + secondRow);

          for (i = 0; i < globals.paramInfo.length; i++)
          {
            offscreenGraphics.drawString (globals.paramInfo[i][0], x1, (statusRow + thirdRow) + (i*rowHeight));
            offscreenGraphics.drawString (globals.paramInfo[i][1], x2, (statusRow + thirdRow) + (i*rowHeight));
            offscreenGraphics.drawString (globals.paramInfo[i][2], x3, (statusRow + thirdRow) + (i*rowHeight));
          }
          break;
        }
      }
      repaint();
    }
  }

  /**
   * Implements ActionListener::actionPerformed, used to trap menu events.
   */
  public void actionPerformed (ActionEvent e)
  {
    MenuItem item = (MenuItem) e.getSource();
    if (item == find)
    {
      find();
    }
    else if (item == zoomIn)
    {
      zoomIn();
    }
    else if (item == zoomOut)
    {
      zoomOut();
    }
    //else if (item == redraw)
    //{
    //  forceRedraw();
    //}
    else if (item == details)
    {
      showDetails(personUnderMenu);
    }
    else if (item == center)
    {
      setCenterPerson(personUnderMenu);
    }
    else if (item == viewAll)
    {
      viewAll();
    }
    //else if (item == back)
    //{
    //  back();
    //}
    //else if (item == forward)
    //{
    //  forward();
    //}
    else if (item == help)
    {
      helpContents();
    }
    //else if (item == about)
    //{
    //  helpAbout();
    //}
  }

  //----------------------------------------
  // Implements MouseListener::mousePressed
  //----------------------------------------
  public void mousePressed (MouseEvent e)
  {
    switch (state)
    {
      case STATE_LOADING:
        // do nothing
        break;
      case STATE_NORMAL:
      {
        int mods       = e.getModifiers();
        int x          = e.getX();
        int y          = e.getY();
        int clickCount = e.getClickCount();

        if (debug)
          System.out.println ("Mouse click at (" + x + "," + y + ")"); //zombie

        if ((mods & InputEvent.BUTTON3_MASK) != 0)
        {
          PopupMenu pmenu = new PopupMenu();
          add (pmenu);

          if (people != null)
          {
            if (find == null)
            {
              // find is used as a flag here, but it means that none of the menu
              // items have been allocated.  Allocate them here and then add
              // 'this' as an action listener to them
              find    = new MenuItem ("Find");
              zoomIn  = new MenuItem ("Zoom In");
              zoomOut = new MenuItem ("Zoom Out");
              //redraw  = new MenuItem ("Redraw");
              viewAll = new MenuItem ("View All");
              details = new MenuItem ("Details");
              center  = new MenuItem ("Make Primary Individual");
              //back    = new MenuItem ("<-Back");
              //forward = new MenuItem ("Forward->");
              help    = new MenuItem ("Help");
              //about   = new MenuItem ("About");

              find   .addActionListener (this); 
              zoomIn .addActionListener (this);
              zoomOut.addActionListener (this);
              //redraw .addActionListener (this);
              viewAll.addActionListener (this);
              details.addActionListener (this);
              center .addActionListener (this);
              //back   .addActionListener (this);
              //forward.addActionListener (this);
              help   .addActionListener (this);
              //about  .addActionListener (this);
            }
            pmenu.add(find   );
            pmenu.add(zoomIn );
            pmenu.add(zoomOut);
            //pmenu.add(redraw );
            if (password != null)
            {
              pmenu.add(viewAll);
            }
            if ((personUnderMenu = people.getPersonUnderPoint(x, y)) != null)
            {
              pmenu.addSeparator();
              pmenu.add(details);
              pmenu.add(center );
            //  pmenu.add(back   );
            //  pmenu.add(forward);
            }
            pmenu.addSeparator();
          }
          pmenu.add(help);
          //pmenu.add(about);

          pmenu.show(this,x,y);
        } // right mouse click
        else if ((mods & InputEvent.BUTTON2_MASK) != 0)
        {
          // Do middle-mouse button action
          if (people != null)
          {
            Person personUnderMouse = people.getPersonUnderPoint (x, y);

            if (personUnderMouse != null)
            {
              setCenterPerson (personUnderMouse);
            }
          }
        } // middle mouse click
        else
        {
          if (people != null)
          {
            if (clickCount <= 1)
            {
              DrawingObject box = people.getBoxUnderPoint (x, y);
              // Do single left click action
              if (box != null)
              {
                people.setSelectedBox (offscreenGraphics, box);
                repaint();
              }
            }
            else
            {
              // Do double left click action
              Person personUnderMouse = people.getPersonUnderPoint (x, y);

              if (personUnderMouse != null)
              {
                showDetails (personUnderMouse);
              } // if personUnderMouse != null
            } // double click
          } // if people != null
        } // left mouse click
        break;
      } // case STATE_NORMAL

//      case STATE_PENDING_DETAILS:
//      {
//        int mods = e.getModifiers();
//        int x    = e.getX();
//        int y    = e.getY();
//
//        Person personUnderMouse = people.getPersonUnderPoint (x, y);
//        state = STATE_NORMAL;
//        setCursor (normalCursor);
//
//        if (personUnderMouse != null)
//        {
//          showDetails (personUnderMouse);
//        }
//      }

    } // switch state
  } // MouseListener::mouseClicked

  //------------------------------
  // Unused mouseListener methods
  //------------------------------
  public void mouseClicked  (MouseEvent e) {;}
  public void mouseReleased (MouseEvent e) {;}
  public void mouseEntered  (MouseEvent e) {;}
  public void mouseExited   (MouseEvent e) {;}


  //-----------------------------------------
  // Implements KeyListener::keyTyped method
  //-----------------------------------------
  public void keyPressed (KeyEvent e)
  {
    if (state == STATE_LOADING)
      return;

    int key  = e.getKeyCode();
    int mods = e.getModifiers();

    if (keyboardEnabled)
    {
      if ((mods & InputEvent.CTRL_MASK) == 0)
      {
        // The CTRL key was not pushed:
        switch (key)
        {
          case KeyEvent.VK_PAGE_UP:
            pageUp();
            break;
          case KeyEvent.VK_PAGE_DOWN:
            pageDown();
            break;
          case KeyEvent.VK_UP:
            incUp();
            break;
          case KeyEvent.VK_DOWN:
            incDown();
            break;
          case KeyEvent.VK_LEFT:
            incLeft();
            break;
          case KeyEvent.VK_RIGHT:
            incRight();
            break;
          case KeyEvent.VK_HOME:
            home();
            break;
          case KeyEvent.VK_F:
            find();
            break;
          case KeyEvent.VK_R:
            forceRedraw();
            break;
          case KeyEvent.VK_D:
            showDetails (people.getSelectedPerson());
            break;
          case KeyEvent.VK_P:
            setPrimary();
            break;
          case KeyEvent.VK_V:
            viewAll();
            break;
          case KeyEvent.VK_HELP:
          case KeyEvent.VK_H:
            helpContents();
            break;
          case KeyEvent.VK_A:
            helpAbout();
            break;
          case KeyEvent.VK_T:
            testKeyCodes();
            break;
          case KeyEvent.VK_I:
            zoomIn();
            break;
          case KeyEvent.VK_O:
            zoomOut();
            break;
          //case KeyEvent.VK_B:       //zombie
          //  debug        = !debug;  //zombie
          //  people.debug =  debug;  //zombie
          //  break;                  //zombie
          case KeyEvent.VK_Z:
//            (new EntryDlg(getFrameParent(),
//                          "HTML Target",
//                          "Choose a new HTML target:",
//                          false,
//                          null,
//                          this,
//                          TASK_NEW_HTML_TARGET)).show();   //zombie
//            break;
          default:
            // These keys turned up distinct but undefined key codes on
            // my keyboard, and I was forced to use the key chars instead.
            // Probably the reason is that the keys associated with these
            // keys varies from keyboard to keyboard and the java designers
            // didn't know what symbolic constant to give the key codes.
            switch (e.getKeyChar())
            {
              case '+':
              case '=':
                zoomIn();
                break;
              case '-':
              case '_':
                zoomOut();
                break;
              case '?':
              case '/':
                helpContents();
                break;
            }
            break;
        } // switch key
      } // if ! ctrl
      else
      {
        // The CTRL key was down:
        switch (key)
        {
          case KeyEvent.VK_PAGE_UP:
            pageLeft();
            break;
          case KeyEvent.VK_PAGE_DOWN:
            pageRight();
            break;
        }
      }
    }
  } // KeyListener::keyTyped

  //----------------------------------------
  // Unused KeyListener methods
  //----------------------------------------
  public void keyTyped    (KeyEvent e) {;}
  public void keyReleased (KeyEvent e) {;}


  private Frame getFrameParent()
  {
    if (frame != null)
      return frame;
    else
      return globals.appletFrameParent;
  }

  private void setTreeFonts ()
  {
    if (offscreenGraphics != null)
    {
      Font        treeNameFont = new Font ("Helvetica", Font.BOLD , treeFontSize);
      Font        treeDateFont = new Font ("Helvetica", Font.PLAIN, treeFontSize);
      FontMetrics treeNameFM = offscreenGraphics.getFontMetrics (treeNameFont);
      FontMetrics treeDateFM = offscreenGraphics.getFontMetrics (treeDateFont);

      if (people != null)
        people.setFonts (treeNameFont, treeDateFont, treeNameFM, treeDateFM);
    }
    else
      System.out.println("TreeCanvas::setTreeFonts - offscreenGraphics is null");
  }

  public void pageUp()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.pageUp())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void pageDown()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.pageDown())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void pageLeft()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.pageLeft())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void pageRight()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.pageRight())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void incUp()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.incUp())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void incDown()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.incDown())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void incLeft()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.incLeft())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void incRight()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.incRight())
    {
      adjustScrollBars();
      redraw();
    }
  }

  public synchronized void setVert(int pos)
  {
    if (state == STATE_LOADING)
      return;

    vertScrollPos = pos;

    if (vertScrollTimer == null)
    {
      vertScrollTimer = new Timer (SCROLL_TIMEOUT, this, TASK_VERT_SCROLL_TIMEOUT);
    }
    else
    {
      vertScrollTimer.reset();
    }
  }

  public synchronized void setHorz(int pos)
  {
    if (state == STATE_LOADING)
      return;

    horzScrollPos = pos;

    if (horzScrollTimer == null)
    {
      horzScrollTimer = new Timer (SCROLL_TIMEOUT, this, TASK_HORZ_SCROLL_TIMEOUT);
    }
    else
    {
      horzScrollTimer.reset();
    }
  }

  private void doVertAbsoluteScroll ()
  {
    if ((people != null) &&
        people.setVert(vertScrollPos))
    {
      adjustScrollBars();
      redraw();
    }
  }

  private void doHorzAbsoluteScroll ()
  {
    if ((people != null) &&
        people.setHorz(horzScrollPos))
    {
      adjustScrollBars();
      redraw();
    }
  }

  public void home()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        people.home())
    {
      // This is a kludge to both validate that the old current position is
      // still valid.
      people.setHorz (people.getHorzCurPos());
      people.setVert (people.getVertCurPos());

      adjustScrollBars();
      redraw();
    }
  }

  public synchronized void setBounds(int x,
                                     int y,
                                     int width,
                                     int height)
  {
    super.setBounds(x, y, width, height);

    canvasWidth  = width;
    canvasHeight = height;

    if (debug)
      System.out.println("TreeCanvas::setBounds - setting setupFlags"); //zombie
    setupFlags |= SETUP_SET_BOUNDS;

    if (setupFlags == SETUP_NEED_INIT)  // If all flags are set...
    {
      if (debug)
        System.out.println("TreeCanvas::setBounds - initializing tree"); //zombie
      initTree();
    }
    else if (setupFlags == SETUP_DONE)
    {
      if (debug)
        System.out.println("TreeCanvas::setBounds - resizing tree"); //zombie
      createOffscreenImage();
      if (people != null)
      {
        people.setScreenSize(canvasHeight, canvasWidth);
        people.calcTree();

        // This is a kludge to both validate that the old current position is
        // still valid.
        people.setHorz (people.getHorzCurPos());
        people.setVert (people.getVertCurPos());
      }

      adjustScrollBars();
      redraw();
    }
  }

  private void adjustScrollBars ()
  {
    if (people != null)
    {
      if (frame != null)
      {
        frame.adjustScrollBars (people.getHorzCurPos(), canvasWidth , people.getHorzMinPos(), people.getHorzMaxPos(),
                                people.getVertCurPos(), canvasHeight, people.getVertMinPos(), people.getVertMaxPos());
      }
      else if (panel != null)
      {
        panel.adjustScrollBars (people.getHorzCurPos(), canvasWidth , people.getHorzMinPos(), people.getHorzMaxPos(),
                                people.getVertCurPos(), canvasHeight, people.getVertMinPos(), people.getVertMaxPos());
      }
    }
  }

  public synchronized void callback(int    taskId,
                                    int    result,
                                    String strValue1,
                                    String strValue2)
  {
    switch (taskId)
    {
      case TASK_VIEW_ALL:
      {
        // This task indicates that the user has entered a password
        // to view all of the people in the data base.
        if (result == 0)
        {
          // This means the user pressed "OK" so we should compare the value
          // with what's in the real password.
          if (strValue1.equals(password))
          {
            password = null;  // we don't need a password any more
            if (frame != null)
            {
              frame.passwordRequired (false);
            }
            people.showHidden();
            forceRedraw();
          }
          else
          {
            (new InfoDlg(getFrameParent(), globals.context, globals.htmlTarget, this,
                         "Invalid Password",
                         "Invalid Password",
                         true)).show();
          }
        }
        break;
      }

      case TASK_VERT_SCROLL_TIMEOUT:
      {
        vertScrollTimer = null;
        doVertAbsoluteScroll();
        break;
      }
      
      case TASK_HORZ_SCROLL_TIMEOUT:
      {
        horzScrollTimer = null;
        doHorzAbsoluteScroll();
        break;
      }
      
      case TASK_NEW_HTML_TARGET: //zombie
      {
        globals.htmlTarget = strValue1;
        break;
      }

      default:
        System.out.println("TreeCanvas#callback: Unknown task id: " + taskId);
    }
  }

  public void find()
  {

    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        keyboardEnabled)
    {
      FindDlg findDlg;

      keyboardEnabled = false;
      findDlg = new FindDlg (getFrameParent(), globals.context, globals.htmlTarget, this, people);
      findDlg.show();
      keyboardEnabled = true;
    }
  }

  public void zoomIn()
  {
    if (state == STATE_LOADING)
      return;

    int newFontSize = treeFontSize + 2;

    if ((people != null) &&
        (newFontSize >= 0) &&
        (offscreenGraphics != null))
    {
      treeFontSize += 2;
      setTreeFonts();
      people.calcTree();

      // This is a kludge to both validate that the old current position is
      // still valid.
      people.setHorz (people.getHorzCurPos());
      people.setVert (people.getVertCurPos());

      adjustScrollBars();
      redraw();
    }
  }

  public void zoomOut()
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        (treeFontSize > 2) &&
        (offscreenGraphics != null))
    {
      treeFontSize -= 2;
      setTreeFonts();
      people.calcTree();

      // This is a kludge to both validate that the old current position is
      // still valid.
      people.setHorz (people.getHorzCurPos());
      people.setVert (people.getVertCurPos());

      adjustScrollBars();
      redraw();
    }
  }

  public void forceRedraw()
  {
    setCenterPerson (people.getCenterPerson());
  }

//  public void findDetailsTarget ()
//  {
//    if (state == STATE_LOADING)
//      return;
//
//    state = STATE_PENDING_DETAILS;
//    setCursor (pendingCursor);
//  }

  public void showDetails()
  {
    showDetails (people.getSelectedPerson());
  }

  public void showDetails(Person person)
  {
    boolean wentToWebPage = false;
    String  urlStr = null;
    DecimalFormat format = new DecimalFormat("0000");
    String        idString = null;

    if (state == STATE_LOADING)
      return;

    if (people != null)
    {
      if (person == null)
        person = people.getCenterPerson();

      if (globals.detailLoc != null)
      {
        try
        {
          idString = format.format(person.id);
          urlStr = globals.detailLoc + "/UHP-" + idString + ".html";
          // This will cause an exception if it's not a real URL
          URL url = new URL (globals.documentBase, urlStr);

          if (url != null)
          {
            if (globals.context != null)
            {
              if (globals.htmlTarget != null)
              {
                System.out.println("Showing details at: " + globals.htmlTarget); //zombie
                globals.context.showDocument(url, globals.htmlTarget);
              }
              else
              {
                System.out.println("Showing details"); //zombie
                globals.context.showDocument(url);
              }
              System.out.println("Back from showing details"); //zombie
              wentToWebPage = true;
            }
            else
              System.out.println("Invalid applet context");
          }
          else
            System.out.println("Couldn't create URL from \"" + urlStr + "\"");
        }
        catch (MalformedURLException e)
        {
          System.out.println("MalformedURLException: " + e);
          System.out.println("  On URL: \"" + urlStr + "\"");
        }
      }

      if (wentToWebPage == false)
      {
        new InfoDlg(getFrameParent(), globals.context, globals.htmlTarget, this,
                    "Details of " + person.fullName,
                    person.details,
                    false).show();
      }
    }
  }

  public void setCenterPerson(Person newCenterPerson)
  {
    if (state == STATE_LOADING)
      return;

    if ((people != null) &&
        (newCenterPerson != null))
    {
      if (frame != null)
        frame.setTitle ("InterneTree: " + newCenterPerson.fullName);
      people.setCenterPerson (newCenterPerson);
      people.calcTree();

      // This is a kludge to both validate that the old current position is
      // still valid.
      people.setHorz (people.getHorzCurPos());
      people.setVert (people.getVertCurPos());

      adjustScrollBars();
      redraw();
    }
  }

  public synchronized void viewAll()
  {
    if (state == STATE_LOADING)
      return;

    if (password != null)
    {
      keyboardEnabled = false;
      (new EntryDlg(getFrameParent(),
                    "Password",
                    "Enter Password",
                    true,
                    null,
                    this,
                    TASK_VIEW_ALL)).show();
      keyboardEnabled = true;
    }
  }

  public synchronized void back()
  {
    if ((people != null) &&
        people.back())
    {
      if (frame != null)
        frame.setTitle ("InterneTree: " + people.getCenterPerson().fullName);
      people.calcTree();

      // This is a kludge to both validate that the old current position is
      // still valid.
      people.setHorz (people.getHorzCurPos());
      people.setVert (people.getVertCurPos());

      adjustScrollBars();
      redraw();
    }
  }

  public synchronized void forward()
  {
    if ((people != null) &&
        people.forward())
    {
      if (frame != null)
        frame.setTitle ("InterneTree: " + people.getCenterPerson().fullName);
      people.calcTree();

      // This is a kludge to both validate that the old current position is
      // still valid.
      people.setHorz (people.getHorzCurPos());
      people.setVert (people.getVertCurPos());

      adjustScrollBars();
      redraw();
    }
  }

  public void helpContents()
  {
    if (globals.helpUrl != null)  // This could happen if URL is malformed
    {
      if (globals.htmlTarget != null)
      {
        System.out.println("Showing details at: " + globals.helpUrl); //zombie
        globals.context.showDocument(globals.helpUrl, globals.htmlTarget);
      }
      else
      {
        System.out.println("Showing details at: " + globals.helpUrl); //zombie
        globals.context.showDocument(globals.helpUrl);
      }
    }
  }

  public void helpAbout()
  {
    if (state == STATE_LOADING)
      return;

    (new InfoDlg(getFrameParent(), globals.context, globals.htmlTarget, this,
                 "About InterneTree (tm)",
                 "InterneTree (tm) was developed for Genealogy.com by\n" +
                 "Don Baldwin, mailto:donb@qualcomm.com  (c) 2000 Don Baldwin.\n" +
                 "All rights reserved.",
                 true)).show();
  }

  private void testKeyCodes()
  {
    System.out.println ("VK_0             = " + KeyEvent.VK_0             );
    System.out.println ("VK_1             = " + KeyEvent.VK_1             );
    System.out.println ("VK_2             = " + KeyEvent.VK_2             );
    System.out.println ("VK_3             = " + KeyEvent.VK_3             );
    System.out.println ("VK_4             = " + KeyEvent.VK_4             );
    System.out.println ("VK_5             = " + KeyEvent.VK_5             );
    System.out.println ("VK_6             = " + KeyEvent.VK_6             );
    System.out.println ("VK_7             = " + KeyEvent.VK_7             );
    System.out.println ("VK_8             = " + KeyEvent.VK_8             );
    System.out.println ("VK_9             = " + KeyEvent.VK_9             );
    System.out.println ("VK_A             = " + KeyEvent.VK_A             );
    System.out.println ("VK_ACCEPT        = " + KeyEvent.VK_ACCEPT        );
    System.out.println ("VK_ADD           = " + KeyEvent.VK_ADD           );
    System.out.println ("VK_ALT           = " + KeyEvent.VK_ALT           );
    System.out.println ("VK_B             = " + KeyEvent.VK_B             );
    System.out.println ("VK_BACK_QUOTE    = " + KeyEvent.VK_BACK_QUOTE    );
    System.out.println ("VK_BACK_SLASH    = " + KeyEvent.VK_BACK_SLASH    );
    System.out.println ("VK_BACK_SPACE    = " + KeyEvent.VK_BACK_SPACE    );
    System.out.println ("VK_C             = " + KeyEvent.VK_C             );
    System.out.println ("VK_CANCEL        = " + KeyEvent.VK_CANCEL        );
    System.out.println ("VK_CAPS_LOCK     = " + KeyEvent.VK_CAPS_LOCK     );
    System.out.println ("VK_CLEAR         = " + KeyEvent.VK_CLEAR         );
    System.out.println ("VK_CLOSE_BRACKET = " + KeyEvent.VK_CLOSE_BRACKET );
    System.out.println ("VK_COMMA         = " + KeyEvent.VK_COMMA         );
    System.out.println ("VK_CONTROL       = " + KeyEvent.VK_CONTROL       );
    System.out.println ("VK_CONVERT       = " + KeyEvent.VK_CONVERT       );
    System.out.println ("VK_D             = " + KeyEvent.VK_D             );
    System.out.println ("VK_DECIMAL       = " + KeyEvent.VK_DECIMAL       );
    System.out.println ("VK_DELETE        = " + KeyEvent.VK_DELETE        );
    System.out.println ("VK_DIVIDE        = " + KeyEvent.VK_DIVIDE        );
    System.out.println ("VK_DOWN          = " + KeyEvent.VK_DOWN          );
    System.out.println ("VK_E             = " + KeyEvent.VK_E             );
    System.out.println ("VK_END           = " + KeyEvent.VK_END           );
    System.out.println ("VK_ENTER         = " + KeyEvent.VK_ENTER         );
    System.out.println ("VK_EQUALS        = " + KeyEvent.VK_EQUALS        );
    System.out.println ("VK_ESCAPE        = " + KeyEvent.VK_ESCAPE        );
    System.out.println ("VK_F             = " + KeyEvent.VK_F             );
    System.out.println ("VK_F1            = " + KeyEvent.VK_F1            );
    System.out.println ("VK_F10           = " + KeyEvent.VK_F10           );
    System.out.println ("VK_F11           = " + KeyEvent.VK_F11           );
    System.out.println ("VK_F12           = " + KeyEvent.VK_F12           );
    System.out.println ("VK_F2            = " + KeyEvent.VK_F2            );
    System.out.println ("VK_F3            = " + KeyEvent.VK_F3            );
    System.out.println ("VK_F4            = " + KeyEvent.VK_F4            );
    System.out.println ("VK_F5            = " + KeyEvent.VK_F5            );
    System.out.println ("VK_F6            = " + KeyEvent.VK_F6            );
    System.out.println ("VK_F7            = " + KeyEvent.VK_F7            );
    System.out.println ("VK_F8            = " + KeyEvent.VK_F8            );
    System.out.println ("VK_F9            = " + KeyEvent.VK_F9            );
    System.out.println ("VK_FINAL         = " + KeyEvent.VK_FINAL         );
    System.out.println ("VK_G             = " + KeyEvent.VK_G             );
    System.out.println ("VK_H             = " + KeyEvent.VK_H             );
    System.out.println ("VK_HELP          = " + KeyEvent.VK_HELP          );
    System.out.println ("VK_HOME          = " + KeyEvent.VK_HOME          );
    System.out.println ("VK_I             = " + KeyEvent.VK_I             );
    System.out.println ("VK_INSERT        = " + KeyEvent.VK_INSERT        );
    System.out.println ("VK_J             = " + KeyEvent.VK_J             );
    System.out.println ("VK_K             = " + KeyEvent.VK_K             );
    System.out.println ("VK_KANA          = " + KeyEvent.VK_KANA          );
    System.out.println ("VK_KANJI         = " + KeyEvent.VK_KANJI         );
    System.out.println ("VK_L             = " + KeyEvent.VK_L             );
    System.out.println ("VK_LEFT          = " + KeyEvent.VK_LEFT          );
    System.out.println ("VK_M             = " + KeyEvent.VK_M             );
    System.out.println ("VK_META          = " + KeyEvent.VK_META          );
    System.out.println ("VK_MODECHANGE    = " + KeyEvent.VK_MODECHANGE    );
    System.out.println ("VK_MULTIPLY      = " + KeyEvent.VK_MULTIPLY      );
    System.out.println ("VK_N             = " + KeyEvent.VK_N             );
    System.out.println ("VK_NONCONVERT    = " + KeyEvent.VK_NONCONVERT    );
    System.out.println ("VK_NUM_LOCK      = " + KeyEvent.VK_NUM_LOCK      );
    System.out.println ("VK_NUMPAD0       = " + KeyEvent.VK_NUMPAD0       );
    System.out.println ("VK_NUMPAD1       = " + KeyEvent.VK_NUMPAD1       );
    System.out.println ("VK_NUMPAD2       = " + KeyEvent.VK_NUMPAD2       );
    System.out.println ("VK_NUMPAD3       = " + KeyEvent.VK_NUMPAD3       );
    System.out.println ("VK_NUMPAD4       = " + KeyEvent.VK_NUMPAD4       );
    System.out.println ("VK_NUMPAD5       = " + KeyEvent.VK_NUMPAD5       );
    System.out.println ("VK_NUMPAD6       = " + KeyEvent.VK_NUMPAD6       );
    System.out.println ("VK_NUMPAD7       = " + KeyEvent.VK_NUMPAD7       );
    System.out.println ("VK_NUMPAD8       = " + KeyEvent.VK_NUMPAD8       );
    System.out.println ("VK_NUMPAD9       = " + KeyEvent.VK_NUMPAD9       );
    System.out.println ("VK_O             = " + KeyEvent.VK_O             );
    System.out.println ("VK_OPEN_BRACKET  = " + KeyEvent.VK_OPEN_BRACKET  );
    System.out.println ("VK_P             = " + KeyEvent.VK_P             );
    System.out.println ("VK_PAGE_DOWN     = " + KeyEvent.VK_PAGE_DOWN     );
    System.out.println ("VK_PAGE_UP       = " + KeyEvent.VK_PAGE_UP       );
    System.out.println ("VK_PAUSE         = " + KeyEvent.VK_PAUSE         );
    System.out.println ("VK_PERIOD        = " + KeyEvent.VK_PERIOD        );
    System.out.println ("VK_PRINTSCREEN   = " + KeyEvent.VK_PRINTSCREEN   );
    System.out.println ("VK_Q             = " + KeyEvent.VK_Q             );
    System.out.println ("VK_QUOTE         = " + KeyEvent.VK_QUOTE         );
    System.out.println ("VK_R             = " + KeyEvent.VK_R             );
    System.out.println ("VK_RIGHT         = " + KeyEvent.VK_RIGHT         );
    System.out.println ("VK_S             = " + KeyEvent.VK_S             );
    System.out.println ("VK_SCROLL_LOCK   = " + KeyEvent.VK_SCROLL_LOCK   );
    System.out.println ("VK_SEMICOLON     = " + KeyEvent.VK_SEMICOLON     );
    System.out.println ("VK_SEPARATER     = " + KeyEvent.VK_SEPARATER     );
    System.out.println ("VK_SHIFT         = " + KeyEvent.VK_SHIFT         );
    System.out.println ("VK_SLASH         = " + KeyEvent.VK_SLASH         );
    System.out.println ("VK_SPACE         = " + KeyEvent.VK_SPACE         );
    System.out.println ("VK_SUBTRACT      = " + KeyEvent.VK_SUBTRACT      );
    System.out.println ("VK_T             = " + KeyEvent.VK_T             );
    System.out.println ("VK_TAB           = " + KeyEvent.VK_TAB           );
    System.out.println ("VK_U             = " + KeyEvent.VK_U             );
    System.out.println ("VK_UNDEFINED     = " + KeyEvent.VK_UNDEFINED     );
    System.out.println ("VK_UP            = " + KeyEvent.VK_UP            );
    System.out.println ("VK_V             = " + KeyEvent.VK_V             );
    System.out.println ("VK_W             = " + KeyEvent.VK_W             );
    System.out.println ("VK_X             = " + KeyEvent.VK_X             );
    System.out.println ("VK_Y             = " + KeyEvent.VK_Y             );
    System.out.println ("VK_Z             = " + KeyEvent.VK_Z             );
  }
}
