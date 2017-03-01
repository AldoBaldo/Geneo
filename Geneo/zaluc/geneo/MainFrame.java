package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;

//+-- Class MainFrame --------------------------------------------------------+
//|                                                                           |
//| Syntax:       class MainFrame extends Panel implements ParserUpdates      |
//|                                                                           |
//| Description:  MainFrame contains the area just inside of the borders      |
//|               painted by the Geneo class.  It receives update messages    |
//|               from the parser and paints the current status of parsing    |
//|               to the screen.  It also creates the TreeCanvas and          |
//|               TreeControls components that will cover the MainFrame once  |
//|               parsing is complete.  It also loads a background image if   |
//|               one was specified in the parameters.                        |
//|                                                                           |
//| Methods:      public      MainFrame         (Globals  inGlobals)          |
//|                                                                           |
//|               public void addNotify         ()                            |
//|                                                                           |
//|               public void update            (Graphics g)                  |
//|                                                                           |
//|               public void paint             (Graphics g)                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class MainFrame extends    Frame
                implements WindowListener,
                           ActionListener,
                           KeyListener,
                           AdjustmentListener,
                           Runnable
{
  private Globals globals;
  private Geneo   geneo;

  private TreeCanvas canvas;
  private Scrollbar  hbar, vbar;
  private MenuBar    mb;
  private Menu       sm, vm, gm, bm, fm, hm;
  private MenuItem   find, zoomIn, zoomOut, details, center, primary, viewAll, back, forward, contents, about;

  public MainFrame (Geneo inGeneo, Globals inGlobals, int width, int height)
  {
    super ("InterneTree");

    globals = inGlobals;
    geneo   = inGeneo;

    setForeground(globals.foregroundColor);
    setBackground(globals.backgroundColor);

    //----------------------------------------------
    // Create the menus
    //----------------------------------------------
    mb = new MenuBar();

    mb.add(sm = new Menu("Search"));
    mb.add(vm = new Menu("View"));
    mb.add(gm = new Menu("Go"));
    //mb.add(bm = new Menu("Back"));
    //mb.add(fm = new Menu("Forward"));
    mb.add(hm = new Menu("Help"));
    mb.setHelpMenu(hm);

    sm.add(find     = new MenuItem("Find Person...", new MenuShortcut (KeyEvent.VK_F)));

    vm.add(zoomIn   = new MenuItem("Zoom In"       , new MenuShortcut (KeyEvent.VK_I)));
    vm.add(zoomOut  = new MenuItem("Zoom Out"      , new MenuShortcut (KeyEvent.VK_O)));
    vm.add(details  = new MenuItem("Details"       , new MenuShortcut (KeyEvent.VK_D)));
    vm.add(center   = new MenuItem("Center Tree"   , new MenuShortcut (KeyEvent.VK_HOME)));
    vm.add(primary  = new MenuItem("Change Primary Individual", new MenuShortcut (KeyEvent.VK_P)));
    viewAll = null; // this one is added later

    gm.add(back     = new MenuItem("<-Back"        , new MenuShortcut (KeyEvent.VK_LEFT)));
    gm.add(forward  = new MenuItem("Forward->"     , new MenuShortcut (KeyEvent.VK_RIGHT)));

    hm.add(contents = new MenuItem("Contents"      , new MenuShortcut (KeyEvent.VK_H)));
    hm.add(about    = new MenuItem("About"         , new MenuShortcut (KeyEvent.VK_A)));

    setMenuBar(mb);

    //----------------------------------------------
    // Create the canvas and scroll bars
    //----------------------------------------------
    canvas = new TreeCanvas(inGeneo, inGlobals, this);
    hbar   = new Scrollbar(Scrollbar.HORIZONTAL);
    vbar   = new Scrollbar(Scrollbar.VERTICAL);
    this.setLayout(new BorderLayout(0,0));
    this.add("Center", canvas);
    this.add("South" , hbar  );
    this.add("East"  , vbar  );
    this.setSize (width, height);

    //----------------------------------------------
    // Add all of our Listeners
    //----------------------------------------------
    addWindowListener (this);
    addKeyListener    (this);
    //bm      .addActionListener (this);
    //fm      .addActionListener (this);
    find    .addActionListener (this);
    zoomIn  .addActionListener (this);
    zoomOut .addActionListener (this);
    details .addActionListener (this);
    center  .addActionListener (this);
    primary .addActionListener (this);
    back    .addActionListener (this);
    forward .addActionListener (this);
    contents.addActionListener (this);
    about   .addActionListener (this);
    hbar    .addAdjustmentListener (this);
    vbar    .addAdjustmentListener (this);

    //----------------------------------------------
    // Place the window in the center of the screen
    //----------------------------------------------
    Dimension scrnSize = getToolkit().getScreenSize();
    setLocation ((scrnSize.width  - width)  / 2,
                 (scrnSize.height - height) / 2);
    this.requestFocus();
    this.show();
    this.toFront();
  }

  /**
  ** This method will update the AppletContext for the tree.
  */
  public void updateContext (AppletContext newContext)
  {
    canvas.updateContext (newContext);
  }

  /**
  ** Load the FindPerson dialog box
  */  
  public void find()
  {
    Thread t = new Thread(this, "MainPanel Find Thread");
    t.start();
  }

  /**
  ** Implements Thread::run()
  */
  public void run ()
  {
    canvas.find();
  }

  /**
  ** Zoom In
  */
  public void zoomIn()
  {
    canvas.zoomIn();
  }

  /**
  ** Zoom Out
  */
  public void zoomOut()
  {
    canvas.zoomOut();
  }

  /**
  ** Re-center the tree around the primary person
  */
  public void home()
  {
    canvas.home();
  }

  /**
  ** Set the specified individual as the primary person
  **
  ** @param newPrimare  the GEDCOM index of the person to set as primary
  */
  public void setPrimary (int newPrimary)
  {
    canvas.setPrimary (newPrimary);
  }

  /**
  ** Set the selected individual as the primary person
  */
  public void setPrimary ()
  {
    canvas.setPrimary ();
  }

  /**
  ** Go back to previous primary individual
  */
  public void back()
  {
    canvas.back();
  }

  /**
  ** Move forward to next primary individual
  */
  public void forward()
  {
    canvas.forward();
  }

  public void notifyDone ()
  {
    // This is just used to handle errors in the Geneo class.  All it does
    // is call notifyDone in the canvas.
    canvas.notifyDone();
  }

  public void passwordRequired(boolean pwdRequired)
  {
    if (pwdRequired && (viewAll == null))
    {
      viewAll = new MenuItem ("View All", new MenuShortcut (KeyEvent.VK_V));
      vm.add (viewAll);
      viewAll.addActionListener (this);
    }
    else if (!pwdRequired && (viewAll != null))
    {
      vm.remove (viewAll);
      viewAll = null;
    }
  }

  /**
   * Implements WindowListener::windowClosing
   */
  public void windowClosing (WindowEvent e)
  {
    geneo.closeInstance();
    dispose();
    //System.exit(0);
  }

  // Other WindowListener methods:
  public void windowOpened      (WindowEvent e) {;}
  public void windowClosed      (WindowEvent e) {;}
  public void windowIconified   (WindowEvent e) {;}
  public void windowDeiconified (WindowEvent e) {;}
  public void windowActivated   (WindowEvent e) {;}
  public void windowDeactivated (WindowEvent e) {;}

  /**
   * Implements ActionListener::actionPerformed, receives menu event
   * and passes it to the canvas object.
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
    else if (item == details)
    {
      // canvas.findDetailsTarget();
      canvas.showDetails();
    }
    else if (item == center)
    {
      home();
    }
    else if (item == primary)
    {
      setPrimary();
    }
    else if (item == viewAll)
    {
      canvas.viewAll();
    }
    else if (item == back)
    {
      back();
    }
    else if (item == forward)
    {
      forward();
    }
    else if (item == contents)
    {
      canvas.helpContents();
    }
    else if (item == about)
    {
      canvas.helpAbout();
    }
    //else if (item == bm)
    //{
    //  System.out.println ("Go Back");
    //}
    //else if (item == fm)
    //{
    //  System.out.println ("Go Forward");
    //}
  }

  //-----------------------------------------
  // Implements KeyListener::keyPressed method
  //-----------------------------------------
  public void keyPressed (KeyEvent e)
  {
    canvas.keyPressed (e);
  }

  //----------------------------------------
  // Unused KeyListener methods
  //----------------------------------------
  public void keyTyped  (KeyEvent e) {;}
  public void keyReleased (KeyEvent e) {;}

  /**
   * Implements AdjustmentListener::adjustmentValueChanged
   */
  public void adjustmentValueChanged (AdjustmentEvent e)
  {
    Scrollbar item = (Scrollbar) e.getAdjustable();

    if (item == hbar)
    {
      switch (e.getAdjustmentType())
      {
        case AdjustmentEvent.UNIT_DECREMENT:
          canvas.incLeft();
          break;
        case AdjustmentEvent.UNIT_INCREMENT:
          canvas.incRight();
          break;
        case AdjustmentEvent.BLOCK_DECREMENT:
          canvas.pageLeft();
          break;
        case AdjustmentEvent.BLOCK_INCREMENT:
          canvas.pageRight();
          break;
        case AdjustmentEvent.TRACK:
        {
          int pos = e.getValue();

          canvas.setHorz(pos);
          break;
        }
      }
    }
    else if (item == vbar)
    {
      switch (e.getAdjustmentType())
      {
        case AdjustmentEvent.UNIT_DECREMENT:
          canvas.incUp();
          break;
        case AdjustmentEvent.UNIT_INCREMENT:
          canvas.incDown();
          break;
        case AdjustmentEvent.BLOCK_DECREMENT:
          canvas.pageUp();
          break;
        case AdjustmentEvent.BLOCK_INCREMENT:
          canvas.pageDown();
          break;
        case AdjustmentEvent.TRACK:
          canvas.setVert(e.getValue());
          break;
      }
    }
  } // adjustmentEvent

  public void adjustScrollBars (int hbarValue,
                                int hbarVisible,
                                int hbarMin,
                                int hbarMax,
                                int vbarValue,
                                int vbarVisible,
                                int vbarMin,
                                int vbarMax)
  {
//          (new InfoDlg(globals.appletFrameParent, globals.context, null,
//                       "Debug Main Panel",
//                       "adjustScrollBars:\nhbarValue = " + hbarValue +
//                       "\nhbarVisible = " + hbarVisible +
//                       "\nhbarMin = " + hbarMin +
//                       "\nhbarMax = " + hbarMax +
//                       "\nvbarValue = " + vbarValue +
//                       "\nvbarVisible = " + vbarVisible +
//                       "\nvbarMin = " + vbarMin +
//                       "\nvbarMax = " + vbarMax,
//                       false)).show();
//    System.out.println("Adjusting scroll bars to Value   Visible   Min   Max");
//    System.out.println("            Horizontal: " + hbarValue + " " + hbarVisible + " " + hbarMin + " " + hbarMax);
//    System.out.println("              Vertical: " + vbarValue + " " + vbarVisible + " " + vbarMin + " " + vbarMax);
    hbar.setValues(hbarValue, hbarVisible, hbarMin, hbarMax);
    vbar.setValues(vbarValue, vbarVisible, vbarMin, vbarMax);
  }
}
