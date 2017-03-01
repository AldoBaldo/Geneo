package zaluc.geneo;

import java.awt.*;
import java.lang.*;
import java.util.*;

//+-- Class DrawingObject ----------------------------------------------------+
//|                                                                           |      
//| Syntax:       class DrawingObject                                         |
//|                                                                           |
//| Description:  A drawing object contains all of the information needed to  |
//|               locate where a person is drawn.  This information           |
//|               must be kept separate from the person record because,       |
//|               as in the case of incest, a person may appear in            |
//|               more than one location in the tree at a time and will       |
//|               therefore require more than one set of location information.|
//|                                                                           |
//| Methods:      public            DrawingObject      (Globals    inGlobals, |
//|                                                     PeopleList inPList)   |
//|                                                                           |
//|               public boolean    isUnderPoint       (int x,                |
//|                                                     int y)                |
//|                                                                           |
//|               public Dimension  calcDim            (FontMetrics fm)       |
//|                                                                           |
//|               public Generation addToChildGenerationList  (Generation gen)|
//|                                                                           |
//|               public Generation addToParentGenerationList (Generation gen)|
//|                                                                           |
//|               public int        calcChildSpacing   (Generation gen,       |
//|                                                     int        leftX)     |
//|                                                                           |
//|               public int        calcParentSpacing  (Generation gen,       |
//|                                                     int        leftX)     |
//|                                                                           |
//|               public int        calcChildY         (int topY)             |
//|                                                                           |
//|               public int        calcParentY        (int topY)             |
//|                                                                           |
//|               public void       drawChildren       (Graphics g)           |
//|                                                                           |
//|               public void       drawParents        (Graphics g)           |
//|                                                                           |
//|               private boolean   shouldDrawAsChild  ()                     |
//|                                                                           |
//|               private boolean   shouldDrawAsParent ()                     |
//|                                                                           |
//|               private void      draw               (Graphics g)           |
//|                                                                           |
//|               private void      drawBox            (Graphics g)           |
//|                                                                           |
//|               private void      drawLineTo         (Graphics g,           |
//|                                                     DrawingObject p)      |
//|                                                                           |
//|---------------------------------------------------------------------------+

class DrawingObject
{
  private Globals    globals;
  private PeopleList plist;   // This is the list that contains us
  public  Person     person;  // This is the person that we represent

  public  Rectangle rect;  // x and y point to top left hand corner of a people
                           // box, relative to a point in the middle of the
                           // space used by the center person and his/her
                           // spouses.
                           // Down and right are in the + direction.
  public  int       rightAnchorX;
  public  int       rightAnchorY;
  public  int       leftAnchorX;
  public  int       leftAnchorY;
  public  int       topAnchorX;
  public  int       topAnchorY;
  public  int       bottomAnchorX;
  public  int       bottomAnchorY;
  public  int       vertChildLineOffset;
  private int       cumulativeHeight;
  private int       spousesHeight;
  private int       childrensHeight;

  private Generation    myGeneration;
  public  Vector        children = new Vector(5,5);
  public  Vector        spouses  = new Vector(3,3);
  public  DrawingObject father;
  public  DrawingObject mother;

  public DrawingObject nextInGeneration;
  public DrawingObject nextVisible;
  public DrawingObject next;              // for transient linked lists.

  private final int selectionBorderWidth = 3;
  private final int selectionBoxOffset   = 1;
  private boolean   selected = false;

  public DrawingObject (Globals    inGlobals,
                        PeopleList inPList,
                        Person     inPerson)
  {
    globals = inGlobals;
    plist   = inPList;
    person  = inPerson;
    rect    = new Rectangle();
    person.drawMe = true;
  }

  public void createChildTree ()
  {
    Family        family;
    Person        pChild;
    Person        pSpouse;
    DrawingObject drawParent;
    DrawingObject curChild;

    for (int f = 0; (family = person.getFamily(f)) != null; f++)
    {
      if (person.sex == Person.male)
        pSpouse = plist.getPerson(family.mother);
      else
        pSpouse = plist.getPerson(family.father);
      if (pSpouse != null)
      {
        drawParent = new DrawingObject (globals, plist, pSpouse);
        spouses.addElement (drawParent);
      }
      else
      {
        drawParent = this;
      }

      for (int c = 0; (pChild = family.getChild(c)) != null; c++)
      {
        curChild = new DrawingObject (globals, plist, pChild);
        drawParent.children.addElement (curChild);
        curChild.createChildTree();
      }
    } // for each family
  } // createChildTree

