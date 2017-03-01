package zaluc.geneo;

import java.awt.*;
import java.lang.*;
import java.util.*;

//+-- Class PeopleList -------------------------------------------------------+
//|                                                                           |
//| Syntax:       class PeopleList                                            |
//|                                                                           |
//| Description:  The PeopleList class is responsible for managing a list of  |
//|               People objects that are linked together to form a tree.     |
//|               It is also responsible for performing actions on that tree  |
//|               like calculating the way the tree is displayed on the       |
//|               screen and drawing the tree.                                |
//|                                                                           |
//| Methods:      public         PeopleList      (int         inCount)        |
//|                                                                           |
//|               public  void   setScreenSize   (int         height,         |
//|                                               int         width)          |
//|                                                                           |
//|               public  void   setCenterPerson (Person      center)         |
//|                                                                           |
//|               public  Person getCenterPerson ()                           |
//|                                                                           |
//|               public  void   setFontMetrics  (FontMetrics metrics)        |
//|                                                                           |
//|               public  void   calcTree        ()                           |
//|                                                                           |
//|               public  int    getVertMaxPos   ()                           |
//|                                                                           |
//|               public  int    getVertMinPos   ()                           |
//|                                                                           |
//|               public  int    getVertCurPos   ()                           |
//|                                                                           |
//|               public  int    getHorzMaxPos   ()                           |
//|                                                                           |
//|               public  int    getHorzMinPos   ()                           |
//|                                                                           |
//|               public  int    getHorzCurPos   ()                           |
//|                                                                           |
//|               public  void   drawTree        (Graphics    g)              |
//|                                                                           |
//|               public  void   pageLeft        ()                           |
//|                                                                           |
//|               public  void   pageRight       ()                           |
//|                                                                           |
//|               public  void   pageUp          ()                           |
//|                                                                           |
//|               public  void   pageDown        ()                           |
//|                                                                           |
//|               public  void   incLeft         ()                           |
//|                                                                           |
//|               public  void   incRight        ()                           |
//|                                                                           |
//|               public  void   incUp           ()                           |
//|                                                                           |
//|               public  void   incDown         ()                           |
//|                                                                           |
//|               public  void   calcShifts      ()                           |
//|                                                                           |
//|               public  Person getPersonUnderPoint (int     x,              |
//|                                                   int     y)              |
//|                                                                           |
//|               private void calcYPositions    ()                           |
//|                                                                           |
//|---------------------------------------------------------------------------+

class PeopleList
{
  private Globals globals;
  private int     peopleCount;
  private Vector  peopleList;
  private int     familyCount;
  private Family  familyList[];

  private class Position
  {
    public Position next = null;
    public Position prev = null;

    public Person centerPerson = null;

    // The CurPos represents the position of the upper left corner of the
    // screen relative to position the center position, which is a point
    // in the middle of the space occupied by the center person and his/her
    // spouses.
    public int    vertCurPos   = 0;
    public int    horzCurPos   = 0;

    Position (Person newCenterPerson)
    {
      centerPerson = newCenterPerson;
    }
  }

  public boolean debug = false;

  Position      curPos      = null;
  DrawingObject centerBox   = null;
  DrawingObject selectedBox = null;
  Font          nameFont    = null;
  Font          dateFont    = null;
  FontMetrics   nameFontMetrics = null;
  FontMetrics   dateFontMetrics = null;

  int vertSpace;
  int horzSpace;
  int vertSpouseSpace;
  int vertLineOffset;
  int screenHeight;
  int screenWidth;
  int fontHeight;
  int fontWidth;
  int trunkLen;
  int branchLen;
  int vertPageAmt;
  int horzPageAmt;
  int vertIncAmt;
  int horzIncAmt;

  // The shift values are the amounts that must be added to all calculated
  // coordinates to draw them in the correct place.  These values will change
  // during scrolling.
  int shiftX = 0;
  int shiftY = 0;

