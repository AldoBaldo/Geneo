package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

//+-- Class InfoDlg ----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class InfoDlg extends Dialog                                |
//|                                                                           |
//| Description:  InfoDlg puts up a simple dialog box containing text and     |
//|               an OK button.  The box uses MultiLineLabel to display the   |
//|               text, therefore the text can span multiple lines and        |
//|               URLs                                                        |
//|                                                                           |
//| Methods:      public         InfoDlg   (Frame         parent,             |
//|                                         AppletContext context,            |
//|                                         String        title,              |
//|                                         String        message,            |
//|                                         boolean       isModal)            |
//|                                                                           |
//|               public void    setText   (String inText)                    |
//|                                                                           |
//|               public void    addNotify ()                                 |
//|                                                                           |
//|               public boolean gotFocus  (Event  e,                         |
//|                                         Object arg)                       |
//|                                                                           |
//|               public void    show      ()                                 |
//|                                                                           |
//|               public boolean action    (Event  e,                         |
//|                                         Object arg)                       |
//|                                                                           |
//|                                                                           |
//|                                                                           |
//|                                                                           |
//|               private void   close     ()                                 |
//|                                                                           |
//|---------------------------------------------------------------------------+

class InfoDlg extends    Dialog
              implements WindowListener,
                         ActionListener,
                         KeyListener
{
  private static InfoDlg focalList = null;
  private InfoDlg        nextFocalReceiver = null;
  private InfoDlg        prevFocalReceiver = null;
  private Component      focusReceiver;
  private Button         okButton;
  private MultiLineLabel text;

  public InfoDlg (Frame          parent,
                  AppletContext  context,
                  String         htmlTarget,
                  Component      focusReceiver,
                  String         title,
                  String         message,
                  boolean        isModal)
  {
    super(parent, title, isModal);

    // Setup who to return focus to.
    if (focalList != null)
      focalList.prevFocalReceiver = this;
    nextFocalReceiver  = focalList;
    focalList          = this;
    this.focusReceiver = focusReceiver;

    setLayout(new BorderLayout(5,5));

//    message.setEnvironment(context, 20, 20, MultiLineLabel.CENTER);
//    add("Center", message);
    add("Center", text = new MultiLineLabel(message, context, htmlTarget, 20, 20, MultiLineLabel.CENTER));

    Panel p = new Panel();
    p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    p.add(okButton = new Button("OK"));
    add("South", p);

    //----------------------------------------------
    // Add all of our Listeners
    //----------------------------------------------
    addWindowListener (this);
    addKeyListener    (this);
    okButton.addActionListener (this);
    okButton.addKeyListener    (this);

    //----------------------------------------------
    // Place the window in the center of the screen
    //----------------------------------------------
    Dimension scrnSize = getToolkit().getScreenSize();
    pack();
    setLocation ((scrnSize.width  - getSize().width)  / 2,
                 (scrnSize.height - getSize().height) / 2);
  }

  public void setText(String inText)
  {
    text.newLabel (inText);
  }

  // This method is invoked after our dialog is first created
  // but before it can actually be displayed.  After we've
  // invoked our superclass's addNotify() method, we have font
  // metrics and can successfully call measure() to figure out
  // how big the label is.

//  public void addNotify()
//  {
//    super.addNotify();
//    pack();
//  }                                                                                       

  // Thanks to Steffen Krug <pdgsvoeu@ford.com> for this code (and the german? comment)
//  public void setVisible(boolean visible)
//  {
//    if (visible)
//    {
//      pack();
//      //Positionieren des Windows im Zentrum des Displays
//      Dimension screenSize = getToolkit().getScreenSize();
//      setLocation ((screenSize.width  - getSize().width )/2,
//                   (screenSize.height - getSize().height)/2);
//      requestFocus();
//    }
//    super.setVisible(visible);
//  }

  /**
   * Implements WindowListener::windowClosing
   */
  public void windowClosing (WindowEvent e)
  {
    closeMe();
  }

  // Other WindowListener methods:
  public void windowOpened      (WindowEvent e) {;}
  public void windowClosed      (WindowEvent e) {;}
  public void windowIconified   (WindowEvent e) {;}
  public void windowDeiconified (WindowEvent e) {;}
  public void windowActivated   (WindowEvent e) {;}
  public void windowDeactivated (WindowEvent e) {;}

  /**
   * Implements ActionListener::actionPerformed
   */
  public void actionPerformed (ActionEvent e)
  {
    Object item = e.getSource();

    if (item == okButton)
    {
      closeMe();
    }
  }

  //-----------------------------------------
  // Implements KeyListener::keyPressed method
  //-----------------------------------------
  public void keyPressed (KeyEvent e)
  {
    int key  = e.getKeyCode();

    switch (key)
    {
      case ' ':
      case '\n':
      case 27:
        closeMe();
    }
  }

  //----------------------------------------
  // Unused KeyListener methods
  //----------------------------------------
  public void keyTyped  (KeyEvent e) {;}
  public void keyReleased (KeyEvent e) {;}

  private void closeMe()
  {
    setVisible(false);
    dispose();

    // remove ourselves from the focal list

    if (nextFocalReceiver != null)
      nextFocalReceiver.prevFocalReceiver = prevFocalReceiver;

    if (prevFocalReceiver == null)
      focalList = nextFocalReceiver;
    else
      prevFocalReceiver.nextFocalReceiver = nextFocalReceiver;

    if (focalList != null)
      focalList.requestFocus();
    else if (focusReceiver != null)
      focusReceiver.requestFocus();
  }
}