  public void createParentTree ()
  {
    Person pMother;
    Person pFather;

    pMother = plist.getPerson(person.mother);
    pFather = plist.getPerson(person.father);

    if (pMother != null)
    {
      mother = new DrawingObject(globals, plist, pMother);
      mother.createParentTree();
    }
    if (pFather != null)
    {
      father = new DrawingObject(globals, plist, pFather);
      father.createParentTree();
    }
  } // CreateParentTree

  public boolean isUnderPoint (int x, int y)
  {
    return ((x >=  rect.x) &&
            (x <= (rect.x + rect.width)) &&
            (y >=  rect.y) &&
            (y <= (rect.y + rect.height)));
  }

  public Generation addToChildGenerationList (Generation gen)
  {
    DrawingObject curSpouse;
    DrawingObject curChild;
    int           spouseCount;
    int           childCount;

    if (gen == null)
      gen = new Generation(plist);

    // Remember this generation
    myGeneration = gen;

    // Add this DrawingObject to the top of the generation list
    nextInGeneration = gen.firstInGeneration;
    gen.firstInGeneration = this;

    // Add children drawn from this object to the next generation
    childCount = children.size();
    for (int c = 0; c < childCount; c++)
    {
      curChild = (DrawingObject) children.elementAt(c);
      gen.nextGeneration = curChild.addToChildGenerationList (gen.nextGeneration);
    }

    // Add each spouse to the top of the generation list
    spouseCount = spouses.size();
    for (int s = 0; s < spouseCount; s++)
    {
      curSpouse = (DrawingObject) spouses.elementAt(s);
      curSpouse.myGeneration = gen;

      childCount = curSpouse.children.size();
      for (int c = 0; c < childCount; c++)
      {
        curChild = (DrawingObject) curSpouse.children.elementAt(c);
        gen.nextGeneration = curChild.addToChildGenerationList (gen.nextGeneration);
      }
    }

    return gen;

  } // addToChildGenerationList

  public Generation addToParentGenerationList (Generation gen)
  {
    if (gen == null)
      gen = new Generation(plist);

    // Remember this generation
    myGeneration = gen;

    // Add this DrawingObject to the top of the generation list
    nextInGeneration = gen.firstInGeneration;
    gen.firstInGeneration = this;

    // Now add each parent to the next generation list
    if (father != null)
      gen.nextGeneration = father.addToParentGenerationList(gen.nextGeneration);
    if (mother != null)
      gen.nextGeneration = mother.addToParentGenerationList(gen.nextGeneration);

    return gen;
  }

  public void calcChildDimensions ()
  {
    DrawingObject curSpouse;
    DrawingObject curChild;
    int           spouseCount;
    int           childCount;

    // Calculate our dimensions
    calcDim();

    // Calc dim of children
    childCount = children.size();
    for (int c = 0; c < childCount; c++)
    {
      curChild = (DrawingObject) children.elementAt(c);
      curChild.calcChildDimensions();
    }

    spouseCount = spouses.size();
    for (int s = 0; s < spouseCount; s++)
    {
      curSpouse = (DrawingObject) spouses.elementAt(s);

      curSpouse.calcDim(); // Calculate our spouse's dimensions

      childCount = curSpouse.children.size();
      for (int c = 0; c < childCount; c++)
      {
        curChild = (DrawingObject) curSpouse.children.elementAt(c);
        curChild.calcChildDimensions();
      }
    }
  } // calcChildDimensions

  public void calcParentDimensions ()
  {
    calcDim();
    if (father != null)
      father.calcParentDimensions();
    if (mother != null)
      mother.calcParentDimensions();
  } // calcParentDimensions