  // These values represent borders of the tree area relative to the center
  // point.  The center point is a point in the middle of the space used by
  // the center person and his/her spouses.
  // Note that verMaxPos represents the bottom of the tree area, and
  // vertMinPos, the top.
  // Also note that, whereas the horz coordinates represent the entire
  // horizontal space of the tree, the vert coordinates only represent the
  // top and bottom of the vertical slice of the tree that paging up and down
  // can reach.  Specifically, people that are to the left or right of the
  // current screen space are not included in this vertical region.
  int vertMaxPos;
  int vertMinPos;
  int horzMaxPos;
  int horzMinPos;

  private boolean showHidden = false;

  Generation    childGenerationList;
  Generation    parentGenerationList;
  Generation    leftMostVisibleGeneration;
  Generation    rightMostVisibleGeneration;
  DrawingObject visibleList;

  public PeopleList (Globals globals,
                     int     peopleCount,
                     int     familyCount)
  {
    this.globals     = globals;
    this.peopleCount = peopleCount;
    this.familyCount = familyCount;
    peopleList       = new Vector(peopleCount, 50);
    familyList       = new Family[familyCount];
  }

  //+--------------------------------------------------------------------+
  //| Routines for adding and getting people to and from the peopleList  |
  //+--------------------------------------------------------------------+

  public synchronized Person newPerson(int index)
  {
    Person person = null;
    
    if (index < peopleCount)
    {
      person = new Person(globals, this, index);
      if (person != null)
      {
        try
        {
          if (index >= peopleList.size())
            peopleList.setSize(index+1);
          peopleList.setElementAt (person, index);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
          System.out.println("PeopleList#newPerson, ArrayIndexOutOfBoundsException: " + e.getMessage());
          e.printStackTrace();
          person = null;
        }
      }
    }

    return person;
  }

  public synchronized int appendPerson()
  {
    Person person = new Person(globals, this, peopleCount);

    if (person != null)
    {
      person.isBlank = true;
      person.complete();
      peopleList.setSize(peopleCount+1);
      peopleList.setElementAt(person, peopleCount);
      return peopleCount++;
    }

    return -1;
  }

  public synchronized Family newFamily(int index)
  {
    return (familyList[index] = new Family(globals, this, index));
  }

  public synchronized Person getPerson(int index)
  {
    Person person = null;

    if ((index >= 0)            &&
        (index <  peopleCount)  &&
        (index <  peopleList.size()))
    {
      person = (Person) peopleList.elementAt(index);

      if ((person != null) &&
          (!person.isComplete ||
           (!showHidden && person.hidden)))
      {
        person = null;
      }
    }

    return person;
  }


  public synchronized Person getPersonFromId(int id)
  {
    Person person = null;
    int    i;

    for (i=0; i<peopleCount; i++)
    {
      person = (Person) peopleList.elementAt(i);

      if (person != null)
      {
        if (person.id == id)
        {
          if (!person.isComplete ||
              (!showHidden && person.hidden))
          {
            person = null;
          }
          break;  // We've found the person, don't look any further
        } // if person.id == id
        else
        {
          person = null;
        }
      } // if person != null
    } // for each person

    // this will be null if the person doesn't exist or is hidden.
    return person;

  } // getPersonFromId


  public synchronized Family getFamily(int index)
  {
    if ((index >= 0) &&
        (index <  familyCount) &&
        (familyList[index] != null) &&
        familyList[index].isComplete)
      return familyList[index];
    else
      return null;
  }

  public int getCount()
  {
    return peopleCount;
  }

  //+--------------------------------------------------------------+
  //| Routines for setting and getting tree characterstics and for |
  //| drawing the tree                                             |
  //+--------------------------------------------------------------+

  public void setScreenSize (int height,
                             int width)
  {
    if (debug)
      System.out.println("PeopleList::setScreenSize (" + height + "," + width + ")"); //zombie
    screenHeight = height;
    screenWidth  = width;
    calcShifts();
  }

