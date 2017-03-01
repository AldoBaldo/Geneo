package zaluc.gparser200;

import java.awt.*;
import java.lang.*;
import java.io.*;
import java.util.*;

import zaluc.utils.*;

//+-- Class Family -----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class Family                                                |
//|                                                                           |
//| Description:  This class links parents to each other and to their         |
//|               children.  It does not connect children to their parents.   |
//|               That is done with the Person.mother and Person.father       |
//|               links.                                                      |
//|                                                                           |
//| Methods:      addChild: Add a child to the family.                        |
//|               write:    Writes the data for the family to the output file.|
//|                                                                           |
//|---------------------------------------------------------------------------+

class Family implements SortableObject
{
  int            id = 0;
  SortableHandle wifesListHandle    = new SortableHandle (this);
  SortableHandle husbandsListHandle = new SortableHandle (this);
  GedcomEvent    marriage = null;
  Person         father;
  Person         mother;
  SortableVector children = new SortableVector(6,6, Person.compareByAge);

  /*-------------------------------------------------------------------------*/
  /* This index is the index into the main family list, which is not sorted. */
  /* The listHandles also contain an index.  The listHandle's are used by    */
  /* the father and mother of the family to sort their families.  The index  */
  /* below is the one that is used to indentify the family by Geneo.         */
  /*-------------------------------------------------------------------------*/
  int index;

  public boolean written = false;

  public void addChild (Person child)
  {
    children.addElement(child.familyListHandle);
  }

  public void write(Record record) throws IOException
  {
    Enumeration enum;
    Person      curChild;

    if (!written)
    {
      written = true;
      record.write(Record.FAMILY, index);
      if (father != null) record.write(Record.FATHER, father.mainListHandle.getIndex());
      if (mother != null) record.write(Record.MOTHER, mother.mainListHandle.getIndex());

      enum = children.elements();
      while (enum.hasMoreElements())
      {
        curChild = (Person) ((SortableHandle) enum.nextElement()).getContainer();
        record.write(Record.CHILD, curChild.mainListHandle.getIndex());
      }
    }
  }

  //+-----------------------------------------+
  //| Routine for implementing SortableObject |
  //+-----------------------------------------+

  public int compareTo (SortableObject soOther, int compareToken)
  {
    Family      other = (Family)soOther;
    int         ret = 0;
    GedcomEvent event1, event2;

    /* Since we always sort the same way, */
    /* we can ignore the compareToken     */

    /* First find some dates we can compare.  We prefer the marriage date, */
    /* but, if there isn't one, we'll take the birth date of a child.      */

    event1 = getComparableEvent();
    if (other != null)
      event2 = other.getComparableEvent();
    else
      event2 = null;

    if (event1 != null)
    {
      ret = event1.compareTo (event2, compareToken);
    }
    else if (event2 != null)
    {
      /* Return null marriages after known marriages */
      ret = 1; 
    }
    else
    {
      ret = 0;
    }

    return ret;
  }

  /**
   * Don't return an event unless it has a date associated with it.
   */
  private GedcomEvent getComparableEvent ()
  {
    Enumeration enum;
    Person      curChild;

    /* Try to return a marriage date if there is one */

    if ((marriage != null) &&
        (marriage.getDate() != null))
      return marriage;

    /* If there is no marriage date, try to find a birth date for */
    /* one of the kids, starting with the oldest to the youngest. */
    /* Note: Children should alreay be sorted from oldest to      */
    /* youngest.                                                  */

    enum = children.elements();
    while (enum.hasMoreElements())
    {
      curChild = (Person) ((SortableHandle) enum.nextElement()).getContainer();
      if ((curChild != null) &&
          (curChild.birth != null) &&
          (curChild.birth.getDate() != null))
        return curChild.birth;
    }

    /* No dates could be found to determine the start of this family */

    return null;
  }
}