  public Dimension calcDim ()
  {
    int nameLen, dateLen;
    int height = 0;

    if ((person.fullName       != null) &&
        (plist.nameFontMetrics != null))
    {
      nameLen = plist.nameFontMetrics.stringWidth(person.fullName);
      height  = plist.nameFontMetrics.getHeight();
    }
    else
      nameLen = 0;

    if ((person.lifeDates      != null) &&
        (plist.dateFontMetrics != null))
    {
      dateLen = plist.dateFontMetrics.stringWidth(person.lifeDates);
      height += plist.dateFontMetrics.getHeight();
    }
    else
      dateLen = 0;

    if ((height                == 0) &&
        (plist.dateFontMetrics != null))
    {
      // If there is no name or life dates, just leave an empty box, one line high
      height = plist.dateFontMetrics.getHeight();
    }

    rect.width  = Math.max (nameLen, dateLen) + 2*globals.peopleBoxBorderWidth + 2 + 2*selectionBorderWidth;
    rect.height = height + 2*globals.peopleBoxBorderWidth + 2*selectionBorderWidth;

    return new Dimension(rect.width, rect.height);
  }

  //+- method calcChildSpacing -------------------------------------------+
  //|                                                                     |
  //|  Description:                                                       |
  //|                                                                     |
  //|    When this method is called, the width of each generation has     |
  //|    already been calculated.  This routine is responsible for setting|
  //|    the X position of each child being drawn, and for calculating    |
  //|    the amount of vertical space required to draw this DrawingObject,|
  //|    his her spouses, and all of this DrawingObject's children.       |
  //|    After this method is finished, the exact Y positions of each     |
  //|    DrawingObject still need to be calculated.                       |
  //|                                                                     |
  //|  Parameters:                                                        |
  //|                                                                     |
  //|    gen   : This DrawingObjects generation                           |
  //|    leftX : The leftX position for this generation                   |
  //|                                                                     |
  //|  Return Value:                                                      |
  //|                                                                     |
  //|    Returns the amount of vertical space required for this           |
  //|    DrawingObject, his/her spouses, and all of this DrawingObject's  |
  //|    children.                                                        |
  //|                                                                     |
  //+---------------------------------------------------------------------+

  public int calcChildSpacing (Generation gen)
  {
    int           childX = 0;
    Generation    nextGen;
    Family        family;
    DrawingObject curSpouse;
    DrawingObject curChild;
    int           spouseCount;
    int           childCount;

    // Set X positions of this generation
    setX(myGeneration.leftX + myGeneration.maxVertLineOffset + plist.trunkLen);
//    setX(leftX);
//    myGeneration.leftX = leftX;

    // Calculate the X positions of the next generation
    nextGen = gen.nextGeneration;
//    if (nextGen != null)
//      childX = leftX - nextGen.dim.width;

    // Calculate the height of this individual plus all of his or
    // her spouses, and the cumulativeHeight of all of this DrawingObject's
    // children.  The greater of these two heights is the cumulativeHeight
    // of this individual (i.e. the amount of vertical space this DrawingObject
    // and his descendants will require).

    spousesHeight = rect.height + plist.vertSpace;    // Init to this DrawingObject's height
    childrensHeight = 0;


    if (shouldDrawAsChild())
    {
      childCount = children.size();
      for (int c = 0; c < childCount; c++)
      {
        curChild = (DrawingObject) children.elementAt(c);
        childrensHeight += curChild.calcChildSpacing(nextGen);
      }

      spouseCount = spouses.size();
      for (int s = 0; s < spouseCount; s++)
      {
        curSpouse = (DrawingObject) spouses.elementAt(s);

        curSpouse.setX(rect.x);
        spousesHeight += curSpouse.rect.height + plist.vertSpouseSpace;    // Add spouse's height

        childCount = curSpouse.children.size();
        for (int c = 0; c < childCount; c++)
        {
          curChild = (DrawingObject) curSpouse.children.elementAt(c);
          childrensHeight += curChild.calcChildSpacing(nextGen);
        }
      }

      cumulativeHeight = Math.max (spousesHeight, childrensHeight);

    } // if shouldDrawAsChild
    else
    {
      spousesHeight    = 0;
      childrensHeight  = 0;
      cumulativeHeight = 0;
    }

    return cumulativeHeight;

  } // calcChildSpacing