  public synchronized void setCenterPerson (Person center)
  {
    if (center != null)
    {
      if (curPos == null)
      {
        curPos = new Position (center);
      }
      else if (curPos.centerPerson != center)
      {
        curPos.next = new Position (center);
        curPos.next.prev = curPos;
        curPos = curPos.next;
      }

      // Re-center the center person
      curPos.vertCurPos = - (screenHeight / 2);
      curPos.horzCurPos = - (screenWidth  / 2);

      if (debug)
      {
        System.out.println("Peoplelist::setCenterPerson - screenHeight = " + screenHeight + ", screenWidth = " + screenWidth); //zombie
        System.out.println("Peoplelist::setCenterPerson - setting vertCurPos to " + curPos.vertCurPos + ", horzCurPos to " + curPos.horzCurPos); //zombie
      }

      // Center the screen on the new center person
      calcShifts();

      // Build the tree of drawing objects
      createTree();
    }
  }

  public Person getCenterPerson ()
  {
    if (curPos != null)
    {
      return curPos.centerPerson;
    }
    else
    {
      return null;
    }
  }

  public synchronized void setSelectedBox (Graphics g, DrawingObject selected)
  {
    if (selectedBox != null)
      selectedBox.deselect(g);
    selectedBox = selected;
    if (selectedBox != null)
      selectedBox.select(g);
  }

  public Person getSelectedPerson ()
  {
    return selectedBox.person;
  }

  public void setFonts (Font        nameFont, Font        dateFont,
                        FontMetrics nameFM  , FontMetrics dateFM  )
  {
    this.nameFont = nameFont;
    this.dateFont = dateFont;
    this.nameFontMetrics = nameFM;
    this.dateFontMetrics = dateFM;

    if (centerBox != null)
    {
      centerBox.calcChildDimensions();
      if (centerBox.father != null)
        centerBox.father.calcParentDimensions();
      if (centerBox.mother != null)
        centerBox.mother.calcParentDimensions();
    }
  }

  public void showHidden()
  {
    showHidden = true;
  }

  //+- method shouldDraw -------------------------------------------------+
  //|                                                                     |
  //|  Syntax:                                                            |
  //|                                                                     |
  //|    public boolean shouldDraw()                                      |
  //|                                                                     |
  //|  Description:                                                       |
  //|                                                                     |
  //|    shouldDraw should be called during parsing of the data file to   |
  //|    see if any new people have been added that would change the      |
  //|    shape of the currently displayed tree. It checks all the         |
  //|    descendants and ancestors of the center person to see if any of  |
  //|    them have not yet been drawn.  If it finds any that have not     |
  //|    yet been drawn, it returns true.  Otherwise it returns false.    |
  //|                                                                     |
  //|  Parameters:                                                        |
  //|                                                                     |
  //|    none.                                                            |
  //|                                                                     |
  //|  Return Value:                                                      |
  //|                                                                     |
  //|    Returns true if the tree should be redrawn, false if not.        |
  //|                                                                     |
  //+---------------------------------------------------------------------+

  public boolean shouldDraw()
  {
    boolean ok;

    if ((curPos != null) &&
        (curPos.centerPerson != null))
    {
      ok = curPos.centerPerson.shouldDrawChildren() ||
           curPos.centerPerson.shouldDrawParents();
    }
    else
    {
      // If the centerPerson is still null, then nobody has been drawn yet,
      // so shouldDraw should return true.
      ok = true;
    }

    return ok;
  }

  //+---------------------------------------------------------------------+
  //| calcTree should be called after all parameters are set and before   |
  //| drawing the tree                                                    |
  //+---------------------------------------------------------------------+

