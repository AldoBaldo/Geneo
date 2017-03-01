package zaluc.geneo;

import java.awt.*;
import java.lang.*;
import java.util.*;

//+-- Class Generation -------------------------------------------------------+
//|                                                                           |
//| Syntax:       class Generation                                            |
//|                                                                           |
//| Description:  The Generation class is responsible for maintaining a list  |
//|               of Persons that belong to a single generation, and for      |
//|               maintaining information about that generation, such as the  |
//|               size of the box that contains all individuals in the        |
//|               generation.                                                 |
//|                                                                           |
//| Methods:      public     Generation    (PeopleList plist)                 |
//|                                                                           |
//|               public int calcChildDim  (boolean isFirstGen,               |
//|                                         boolean calcVertLineOffsets,      |
//|                                         int     leftX)                    |
//|                                                                           |
//|               public int calcParentDim (int leftX)                        |
//|                                                                           |
//|---------------------------------------------------------------------------+

class Generation
{
  private PeopleList  plist;

  public Generation    nextGeneration;
  public DrawingObject firstInGeneration;

  public Dimension  dim;
  public int        leftX;
  public int        spouseLineXOffset;
  public int        maxVertLineOffset; // # of overlapping vertical lines from
                                       // different spouses to their children
                                       // that need to be offset so that they
                                       // don't overlap

  public Generation(PeopleList plist)
  {
    this.plist = plist;
    nextGeneration = null;
    firstInGeneration = null;
    dim = new Dimension(0, 0);
    maxVertLineOffset = 0;
  }

  //+- method calcChildDim -----------------------------------------------+
  //|                                                                     |
  //|  Description:                                                       |
  //|                                                                     |
  //|    calcChildDim calculates the dimensions of a generation.  By the  |
  //|    time this routine is called, we already know who will be drawn   |
  //|    and what the individual dimensions of each person is.  We also   |
  //|    know the exact Y coordinates of each person.                     |
  //|                                                                     |
  //|    For the most part, this routine is fairly simple in that it      |
  //|    simply iterates through all the people in this generation,       |
  //|    summing their heights and taking the max of their widths.  These |
  //|    are the dimensions of the generation.  However, a complication   |
  //|    arises when a spouses children are pushed either up or down      |
  //|    because of another spouses children.  Then the vertical part of  |
  //|    the line between that spouse and his/her children will overlap   |
  //|    with the vertical part of the line between the other spouse and  |
  //|    his/her children.  When this happens, we need to offset the      |
  //|    vertical lines from each other and this adds to the horizontal   |
  //|    dimensions of the generation.                                    |
  //|                                                                     |
  //|    Note a subtle difference between this routine and calcParentDim. |
  //|    calcChildDim routine takes the leftX of it's parent generation   |
  //|    as a parameter and returns it's own leftX, whereas calcParentDim |
  //|    takes it's own leftX as a parameter and returns the leftX of     |
  //|    it's parents.                                                    |
  //|                                                                     |
  //|    The sense of this becomes clear when you realize that both       |
  //|    routines receive the X coordinate of the side of the generation  |
  //|    that is closest to the centerPerson and then adds or subtracts   |
  //|    their own width to calculate and return the X coordinate of the  |
  //|    side away from the center person.                                |
  //|                                                                     |
  //|  Parameters:                                                        |
  //|                                                                     |
  //|    leftX : The left-most position of the parent generation          |
  //|                                                                     |
  //|  Return Value:                                                      |
  //|                                                                     |
  //|    The left-most position of the current generation.                |
  //|                                                                     |
  //+---------------------------------------------------------------------+