  public int calcParentSpacing (Generation gen)
  {
    Generation nextGen;

    cumulativeHeight = 0;

    // for each Parent, add the width of the Parent
    nextGen = gen.nextGeneration;
    if (nextGen != null)
    {
      //zombie
      //if (father != null)
      //  System.out.println("calcParentSpacing calling shouldDrawAsParent for " + father.person.fullName);

      if ((father != null) &&
          father.shouldDrawAsParent())
        cumulativeHeight += father.calcParentSpacing(nextGen);

      //zombie
      //if (mother != null)
      //  System.out.println("calcParentSpacing calling shouldDrawAsParent for " + mother.person.fullName);

      if ((mother != null) &&
          mother.shouldDrawAsParent())
        cumulativeHeight += mother.calcParentSpacing(nextGen);
    }

    if (cumulativeHeight == 0)  // no Parents were added
      cumulativeHeight = rect.height + plist.vertSpace;

    return cumulativeHeight;

  } // CalcParentSpacing

  //+- method calcChildY -------------------------------------------------+
  //|                                                                     |
  //|  Description:                                                       |
  //|                                                                     |
  //|    This method calculates the Y positions of the this DrawingObject,|
  //|    his/her spouses, and all of their children.                      |
  //|                                                                     |
  //|    It also finds the left and right most visible generations.       |
  //|                                                                     |
  //|  Parameters:                                                        |
  //|                                                                     |
  //|    topY  : The top Y position of this DrawingObject's vertical space|
  //|            that was calculated in calcChildSpacing.                 |
  //|                                                                     |
  //|  Return Value:                                                      |
  //|                                                                     |
  //|    Returns the amount of vertical space required for this           |
  //|    DrawingObject, his/her spouses, and all of this DrawingObject's  |
  //|    children.                                                        |
  //|                                                                     |
  //+---------------------------------------------------------------------+

  public int calcChildY (int topY)
  {
    int           childY;
    int           spouseY;
    Family        family;
    DrawingObject curSpouse;
    DrawingObject prevSpouse;
    DrawingObject curChild;
    int           spouseCount;
    int           childCount;
    boolean       shouldDrawChildren;

    if (childrensHeight < spousesHeight)
    {
      setY(topY + (plist.vertSpace / 2));
      childY = topY + ((spousesHeight - childrensHeight) / 2);
    }
    else
    {
      setY(topY + ((childrensHeight - spousesHeight + plist.vertSpace) / 2));
      childY = topY;
    }
    spouseY = rect.y;

    shouldDrawChildren = (myGeneration.nextGeneration != null) &&
                         myGeneration.nextGeneration.shouldDrawGenAsChild();

    if ((plist.leftMostVisibleGeneration == null) ||
       (myGeneration.leftX < plist.leftMostVisibleGeneration.leftX))
    {
      plist.leftMostVisibleGeneration = myGeneration;
    }

    if (shouldDrawChildren)
    {
      childCount = children.size();
      for (int c = 0; c < childCount; c++)
      {
        curChild = (DrawingObject) children.elementAt(c);
        childY += curChild.calcChildY (childY);
      }
    }

    spouseCount = spouses.size();
    prevSpouse = this;
    for (int s = 0; s < spouseCount; s++)
    {
      curSpouse = (DrawingObject) spouses.elementAt(s);
      spouseY += prevSpouse.rect.height + plist.vertSpouseSpace;
      curSpouse.setY(spouseY);
      prevSpouse = curSpouse;

      if (shouldDrawChildren)
      {
        childCount = curSpouse.children.size();
        for (int c = 0; c < childCount; c++)
        {
          curChild = (DrawingObject) curSpouse.children.elementAt(c);
          childY += curChild.calcChildY (childY);
        }
      }
    }

    // Do this after recursing into curChild.calcChildY to overwrite
    // later visible generations
    if (shouldDrawAsParent() &&
        ((plist.rightMostVisibleGeneration == null) ||
         (myGeneration.leftX > plist.rightMostVisibleGeneration.leftX)))
    {
      plist.rightMostVisibleGeneration = myGeneration;
    }

    return cumulativeHeight;

  } // calcChildY