  public synchronized void calcTree ()
  {
    Generation  curGeneration;
    boolean     isFirstGen;
    FontMetrics fontMetrics;

    // Reset the dimensions of the tree
    horzMaxPos = 0;
    horzMinPos = 0;
    vertMaxPos = 0;
    vertMinPos = 0;

    if (debug)
      System.out.println("PeopleList::calcTree - shiftX = " + shiftX + ", shiftY = " + shiftY); //zombie

    if ((centerBox        != null) &&
        (dateFontMetrics  != null))
    {
      // Calculate some common values.

      fontHeight      = dateFontMetrics.getHeight();
      fontWidth       = dateFontMetrics.charWidth('F');  // average width char chosen at random
      trunkLen        = 2 * fontWidth;
      branchLen       = 2 * fontWidth;
      vertSpace       = 2 * fontHeight;
      vertSpouseSpace = fontHeight;
      vertLineOffset  = fontWidth;
      horzSpace       = trunkLen + branchLen;
      vertIncAmt      = 2 * vertSpace;
      vertPageAmt     = screenHeight - vertIncAmt;
      horzIncAmt      = 4 * branchLen;
      horzPageAmt     = screenWidth  - horzIncAmt;

      // For each generation, calculate the horizontal dimensions of the generation,
      // then expand the treeDimensions to include the dimensions of that generation.
      // This pass will tell us which generations should be included in the tree.
      isFirstGen = true;
      for (curGeneration = childGenerationList; curGeneration != null; curGeneration = curGeneration.nextGeneration)
      {
        horzMinPos = curGeneration.calcChildDim(isFirstGen, false, horzMinPos);
        isFirstGen = false;
      }
      horzMaxPos = childGenerationList.leftX + childGenerationList.dim.width;
      for (curGeneration = parentGenerationList; curGeneration != null; curGeneration = curGeneration.nextGeneration)
      {
        horzMaxPos = curGeneration.calcParentDim(horzMaxPos);
      }

      // For those generations that should be in the tree, calculate the vertical
      // position of each individual in that generation.

      calcYPositions();

      // For each generation, calculate the horizontal dimensions of the generation,
      // then expand the treeDimensions to include the dimensions of that generation
      // This will also calculate the offsets for the vertical portions of the lines
      // between parents and their children so that they don't overlap with those of
      // other parents.
      //
      // This is exactly the same code as what we did above.  The difference is that
      // now we know the vertical positions of the children, and this will allow us
      // to determine if any lines from a spouse to their children overlap with those
      // of another spouse.
      //
      // Basically, we have a chicken and egg thing here.  We have to calculate the
      // horizontal spacing before we calculate the vertical spacing so that we know
      // which generations are visible on the screen.  However, we have to know the
      // vertical spacing of the children before we'll know if any spouse-to-child
      // lines will overlap.  If they do, we'll have to add space between the
      // generations, thus changing the horizontal positions of the generations.
      //
      // Note that this later shifting of the horizontal positions of the generations
      // could cause some generations that were marked as visible to shift off of the
      // left edge of the screen.  This could mean that children would be further apart
      // vertically than they need to be.  However, this should be a rare occurance,
      // and the consequences are not that bad, so we end the calculation here.
      // Otherwise, it would be a recursive problem.

      isFirstGen = true;
      // Reset the dimensions of the tree
      horzMaxPos = 0;
      horzMinPos = 0;
      for (curGeneration = childGenerationList; curGeneration != null; curGeneration = curGeneration.nextGeneration)
      {
        horzMinPos = curGeneration.calcChildDim(isFirstGen, true, horzMinPos);
        isFirstGen = false;
      }
      horzMaxPos = childGenerationList.leftX + childGenerationList.dim.width;
      for (curGeneration = parentGenerationList; curGeneration != null; curGeneration = curGeneration.nextGeneration)
      {
        horzMaxPos = curGeneration.calcParentDim(horzMaxPos);
      }

    // Debug the generation lists
//    Generation curGen;
//    int        i = 0;
//    System.out.println("calcYPositions: Child generations:");
//    for (curGen = childGenerationList; curGen != null; curGen = curGen.nextGeneration)
//    {
//      System.out.println("                generation " + i++ + ":" +
//                       "\n                   width = " + curGen.dim.width +
//                       "\n                   leftX = " + curGen.leftX);
//    }
//    i = 0;
//    System.out.println("calcYPositions: Parent generations:");
//    for (curGen = parentGenerationList; curGen != null; curGen = curGen.nextGeneration)
//    {
//      System.out.println("                generation " + i++ + ":" +
//                       "\n                   width = " + curGen.dim.width +
//                       "\n                   leftX = " + curGen.leftX);
//    }
    }
  }