  public int calcChildDim(boolean isFirstGen,
                          boolean calcVertLineOffsets,
                          int     leftX)
  {
    DrawingObject curDObj;
    int           minWidth = Integer.MAX_VALUE;
    int           maxNumVertOffsets = 0;
    int           numVertOffsets;
    int           numOffsetsAbove;
    int           numOffsetsBelow;
    DrawingObject curSpouse;
    DrawingObject curChild;
    int           spouseCount;
    int           childCount;
    boolean       childIsAbove = false;
    boolean       childIsBelow = false;
    boolean       childIsEven  = false;
    DrawingObject spousesAbove = null;
    DrawingObject spousesBelow = null;
    int           curOffset;

    dim.width = dim.height = 0;

    for (curDObj = firstInGeneration;
         curDObj != null;
         curDObj = curDObj.nextInGeneration)
    {
      dim.width   = Math.max(dim.width, curDObj.rect.width);
      dim.height += curDObj.rect.height;
      minWidth    = Math.min(minWidth, curDObj.rect.width);

      numOffsetsAbove = 0;
      numOffsetsBelow = 0;
      spousesAbove = null;
      spousesBelow = null;

      // Add the dimensions for each spouse
      spouseCount = curDObj.spouses.size();
      for (int s = 0; s < spouseCount; s++)
      {
        curSpouse = (DrawingObject) curDObj.spouses.elementAt(s);
        dim.width   = Math.max(dim.width, curSpouse.rect.width);
        dim.height += curSpouse.rect.height;
        minWidth    = Math.min(minWidth, curSpouse.rect.width);
        curSpouse.vertChildLineOffset = 0;

        if (calcVertLineOffsets)
        {
          // In this section, we determine if we will need to offset the
          // vertical parts of the lines between spouses and their children
          // in order to avoid overlap.  If all of the children's Y positions
          // are far above that of the spouse, then another spouse's children
          // are 'pushing' them up there and we will need to offset the
          // vertical line.  The same is also true of children far below the
          // spouse.  However, if a spouse has children both above and below,
          // then there is no need to worry about overlap.
          childIsAbove = childIsBelow = childIsEven = false;
          childCount = curSpouse.children.size();
          for (int c = 0; c < childCount; c++)
          {
            curChild = (DrawingObject) curSpouse.children.elementAt(c);
            if (curChild.rect.y < (curSpouse.rect.y - plist.vertSpace))
            {
              childIsAbove = true;
            }
            else if (curChild.rect.y > (curSpouse.rect.y + plist.vertSpace))
            {
              childIsBelow = true;
            }
            else
            {
              childIsEven  = true;
            }
          }
          if (!childIsEven)
          {
            if (childIsAbove && !childIsBelow)
            {
              numOffsetsAbove++;
              curSpouse.next = spousesAbove;
              spousesAbove = curSpouse;
            }
            else if (childIsBelow && !childIsAbove)
            {
              numOffsetsBelow++;
              curSpouse.next = spousesBelow;
              spousesBelow = curSpouse;
            }
          }
        } // if calcVertLineOffsets
      } // for each spouse

      if (calcVertLineOffsets)
      {
        // Update the max number of vertical line offsets for this generation
        numVertOffsets = Math.max (numOffsetsAbove, numOffsetsBelow);
        maxNumVertOffsets = Math.max(maxNumVertOffsets, numVertOffsets);

        // Set the vertical line offset for each spouse
        // Start with the last one added first (this one has the largest offset
        curOffset = numOffsetsAbove * plist.vertLineOffset;
        for (curSpouse  = spousesAbove;
             curSpouse != null;
             curSpouse  = curSpouse.next)
        {
          curSpouse.vertChildLineOffset = curOffset;
          curOffset -= plist.vertLineOffset;
        }
        curOffset = numOffsetsBelow * plist.vertLineOffset;
        for (curSpouse  = spousesBelow;
             curSpouse != null;
             curSpouse  = curSpouse.next)
        {
          curSpouse.vertChildLineOffset = curOffset;
          curOffset -= plist.vertLineOffset;
        }
      } // if calcVertLineOffsets
    } // for curDObj

    // Make all members of a generation the same width
    for (curDObj  = firstInGeneration;
         curDObj != null;
         curDObj  = curDObj.nextInGeneration)
    {
	    curDObj.rect.width = dim.width;
      // Add the dimensions for each spouse
      spouseCount = curDObj.spouses.size();
      for (int s = 0; s < spouseCount; s++)
      {
        curSpouse = (DrawingObject) curDObj.spouses.elementAt(s);
        curSpouse.rect.width = dim.width;
      }
	  }

    maxVertLineOffset  = maxNumVertOffsets * plist.vertLineOffset;
    dim.width         += maxVertLineOffset + plist.horzSpace;
    if (isFirstGen)
      this.leftX = leftX - (dim.width / 2);  // Center this gen in the tree space
    else
      this.leftX = leftX - dim.width;
    spouseLineXOffset  = minWidth / 2;

    return this.leftX;
  }

  //+- method calcParentDim ----------------------------------------------+
  //|                                                                     |
  //|  Description:                                                       |
  //|                                                                     |
  //|    calcParentDim calculates the dimensions of a generation.  By the |
  //|    time this routine is called, we already know who will be drawn   |
  //|    and what the individual dimensions of each person is.  We also   |
  //|    know the exact Y coordinates of each person.                     |
  //|                                                                     |
  //|    This routine simply iterates through all the people in this      |
  //|    generation, summing their heights and taking the max of their    |
  //|    widths.  These are the dimensions of the generation.             |
  //|                                                                     |
  //|    Note a subtle difference between this routine and calcChildDim.  |
  //|    calcChildDim routine takes the leftX of it's parent generation   |
  //|    as a parameter and returns it's own leftX, whereas calcParentDim |
  //|    takes it's own leftX as a parameter and returns the leftX of     |
  //|    it's parents.                                                    |
  //|                                                                     |
  //|    The sense of this becomes clear when you realize that both       |
  //|    routines receive the X coordinate of the side of the generation  |
  //|    that is closest to the centerPerson and then adds or subtracts   |
  //|    their own width to calculate and return the X coordinate of the  |
  //|    side away from the center person.                                |
  //|                                                                     |
  //|  Parameters:                                                        |
  //|                                                                     |
  //|    leftX : The left-most position of the current generation         |
  //|                                                                     |
  //|  Return Value:                                                      |
  //|                                                                     |
  //|    The left-most position of the parent generation.                 |
  //|                                                                     |
  //+---------------------------------------------------------------------+

  public int calcParentDim(int leftX)
  {
    DrawingObject curDObj;

    dim.width = dim.height = 0;

    this.leftX = leftX;
    for (curDObj  = firstInGeneration;
         curDObj != null;
         curDObj  = curDObj.nextInGeneration)
    {
      curDObj.setX(leftX + plist.trunkLen);
      curDObj.vertChildLineOffset = 0;
      dim.width   = Math.max(dim.width, curDObj.rect.width);
      dim.height += curDObj.rect.height;
    } // for curDObj

    for (curDObj  = firstInGeneration;
         curDObj != null;
         curDObj  = curDObj.nextInGeneration)
    {
	    curDObj.rect.width = dim.width;
	  }

    dim.width += plist.horzSpace;

    return leftX + dim.width;
  }

  public boolean shouldDrawGenAsChild()
  {
    // Use myGeneration.leftX instead of rect.x
    // because rect.x may not have been set yet.
    return (leftX + dim.width + plist.shiftX) >= 0;
  }

  public boolean shouldDrawGenAsParent()
  {
    //zombie
    //boolean temp = (leftX + plist.shiftX) <= plist.screenWidth;
    //System.out.println ("shouldDrawGenAsParent returning ((" + leftX + " + " + plist.shiftX + ") <= " + plist.screenWidth + ") = " + temp);
    return (leftX + plist.shiftX) <= plist.screenWidth;
  }

}