  public int calcParentY (int topY)
  {
    int    ParentY = topY;

    setY(topY + ((cumulativeHeight - rect.height) / 2));

    if ((plist.rightMostVisibleGeneration == null) ||
        (myGeneration.leftX > plist.rightMostVisibleGeneration.leftX))
    {
      plist.rightMostVisibleGeneration = myGeneration;
    }

    //zombie
    //if (father != null)
    //  System.out.println("calcParentY calling shouldDrawAsParent for " + father.person.fullName);

    if ((father != null) &&
        father.shouldDrawAsParent())
      ParentY += father.calcParentY (ParentY);

    //zombie
    //if (mother != null)
    //  System.out.println("calcParentY calling shouldDrawAsParent for " + mother.person.fullName);

    if ((mother != null) &&
        mother.shouldDrawAsParent())
      ParentY += mother.calcParentY (ParentY);

    if (shouldDrawAsChild() &&
        ((plist.leftMostVisibleGeneration == null) ||
         (myGeneration.leftX < plist.leftMostVisibleGeneration.leftX)))
    {
      plist.leftMostVisibleGeneration = myGeneration; // Do this on the way back to overwrite earlier visible generations
    }

    return cumulativeHeight;
  } // calcParentY

  public void setX (int leftX)
  {
    rect.x = leftX;
    leftAnchorX  = rect.x;
    rightAnchorX = rect.x + rect.width;
    topAnchorX = bottomAnchorX = rect.x + rect.width/2;
  }

  public void setY (int topY)
  {
    rect.y = topY;
    rightAnchorY  = leftAnchorY = rect.y + rect.height/2;
    topAnchorY    = rect.y;
    bottomAnchorY = rect.y + rect.height;

    if (rect.y < 0)
      plist.vertMinPos = Math.min(plist.vertMinPos, rect.y - plist.vertSpace);
    if ((rect.y + rect.height) > 0)
      plist.vertMaxPos = Math.max(plist.vertMaxPos, rect.y + rect.height + plist.vertSpace);
  }

  public void drawChildren (Graphics g)
  {
    DrawingObject prevSpouse;
    DrawingObject curSpouse;
    DrawingObject curChild;
    int           spouseCount;
    int           childCount;
    boolean       shouldDrawChildren;

    // This is done as a kludge to save one loop through a child generation
    // and it's spouses since Generation#calcChildDim doesn't know it's
    // X coordinate until after it loops through everyone in the generation.
    // So, rather than have calcChildDim loop through a second time to set
    // the X pos, we set it here.
    setX(myGeneration.leftX + myGeneration.maxVertLineOffset + plist.trunkLen);

    // Draw this DrawingObject
    draw (g);

    shouldDrawChildren = (myGeneration.nextGeneration != null) &&
                         myGeneration.nextGeneration.shouldDrawGenAsChild();

    childCount = children.size();
    if (shouldDrawChildren)
    {
      for (int c = 0; c < childCount; c++)
      {
        curChild = (DrawingObject) children.elementAt(c);
        curChild.drawChildren(g);
        drawLineTo(g, curChild);
      }
    }
    else if (childCount != 0)
    {
      // Draw a line from this person to the end of the screen
      // to indicate that there are more people to the left
      drawLine (g, leftAnchorX + plist.shiftX,
                  leftAnchorY + plist.shiftY,
                  0,
                  leftAnchorY + plist.shiftY);
    }

    // Now draw each spouse and their children
    prevSpouse = this;
    spouseCount = spouses.size();
    for (int s = 0; s < spouseCount; s++)
    {
      curSpouse = (DrawingObject) spouses.elementAt(s);
      curSpouse.setX(rect.x);
      curSpouse.draw(g);
      prevSpouse.drawSpouseLineTo(g, curSpouse);
      prevSpouse = curSpouse;

      childCount = curSpouse.children.size();
      if (shouldDrawChildren)
      {
        for (int c = 0; c < childCount; c++)
        {
          curChild = (DrawingObject) curSpouse.children.elementAt(c);
          curChild.drawChildren(g);
          curSpouse.drawLineTo(g, curChild);
        }
      }
      else if (childCount != 0)
      {
        // Draw a line from this person to the end of the screen
        // to indicate that there are more people to the left
        drawLine (g, curSpouse.leftAnchorX + plist.shiftX,
                    curSpouse.leftAnchorY + plist.shiftY,
                    0,
                    curSpouse.leftAnchorY + plist.shiftY);
      }
    }
  } // drawChildren