  public int getVertMaxPos()
  {
    return vertMaxPos;
  }

  public int getVertMinPos()
  {
    return vertMinPos;
  }

  public int getVertCurPos()
  {
    if (curPos != null)
      return curPos.vertCurPos;
    else
      return 0;
  }

  public int getHorzMaxPos()
  {
    return horzMaxPos;
  }

  public int getHorzMinPos()
  {
    return horzMinPos;
  }

  public int getHorzCurPos()
  {
    if (curPos != null)
      return curPos.horzCurPos;
    else
      return 0;
  }

  /**
   * Draws the people tree
   */
  public void drawTree (Graphics g)
  {
    visibleList = null;   // erase old visible list

    if (debug)
      System.out.println("PeopleList::drawTree - shiftX = " + shiftX + ", shiftY = " + shiftY); //zombie

    if (centerBox != null)
    {
      //zombie-start
      if (debug)
      {
        g.setColor (Color.red);
        g.drawLine (horzMinPos+shiftX  , vertMinPos+shiftY  , horzMaxPos+shiftX-1, vertMinPos+shiftY  );
        g.drawLine (horzMinPos+shiftX  , vertMinPos+shiftY  , horzMinPos+shiftX  , vertMaxPos+shiftY-1);
        g.drawLine (horzMaxPos+shiftX-1, vertMaxPos+shiftY-1, horzMaxPos+shiftX-1, vertMinPos+shiftY  );
        g.drawLine (horzMaxPos+shiftX-1, vertMaxPos+shiftY-1, horzMinPos+shiftX  , vertMaxPos+shiftY-1);
      }
      //zombie-end
      centerBox.drawChildren (g);
      centerBox.drawParents  (g);
    }
  }

  public boolean back()
  {
    if ((curPos != null) &&
        (curPos.prev != null))
    {
      curPos = curPos.prev;
      calcShifts();
      createTree();
      return true;
    }
    else
    {
      return false;
    }
  }

  public boolean forward()
  {
    if ((curPos != null) &&
        (curPos.next != null))
    {
      curPos = curPos.next;
      calcShifts();
      createTree();
      return true;
    }
    else
      return false;
  }

  public boolean pageLeft()
  {
    // Paging to the left means to take the left most completely visible
    // generation and make it the right most completely visible generation.
    int pageAmount;

    if ((leftMostVisibleGeneration != null) &&
        (curPos != null))
    {
      pageAmount  = screenWidth - leftMostVisibleGeneration.dim.width;
      return setHorz (curPos.horzCurPos - pageAmount);
    }
    return false;
  }

  public boolean pageRight()
  {
    // Paging to the right means to take the right most completely visible
    // generation and make it the left most completely visible generation.
    int pageAmount;

    if ((rightMostVisibleGeneration != null) &&
        (curPos != null))
    {
      pageAmount  = screenWidth - rightMostVisibleGeneration.dim.width;
      return setHorz (curPos.horzCurPos + pageAmount);
    }
    return false;
  }

  public boolean pageUp()
  {
    if (curPos != null)
    {
      return setVert (curPos.vertCurPos - vertPageAmt);
    }
    return false;
  }

  public boolean pageDown()
  {
    if (curPos != null)
    {
      return setVert (curPos.vertCurPos + vertPageAmt);
    }
    return false;
  }

  public boolean incLeft()
  {
    int pageAmount;

    if ((leftMostVisibleGeneration != null) &&
        (curPos != null))
    {
      pageAmount  = leftMostVisibleGeneration.dim.width;
      return setHorz (curPos.horzCurPos - pageAmount);
    }
    return false;
  }

  public boolean incRight()
  {
    int pageAmount;

    if ((rightMostVisibleGeneration != null) &&
        (curPos != null))
    {
      pageAmount  = rightMostVisibleGeneration.dim.width;
      return setHorz (curPos.horzCurPos + pageAmount);
    }
    return false;
  }

