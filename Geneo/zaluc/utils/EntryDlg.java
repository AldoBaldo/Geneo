package zaluc.utils;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class EntryDlg extends    Dialog
                      implements WindowListener,
                                 ActionListener,
                                 KeyListener
{
  private TextField textField;
  private Button    okButton;
  private Button    cancelButton;
  private Button    helpButton;
  private Frame     parent;
  private Callback  caller = null;
  private int       taskId;

  public EntryDlg (Frame    parent,
                   String   title,
                   String   label,
                   boolean  secret,
                   String   helpUrl,
                   Callback caller,
                   int      taskId)
  {
    super(parent, title, true);

    this.parent = parent;
    this.caller = caller;
    this.taskId = taskId;

    setLayout(new BorderLayout(5,5));

//    Panel p1 = new Panel();
//    p1.setLayout (new GridLayout(2,1));
//    p1.add (new Label(label));
//    p1.add (textField = new TextField());
//    if (secret)
//      textField.setEchoCharacter ('*');
//    add("Center", p1);

    add ("North",  new Label(label));
    add ("Center", textField = new TextField());
    if (secret)
      textField.setEchoChar ('*');

    Panel p2 = new Panel();
    p2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    p2.add(okButton     = new Button("OK"));
    p2.add(cancelButton = new Button("Cancel"));
//    p2.add(helpButton   = new Button("Help"));
    add("South", p2);

    //----------------------------------------------
    // Add all of our Listeners
    //----------------------------------------------
    addWindowListener (this);
    addKeyListener    (this);
    textField   .addKeyListener    (this);
    okButton    .addActionListener (this);
    okButton    .addKeyListener    (this);
    cancelButton.addActionListener (this);
    cancelButton.addKeyListener    (this);

    //----------------------------------------------
    // Place the window in the center of the screen
    //----------------------------------------------
    Dimension scrnSize = getToolkit().getScreenSize();
    pack();
    setLocation ((scrnSize.width  - getSize().width)  / 2,
                 (scrnSize.height - getSize().height) / 2);
  }

  // This method is invoked after our dialog is first created
  // but before it can actually be displayed.  After we've
  // invoked our superclass's addNotify() method, we have font
  // metrics and can successfully call measure() to figure out
  // how big the label is.

//  public void addNotify()
//  {
//    super.addNotify();
////    textField.requestFocus();
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
//      textField.requestFocus();
//    }
//    super.setVisible (visible);
//  }

  /**
   * Implements WindowListener::windowClosing
   */
  public void windowClosing (WindowEvent e)
  {
    close(1);
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
      close(0);
    }
    else if (item == cancelButton)
    {
      close(1);
    }
  }

  //-----------------------------------------
  // Implements KeyListener::keyPressed method
  //-----------------------------------------
  public void keyPressed (KeyEvent e)
  {
    int key = e.getKeyCode();

    switch (key)
    {
      case '\n':
        close(0);
        break;
      case 27:
        close(1);
        break;
    }
  }

  //----------------------------------------
  // Unused KeyListener methods
  //----------------------------------------
  public void keyTyped  (KeyEvent e) {;}
  public void keyReleased (KeyEvent e) {;}

  private void close(int result)
  {
    setVisible(false);
    dispose();
    if (caller != null)
    {
      if (result == 0)
      {
        // The user pressed OK so we need to return what they typed in.
        caller.callback(taskId, result, textField.getText(), null);
      }
      else
      {
        // The user cancelled, return null
        caller.callback(taskId, result, null, null);
      }
    }
    if (parent != null)
    {
      parent.requestFocus();
    }
  }
}