  public void drawParents (Graphics g)
  {
    boolean drawTail = false;

    if (father != null)
    {
      //zombie
      //System.out.println("drawParents calling shouldDrawAsParent for " + father.person.fullName);

      if (father.shouldDrawAsParent())
      {
        father.draw(g);
        father.drawLineTo (g, this);
        father.drawParents (g);
      }
      else
        drawTail = true;
    }

    if (mother != null)
    {
      //zombie
      //System.out.println("drawParents calling shouldDrawAsParent for " + mother.person.fullName);

      if (mother.shouldDrawAsParent())
      {
        mother.draw(g);
        mother.drawLineTo (g, this);
        mother.drawParents (g);
      }
      else
        drawTail = true;
    }

    if (drawTail)
    {
      // Draw a line from this person to the end of the screen
      // to indicate that there are more people to the right
      drawLine (g, rightAnchorX + plist.shiftX,
                  rightAnchorY + plist.shiftY,
                  plist.screenWidth,
                  rightAnchorY + plist.shiftY);
    }
  } // drawParents

  public void select (Graphics g)
  {
    selected = true;
    drawSelectionBox(g);
  }

  public void deselect (Graphics g)
  {
    selected = false;
    clearSelectionBox(g);
  }

  //+------------------+
  //| Private routines |
  //+------------------+

  private boolean shouldDrawAsChild()
  {
    // Use myGeneration.leftX instead of rect.x
    // because rect.x may not have been set yet.
//    return (myGeneration.leftX     +
//            myGeneration.dim.width +
//            plist.shiftX) >= 0;
    return myGeneration.shouldDrawGenAsChild();
  }

  private boolean shouldDrawAsParent()
  {
//    return (rect.x + plist.shiftX) <= plist.screenWidth;
    return myGeneration.shouldDrawGenAsParent();
  }

  private void draw (Graphics g)
  {
    if (!person.isBlank)
    {
      // Add to visible list
      nextVisible = plist.visibleList;
      plist.visibleList = this;
    }

    drawBox(g);

    if (selected)
      drawSelectionBox(g);

    if ((person.fullName != null) &&
        (plist.nameFont  != null))
    {
      g.setFont (plist.nameFont);
      g.drawString (person.fullName,
                    rect.x + plist.shiftX + globals.peopleBoxBorderWidth + selectionBorderWidth + 1,
                    rect.y + plist.shiftY + globals.peopleBoxBorderWidth + selectionBorderWidth + plist.fontHeight - 2);
    }

    if ((person.lifeDates != null) &&
        (plist.dateFont   != null))
    {
      g.setFont (plist.dateFont);
      g.drawString (person.lifeDates,
                    rect.x + plist.shiftX + globals.peopleBoxBorderWidth + selectionBorderWidth + 1,
                    rect.y + plist.shiftY + globals.peopleBoxBorderWidth + selectionBorderWidth + 2*plist.fontHeight - 2);
    }
  }