  public boolean incUp()
  {
    if (curPos != null)
    {
      return setVert (curPos.vertCurPos - vertIncAmt);
    }
    return false;
  }

  public boolean incDown()
  {
    if (curPos != null)
    {
      return setVert (curPos.vertCurPos + vertIncAmt);
    }
    return false;
  }

  public boolean setVert(int pos)
  {
    if (debug)
    {
      System.out.println("PeopleList::setVert(" + pos + ") - screenHeight = " + screenHeight); //zombie
      System.out.println("                                   vertMinPos   = " + vertMinPos  ); //zombie
      System.out.println("                                   vertMaxPos   = " + vertMaxPos  ); //zombie
    }

    if (curPos != null)
    {
      // When the tree is taller than the screen, we make sure that the
      // screen doesn't go beyond the edges of the tree.  When the screen
      // is taller than the tree, we make sure that the tree doesn't go
      // beyond the edges of the screen.
      //
      // vertMinPos         - the top edge of the tree
      // vertMaxPos         - the bottom edge of the tree
      // pos                - the top edge of the screen
      // pos + screenHeight - the bottom edge of the screen

      if ((vertMaxPos - vertMinPos) > screenHeight)
      {
        // The tree is taller than the screen.

        if (pos < vertMinPos)
        {
          // We tried to move past the top of the tree.
          // Rest the tree against the top of the screen.
          pos = vertMinPos;
        }
        else if ((pos + screenHeight) > vertMaxPos)
        {
          // We tried to move past the right edge of the tree.
          // Rest the tree agains the right edge of the screen.
          pos = vertMaxPos - screenHeight;
        }
      }
      else
      {
        // The screen is as tall or taller than the tree.

        if (vertMinPos < pos)
        {
          // We tried to move the tree past the top of the
          // screen.  Let's adjust the tree to the top of
          // the screen.
          pos = vertMinPos;
        }
        else if (vertMaxPos > (pos + screenHeight))
        {
          // We tried to move the tree past the bottom of the
          // screen.  Let's adjust the tree to the bottom of
          // the screen.
          pos = vertMaxPos - screenHeight;
        }
      }

      if (pos != curPos.vertCurPos)
      {
        shiftY            = - pos;
        curPos.vertCurPos = pos;
        if (debug)
          System.out.println("PeopleList::setVert - setting vertCurPos to " + curPos.vertCurPos);
        return true;
      }
    }
    return false;
  }

  public boolean setHorz(int pos)
  {
    if (debug)
    {
      System.out.println("PeopleList::setHorz(" + pos + ") - screenWidth = " + screenWidth); //zombie
      System.out.println("                                   horzMinPos  = " + horzMinPos ); //zombie
      System.out.println("                                   horzMaxPos  = " + horzMaxPos ); //zombie
    }

    if (curPos != null)
    {
      // When the tree is wider than the screen, we make sure that the
      // screen doesn't go beyond the edges of the tree.  When the screen
      // is wider than the tree, we make sure that the tree doesn't go
      // beyond the edges of the screen.
      //
      // horzMinPos        - the left edge of the tree
      // horzMaxPos        - the right edge of the tree
      // pos               - the left edge of the screen
      // pos + screenWidth - the right edge of the screen

      if ((horzMaxPos - horzMinPos) > screenWidth)
      {
        // The tree is wider than the screen.

        if (pos < horzMinPos)
        {
          // We tried to move past the left edge of the tree.
          // Rest the tree against the left edge of the screen.
          pos = horzMinPos;
        }
        else if ((pos + screenWidth) > horzMaxPos)
        {
          // We tried to move past the right edge of the tree.
          // Rest the tree agains the right edge of the screen.
          pos = horzMaxPos - screenWidth;
        }
      }
      else
      {
        // The screen is as wide or wider than the tree.

        if (horzMinPos < pos)
        {
          // We tried to move the tree past the left edge of the
          // screen.  Let's adjust the tree to the left edge of
          // the screen.
          pos = horzMinPos;
        }
        else if (horzMaxPos > (pos + screenWidth))
        {
          // We tried to move the tree past the right edge of the
          // screen.  Let's adjust the tree to the right edge of
          // the screen.
          pos = horzMaxPos - screenWidth;
        }
      }

      if (pos != curPos.horzCurPos)
      {
        shiftX            = - pos;
        curPos.horzCurPos = pos;
        if (debug)
          System.out.println("PeopleList::setHorz - setting horzCurPos to " + curPos.vertCurPos);
        calcTree();
        // Call setVert just in case the vertical dimensions of the
        // tree changed and the old vertCurPos is no longer valid.
        setVert (curPos.vertCurPos);
        return true;
      }
    }
    return false;
  }

