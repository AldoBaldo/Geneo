package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;

//+-- Class MainPanel --------------------------------------------------------+
//|                                                                           |
//| Syntax:       class MainPanel extends Panel implements ParserUpdates      |
//|                                                                           |
//| Description:  MainPanel contains the area just inside of the borders      |
//|               painted by the Geneo class.  It receives update messages    |
//|               from the parser and paints the current status of parsing    |
//|               to the screen.  It also creates the TreeCanvas and          |
//|               TreeControls components that will cover the MainPanel once  |
//|               parsing is complete.  It also loads a background image if   |
//|               one was specified in the parameters.                        |
//|                                                                           |
//| Methods:      public      MainPanel         (Globals  inGlobals)          |
//|                                                                           |
//|               public void addNotify         ()                            |
//|                                                                           |
//|               public void update            (Graphics g)                  |
//|                                                                           |
//|               public void paint             (Graphics g)                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class MainPanel extends    Panel
                implements AdjustmentListener,
                           Runnable
{
  private Globals globals;

  private TreeCanvas canvas;
  private Scrollbar  hbar, vbar;

  public MainPanel (Geneo inGeneo, Globals inGlobals)
  {
    Frame frame = inGlobals.appletFrameParent;

    globals = inGlobals;

    setForeground(globals.foregroundColor);
    setBackground(globals.backgroundColor);

    canvas = new TreeCanvas(inGeneo, inGlobals, this);
    hbar   = new Scrollbar(Scrollbar.HORIZONTAL);
    vbar   = new Scrollbar(Scrollbar.VERTICAL);
    this.setLayout(new BorderLayout(0,0));
    this.add("Center", canvas);
    this.add("South" , hbar  );
    this.add("East"  , vbar  );

    //----------------------------------------------
    // Add all of our Listeners
    //----------------------------------------------
    hbar.addAdjustmentListener (this);
    vbar.addAdjustmentListener (this);
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
    hbar.setValues(hbarValue, hbarVisible, hbarMin, hbarMax);
    vbar.setValues(vbarValue, vbarVisible, vbarMin, vbarMax);
  }
}