  private void drawBox(Graphics g)
  {
    if (isVisibleOnScreen (rect.x + plist.shiftX,
                           rect.y + plist.shiftY,
                           rect.x + plist.shiftX + rect.width,
                           rect.y + plist.shiftY + rect.height))
    {
      if (globals.peopleBoxBkg != null)
      {
        // Fill the inside of the rectangle
        g.setColor (globals.peopleBoxBkg);
        g.fillRect (rect.x + plist.shiftX, rect.y + plist.shiftY, rect.width, rect.height);
      }

      if (globals.peopleBoxBorderWidth > 1)
      {
        int i;
        int topY    = rect.y + plist.shiftY;
        int leftX   = rect.x + plist.shiftX;
        int bottomY = rect.y + plist.shiftY + rect.height;
        int rightX  = rect.x + plist.shiftX + rect.width;
        int red;
        int green;
        int blue;

        // Figure out my border colors

        if ((globals.peopleBoxBkg    != null) &&
            (globals.backgroundImage != null))
        {
          red   = globals.peopleBoxBkg.getRed();
          green = globals.peopleBoxBkg.getGreen();
          blue  = globals.peopleBoxBkg.getBlue();
        }
        else
        {
          red   = globals.backgroundColor.getRed();
          green = globals.backgroundColor.getGreen();
          blue  = globals.backgroundColor.getBlue();
        }

        int redShift   = (255-red)   / 5;
        int greenShift = (255-green) / 5;
        int blueShift  = (255-blue)  / 5;

  //      Color lightInner = new Color (red + 2*redShift, green + 2*greenShift, blue + 2*blueShift);
        Color lightOuter = new Color (red + 3*redShift, green + 3*greenShift, blue + 3*blueShift);

        redShift   = red   / 5;
        greenShift = green / 5;
        blueShift  = blue  / 5;

        Color darkInner  = new Color (3*redShift, 3*greenShift, 3*blueShift);
        Color darkOuter  = new Color (4*redShift, 4*greenShift, 4*blueShift);

        // Draw the right and bottom sides lightened

        g.setColor(lightOuter);
        for (i = 0; i < (globals.peopleBoxBorderWidth - 1); i++)
        {
          drawLine (g, rightX - i, bottomY - i, rightX - i, topY    + i);
          drawLine (g, rightX - i, bottomY - i, leftX  + i, bottomY - i);
        }

        g.setColor(darkOuter);
        drawLine (g, rightX - i, bottomY - i, rightX - i, topY    + i);
        drawLine (g, rightX - i, bottomY - i, leftX  + i, bottomY - i);

        // Draw the left and top sides darkened

        g.setColor(darkOuter);
        for (i = 0; i < (globals.peopleBoxBorderWidth - 1); i++)
        {
          drawLine (g, leftX + i, topY + i, rightX - i, topY    + i);
          drawLine (g, leftX + i, topY + i, leftX  + i, bottomY - i);
        }

        g.setColor(darkInner);
        drawLine (g, leftX + i, topY + i, rightX - i, topY    + i);
        drawLine (g, leftX + i, topY + i, leftX  + i, bottomY - i);

        g.setColor (globals.foregroundColor);
      }
      else
      {
        g.setColor (globals.foregroundColor);
        g.drawRect (rect.x + plist.shiftX, rect.y + plist.shiftY, rect.width, rect.height);
      }
    } // if isVisibleOnScreen
  }

  private void drawSelectionBox(Graphics g)
  {
    int topY    = rect.y + plist.shiftY + globals.peopleBoxBorderWidth + selectionBoxOffset;
    int leftX   = rect.x + plist.shiftX + globals.peopleBoxBorderWidth + selectionBoxOffset;
    int bottomY = rect.y + plist.shiftY - globals.peopleBoxBorderWidth - selectionBoxOffset + rect.height;
    int rightX  = rect.x + plist.shiftX - globals.peopleBoxBorderWidth - selectionBoxOffset + rect.width;

    g.setColor (globals.foregroundColor);

    drawDashedHorzLine (g, leftX, rightX,    topY);  // top line
    drawDashedHorzLine (g, leftX, rightX, bottomY);  // bottom line

    drawDashedVertLine (g, topY, bottomY,  leftX);  // left line
    drawDashedVertLine (g, topY, bottomY, rightX);  // right line
  }

  private void clearSelectionBox(Graphics g)
  {
    int topY    = rect.y + plist.shiftY + globals.peopleBoxBorderWidth + selectionBoxOffset;
    int leftX   = rect.x + plist.shiftX + globals.peopleBoxBorderWidth + selectionBoxOffset;
    int bottomY = rect.y + plist.shiftY - globals.peopleBoxBorderWidth - selectionBoxOffset + rect.height;
    int rightX  = rect.x + plist.shiftX - globals.peopleBoxBorderWidth - selectionBoxOffset + rect.width;

    if (globals.peopleBoxBkg != null)
      g.setColor (globals.peopleBoxBkg);
    else
      g.setColor (globals.backgroundColor);

    drawLine (g,  leftX,    topY, rightX,    topY);  // top line
    drawLine (g,  leftX, bottomY, rightX, bottomY);  // bottom line
    drawLine (g,  leftX,    topY,  leftX, bottomY);  // left line
    drawLine (g, rightX,    topY, rightX, bottomY);  // right line
  }