  public boolean home()
  {
    if (curPos != null)
    {
      System.out.println("PeopleList::home");
      curPos.vertCurPos = -(screenHeight / 2);
      curPos.horzCurPos = -(screenWidth  / 2);
      calcShifts();
      calcTree();
      return true;
    }
    return false;
  }

  public DrawingObject getBoxUnderPoint (int x,
                                         int y)
  {
    DrawingObject box;

    for (box = visibleList; box != null; box = box.nextVisible)
    {
      if (box.isUnderPoint(x - shiftX, y - shiftY))
        return box;
    }
    return null;
  }

  public Person getPersonUnderPoint (int x,
                                     int y)
  {
    DrawingObject box;

    box = getBoxUnderPoint (x, y);

    if (box != null)
      return box.person;

    return null;
//
//    for (box = visibleList; box != null; box = box.nextVisible)
//    {
//      if (box.isUnderPoint(x - shiftX, y - shiftY))
//        return box.person;
//    }
//    return null;
  }

  //+------------------+
  //| Private routines |
  //+------------------+

  private void createTree()
  {
    if ((curPos != null) &&
        curPos.centerPerson.isComplete)
    {
      centerBox = new DrawingObject(globals, this, curPos.centerPerson);
      selectedBox = centerBox;

      // Build our DrawObject and Generation data trees with the
      // new center person
      centerBox.createChildTree();
      childGenerationList  = centerBox.addToChildGenerationList (null);
      centerBox.calcChildDimensions();

      centerBox.createParentTree();
      parentGenerationList = null;
      if (centerBox.father != null)
      {
        parentGenerationList = centerBox.father.addToParentGenerationList(null);
        centerBox.father.calcParentDimensions();
      }
      if (centerBox.mother != null)
      {
        parentGenerationList = centerBox.mother.addToParentGenerationList(parentGenerationList);
        centerBox.mother.calcParentDimensions();
      }
    }
  }

  private void calcYPositions()
  {
    int           childHeight, parentHeight;
    int           parentY;
    DrawingObject father, mother;
    int           parentStartX;

    vertMaxPos = 0;
    vertMinPos = 0;
    leftMostVisibleGeneration  = null;
    rightMostVisibleGeneration = null;

    childHeight = centerBox.calcChildSpacing(childGenerationList);

    centerBox.calcChildY(-(childHeight/2));

    if (parentGenerationList != null)
    {
      father = centerBox.father;
      mother = centerBox.mother;
      parentHeight = 0;
      if (father != null)
        parentHeight = father.calcParentSpacing(parentGenerationList);
      if (mother != null)
        parentHeight += mother.calcParentSpacing(parentGenerationList);
      parentY = -(parentHeight/2);
      if (father != null)
        parentY += father.calcParentY(parentY);
      if (mother != null)
        mother.calcParentY(parentY);
    }
  }

  private void calcShifts()
  {
    if (curPos != null)
    {
      shiftY = -curPos.vertCurPos;
      shiftX = -curPos.horzCurPos;
    }
    else
    {
      shiftY = screenHeight / 2;
      shiftX = screenWidth  / 2;
    }
    if (debug)
      System.out.println("PeopleList::calcShifts - setting shiftX to " + shiftX + ", shiftY to " + shiftY); //zombie
  }
}
