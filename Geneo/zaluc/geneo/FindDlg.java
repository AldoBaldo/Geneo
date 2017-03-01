//+-- File FindDlg.java ------------------------------------------------------+
//|                                                                           |
//| Description:  This file contains the code necessary to implement a dialog |
//|               box for finding individuals in a tree.                      |
//|                                                                           |
//| Classes:      class InfoDlg extends Dialog                                |
//|                                                                           |
//|---------------------------------------------------------------------------+

package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


//+-- Class FindDlg ----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class FindDlg extends Dialog                                |
//|                                                                           |
//| Description:  FindDlg puts up a dialog box that can be used to find a     |
//|               person in the people list.                                  |
//|                                                                           |
//| Methods:      public         FindDlg   (Frame         parent,             |
//|                                         AppletContext context,            |
//|                                         PeopleList    people)             |
//|                                                                           |
//|---------------------------------------------------------------------------+

class FindDlg extends    Dialog
              implements WindowListener,
                         ActionListener,
                         ItemListener,
                         KeyListener
{
  Button okButton;
  Button applyButton;
  Button cancelButton;

  Frame         parent;
  TreeCanvas    canvas;
  Person        ourPeople[];
  int           ourPeopleCount;

  FindList       list;
  MultiLineLabel details;

  int            maxItemLen = 0;

  public FindDlg (Frame         inParent,
                  AppletContext inContext,
                  String        inHtmlTarget,
                  TreeCanvas    inCanvas,
                  PeopleList    inPeople)
  {
    super(inParent, "Find Person", true);

    parent  = inParent;
    canvas  = inCanvas;

    Person      person;
    int         centerPersonIndex = inPeople.getCenterPerson().index;
    int         ourCenterPersonIndex = 0;
    Person      centerPerson;
    String      listItem;
    FontMetrics fm = getFontMetrics(getFont());
    int         allCount = inPeople.getCount();  // This count includes hidden people

    ourPeople = new Person[allCount];
    ourPeopleCount = 0;              // This count is for only non-hidden people.

    setLayout(new BorderLayout(5,5));

    list = new FindList(Math.min(20, allCount));
    list.addItemListener (this);

    for (int i = 0; i < allCount; i++)
    {
      if (((person = inPeople.getPerson(i)) != null) &&
          !person.isBlank)
      {
        // Add their name to the list
        //listItem = ((person.lastName  != null) ? person.lastName  : "???") + ", " +
        //            ((person.firstName != null) ? person.firstName : "???") + " (" +
        //            person.lifeDates + ")";
        listItem = null;
        if (person.lastName != null)
        {
          listItem = person.lastName;
          if (person.firstName != null)
            listItem += ", " + person.firstName;
          if (person.lifeDates != null)
            listItem += " (" + person.lifeDates + ")";
        }
        else if (person.firstName != null)
        {
          listItem = person.firstName;
          if (person.lifeDates != null)
            listItem += " (" + person.lifeDates + ")";
        }
        else
        {
          // Just on the off-chance we have a person with life dates
          // but no name.
          listItem = "(" + person.lifeDates + ")";
        }
        list.add (listItem);
        maxItemLen = Math.max(maxItemLen, fm.stringWidth(listItem));

        // Start with the current center person selected
        if (i == centerPersonIndex)
        {
          centerPerson = person;
          ourCenterPersonIndex = ourPeopleCount;
        }

        // Add to our list of non-hidden people
        ourPeople[ourPeopleCount] = person;
        ourPeopleCount++;
      }
    }
    list.setWidth(maxItemLen + 20);  // The +20 is a kludge to accomodate the v-scroll bar
    list.makeVisible(ourCenterPersonIndex);
    list.select     (ourCenterPersonIndex);

    centerPerson = ourPeople[ourCenterPersonIndex];
    details = new MultiLineLabel(centerPerson.details, inContext, inHtmlTarget, 20, 20, MultiLineLabel.LEFT);

    add("West", list);
    add("Center", details);

    Panel p = new Panel();
    p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    p.add(okButton     = new Button("OK"));
    p.add(applyButton  = new Button("Apply"));
    p.add(cancelButton = new Button("Cancel"));
    add("South", p);

    //----------------------------------------------
    // Add all of our Listeners
    //----------------------------------------------
    addWindowListener (this);
    addKeyListener    (this);
    list        .addActionListener (this);
    list        .addKeyListener    (this);
    okButton    .addActionListener (this);
    okButton    .addKeyListener    (this);
    applyButton .addActionListener (this);
    applyButton .addKeyListener    (this);
    cancelButton.addActionListener (this);
    cancelButton.addKeyListener    (this);

    //----------------------------------------------
    // Place the window in the center of the screen
    //----------------------------------------------
    Dimension scrnSize = getToolkit().getScreenSize();
    Dimension winSize;
    pack();
    winSize = getSize();
    winSize.width = 640;
    setSize (winSize);
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
//    pack();
//  }

  // Thanks to Steffen Krug <pdgsvoeu@ford.com> for this code (and the german? comment)
//  public void setVisible(boolean visible)
//  {
//    Dimension listDim;
//    Person    curCenterPerson;
//
//    System.out.println("In setVisible, visible = " + visible);
//
//    if (visible)
//    {
//      pack();
//
//      //Positionieren des Windows im Zentrum des Displays
//      Dimension screenSize = getToolkit().getScreenSize();
//      setLocation ((screenSize.width  - getSize().width )/2,
//                   (screenSize.height - getSize().height)/2);
//
//      System.out.println("In setVisible, screenSize == ("
//                         + screenSize.width + "," + screenSize.height
//                         + "), getSize == ("
//                         + getSize().width + "," + getSize().height
//                         + ")");
//    }
//
//    super.setVisible (visible);
//  }

  /**
   * Implements WindowListener::windowClosing
   */
  public void windowClosing (WindowEvent e)
  {
    close();
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

    if ((item == okButton) ||
        (item == list))       // Item was double clicked on in list
    {
      apply();
      close();
    }
    else if (item == applyButton)
    {
      apply();
    }
    else if (item == cancelButton)
    {
      close();
    }
  }

  /**
   * Implements ItemListener::itemStateChanged
   */
  public void itemStateChanged (ItemEvent event)
  {
    int    curSelIndex  = list.getSelectedIndex();

    if ((curSelIndex >= 0) &&
        (curSelIndex < ourPeopleCount))
    {
      Person curSelPerson = ourPeople[curSelIndex];

      details.setLabel(curSelPerson.details);

      //Dimension oldDim;
      //Dimension newDim;
      //int       diffWidth;
      //Rectangle rect;

      //// Calculate the change in size of the details area:

      //oldDim = details.getPreferredSize();
      //details.setLabel(curSelPerson.details);
      //newDim = details.getPreferredSize();

      //diffWidth = newDim.width - oldDim.width;

      //// Change the size and position of the dialog box to accomodate the
      //// new details.
      
      //rect = getBounds();
      //rect.width += diffWidth;
      //rect.x -= diffWidth / 2;
      //setBounds (rect);
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
        apply();
        break;
      case '\n':
        apply();
        // fall through to the escape key case
      case 27:
        close();
        break;
    }
  }

  //----------------------------------------
  // Unused KeyListener methods
  //----------------------------------------
  public void keyTyped  (KeyEvent e) {;}
  public void keyReleased (KeyEvent e) {;}

  private void apply()
  {
    int selected = list.getSelectedIndex();
    if ((selected >= 0) &&
        (selected < ourPeopleCount))
    {
      canvas.setCenterPerson (ourPeople[selected]);
    }
  }

  private void close()
  {
    setVisible(false);
    dispose();
    parent.requestFocus();
//    canvas.requestFocus();
  }
}

//+-- Class FindList ---------------------------------------------------------+
//|                                                                           |
//| Syntax:       class FindList extends Choice                               |
//|                                                                           |
//| Description:  FindList defines a Choice box with a few added methods      |
//|               to taylor it to holding a list of people.  This class       |
//|               probably isn't necessary anymore.  At one time it was going |
//|               sort the list of people as they are added, but I decided to |
//|               move that functionality into the parser, so it is no        |
//|               longer needed here.                                         |
//|                                                                           |
//| Methods:      public void   addPeople   (PeopleList inPeople)             |
//|                                                                           |
//|               public Person getSelected ()                                |
//|                                                                           |
//|---------------------------------------------------------------------------+

class FindList extends List
{
  FindDlg owner;
  int     width = 0;

  public FindList(int visibleCount)
  {
    super(visibleCount);
  }

//  public boolean keyDown (Event e,
//                          int   key)
//  {
//    return super.keyDown (e, key);
//  }

  public void setWidth(int inWidth)
  {
    width = inWidth;
  }

  public Dimension getMinimumSize()
  {
    Dimension dim = super.getMinimumSize();
    dim.width = Math.max(dim.width, width);
    return dim;
  }

  public Dimension getMinimumSize(int rows)
  {
    Dimension dim = super.getMinimumSize(rows);
    dim.width = Math.max(dim.width, width);
    return dim;
  }

  public Dimension getPreferredSize()
  {
    Dimension dim = super.getPreferredSize();
    dim.width = Math.max(dim.width, width);
    return dim;
  }

  public Dimension getPreferredSize(int rows)
  {
    Dimension dim = super.getPreferredSize(rows);
    dim.width = Math.max(dim.width, width);
    return dim;
  }

//  public void select(int index)
//  {
//    // Selecting the item seems to make it appears one above the
//    // top item visible in the list box.  And it seems to make
//    // makeVisible a no-op.  However, I have left it in in the
//    // belief that this is a bug in the JVM and will be fixed
//    // in later version.
//    super.select(index);
//  }

//  public void makeVisible(int index)
//  {
//    super.makeVisible(index);
//  }
}