  private void drawLineTo (Graphics      g,
                           DrawingObject p)
  {
    int sourceX, sourceY, destX, destY, elbowX;
    int trunkLen = plist.trunkLen;

    if (rect.x > p.rect.x)
    {
      //zombie
//      if (plist.debug &&
//          (((leftAnchorY - p.rightAnchorY) > 20000) ||
//           ((p.rightAnchorY - leftAnchorY) > 20000)))
//      {
//        System.out.println("Drawing from " + person.fullName + " at (" +   leftAnchorX + "," +   leftAnchorY + ") to "
//                                           + p.person.fullName + " at (" + p.rightAnchorX + "," + p.rightAnchorY + ")");
//      }
      // We are the parent, so draw the line left to the child
      sourceX = leftAnchorX + plist.shiftX - 1;
      sourceY = leftAnchorY + plist.shiftY;
      destX   = p.rightAnchorX + plist.shiftX + 1;
      destY   = p.rightAnchorY + plist.shiftY;
      elbowX  = sourceX - trunkLen - myGeneration.maxVertLineOffset + vertChildLineOffset;

      drawLine (g, sourceX, sourceY, elbowX, sourceY);
      drawLine (g, elbowX , sourceY, elbowX,   destY);
      drawLine (g, elbowX ,   destY,  destX,   destY);
    }
    else
    {
      System.out.println("Error: drawing line backwards from " + person.id + " at " + rect.x +
                         " to " + p.person.id + " at " + p.rect.x);
    }
  }

  private void drawSpouseLineTo (Graphics g,
                                 DrawingObject   p)
  {
    int jointX, sourceY, destY;

    // We are the top spouse, so draw the line down to the next spouse
    jointX = rect.x + myGeneration.spouseLineXOffset + plist.shiftX - 1;
    sourceY = bottomAnchorY + plist.shiftY;
    destY   = p.topAnchorY + plist.shiftY;

    drawLine (g, jointX - 1, sourceY, jointX - 1, destY);
    drawLine (g, jointX + 1, sourceY, jointX + 1, destY);
  }

  private void drawLine (Graphics g, int x1, int y1, int x2, int y2)
  {
    // make sure at least part of the line will be seen
    if (isVisibleOnScreen (x1, y1, x2, y2))
    {
//      if (plist.debug)
//          (((y1 - y2) > 10000) ||
//           ((y2 - y1) > 10000)))
//        System.out.println ("Drawing line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
      g.drawLine (x1, y1, x2, y2);
    }
  }

  private boolean isVisibleOnScreen (int x1, int y1, int x2, int y2)
  {
    boolean x_within_screen;
    boolean y_within_screen;
    boolean x_spans_screen;
    boolean y_spans_screen;

    x_within_screen = ( (x1 >= 0) && (x1 <= plist.screenWidth) ) ||
                      ( (x2 >= 0) && (x2 <= plist.screenWidth) ) ;

    y_within_screen = ( (y1 >= 0) && (y1 <= plist.screenHeight) ) ||
                      ( (y2 >= 0) && (y2 <= plist.screenHeight) ) ;

    x_spans_screen  = ( (x1 <= 0) && (x2 >= plist.screenWidth) ) ||
                      ( (x2 <= 0) && (x1 >= plist.screenWidth) ) ;

    y_spans_screen  = ( (y1 <= 0) && (y2 >= plist.screenHeight) ) ||
                      ( (y2 <= 0) && (y1 >= plist.screenHeight) ) ;

    if ((x_within_screen || x_spans_screen) &&
        (y_within_screen || y_spans_screen))
      return true;
    else
      return false;
  }

  private void drawDashedHorzLine (Graphics g, int x1, int x2, int y)
  {
    final int spacing = 1;
    int start;
    int end;

    for (start = x1; start <= x2; start += 5*spacing)
    {
      if ((start + spacing) > x2)
        end = x2;
      else
        end = start + spacing;

      drawLine (g, start, y, end, y);
    }
  }

  private void drawDashedVertLine (Graphics g, int y1, int y2, int x)
  {
    final int spacing = 1;
    int start;
    int end;

    for (start = y1; start <= y2; start += 5*spacing)
    {
      if ((start + spacing) > y2)
        end = y2;
      else
        end = start + spacing;

      drawLine (g, x, start, x, end);
    }
  }
}


