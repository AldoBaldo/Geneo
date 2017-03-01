package zaluc.geneo;

import java.awt.*;
import java.lang.*;
import java.util.*;

//+-- Class Person -----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class Person                                                |
//|                                                                           |
//| Description:  Person contains all of the data relevant to a particular    |
//|               individual, including that individuals location on the      |
//|               screen if that person is currently displayes.  It also      |
//|               provides methods for calculating that person's location     |
//|               and for drawing that person.                                |
//|                                                                           |
//| Methods:      public            Person             (Globals    inGlobals, |
//|                                                     PeopleList inPList)   |
//|                                                                           |
//|               public int        compareTo          (Person other)         |
//|                                                                           |
//|---------------------------------------------------------------------------+

class Person
{
  public static final int male   = 1;
  public static final int female = 2;

  private Globals    globals;
  private PeopleList plist;   // This is the list that contains us

  public  boolean    isBlank = false;

  public  int     id;
  public  int     index;        // index into table (for list box use)
  public  boolean hidden = false;
  public  String  firstName;
  public  String  lastName;
  public  String  title;
  public  String  nameSuffix;
  public  String  fullName;
  public  String  details;
  public  String  lifeDates;
  public  int     sex;
  public  int     father;
  public  int     mother;
  public  Vector  families = new Vector(3,3);

  public Person    next;              // for transient linked lists.

  public boolean isComplete = false;
  public boolean drawMe     = false;

  public Person (Globals    inGlobals,
                 PeopleList inPList,
                 int        inIndex)
  {
    globals = inGlobals;
    plist   = inPList;
    index   = inIndex;
    father  = mother = -1;
    sex     = male;  // assume male unless told otherwise
  }

  public void addFamily (int index)
  {
    families.addElement(new Integer(index));
  }

  public Family getFamily (int index)
  {
    Family  result = null;
    Integer vectValue;

    if (families != null)
    {
      try
      {
        vectValue = (Integer)families.elementAt(index);
        if (vectValue != null)
        {
          result = plist.getFamily(vectValue.intValue());
        }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
      }
    }

    return result;
  }

  public void complete()
  {
    if ((firstName != null) && (lastName != null))
      fullName = firstName + " " + lastName;
    else if ((firstName == null) && (lastName != null))
      fullName = lastName;
    else if ((firstName != null) && (lastName == null))
      fullName = firstName;
    else
      fullName = "no name";

    if (details != null)
      details = fullName + "\n" + details;
    else if (lifeDates != null)
      details = fullName + "\n" + lifeDates;
    else
      details = fullName;

    isComplete = true;
  }

  //+- method shouldDrawChildren -----------------------------------------+
  //|                                                                     |
  //|  Syntax:                                                            |
  //|                                                                     |
  //|    public boolean shouldDrawChildren()                              |
  //|                                                                     |
  //|  Description:                                                       |
  //|                                                                     |
  //|    shouldDrawChildren is called by PeopleList#shouldDraw to see if  |
  //|    any of the children of current center person have been added to  |
  //|    the tree since the last time it was drawn.  It is a recursive    |
  //|    routine that checks the current person and then calls the        |
  //|    shouldDrawChildren on each of it's children.  It only takes one  |
  //|    new person to require the whole tree to be redrawn.  Therefore,  |
  //|    shouldDrawChildren will return true after it finds the first     |
  //|    person that is new to the tree.                                  |
  //|                                                                     |
  //|  Parameters:                                                        |
  //|                                                                     |
  //|    none.                                                            |
  //|                                                                     |
  //|  Return Value:                                                      |
  //|                                                                     |
  //|    Returns true this person or any of it's descendants have been    |
  //|    added to the tree since the last time it was drawn.              |
  //|                                                                     |
  //+---------------------------------------------------------------------+

  public boolean shouldDrawChildren()
  {
    Family family;
    Person child;
    Person spouse;

    if (!drawMe)
    {
      return true;  // this person is new to the tree, so there is no need
                    // to look any farther.
    }

    // Now check spouses and children
    for (int f = 0; (family = getFamily(f)) != null; f++)
    {
      if (sex == male)
        spouse = plist.getPerson(family.mother);
      else
        spouse = plist.getPerson(family.father);

      if (spouse != null)
      {
        if (!spouse.drawMe)
        {
          return true;
        }
      }

      for (int c = 0; (child = family.getChild(c)) != null; c++)
      {
        if (child.shouldDrawChildren())
          return true;
      }
    }

    return false;

  } // end of method shouldDrawChildren

  //+- method shouldDrawParents ------------------------------------------+
  //|                                                                     |
  //|  Syntax:                                                            |
  //|                                                                     |
  //|    public boolean shouldDrawParents()                               |
  //|                                                                     |
  //|  Description:                                                       |
  //|                                                                     |
  //|    shouldDrawParents is called by PeopleList#shouldDraw to see if   |
  //|    any of the parents of current center person have been added to   |
  //|    the tree since the last time it was drawn.  It is a recursive    |
  //|    routine that checks the current person and then calls the        |
  //|    shouldDrawParents on each of it's parents.  It only takes one    |
  //|    new person to require the whole tree to be redrawn.  Therefore,  |
  //|    shouldDrawParents will return true after it finds the first      |
  //|    person that is new to the tree.                                  |
  //|                                                                     |
  //|  Parameters:                                                        |
  //|                                                                     |
  //|    none.                                                            |
  //|                                                                     |
  //|  Return Value:                                                      |
  //|                                                                     |
  //|    Returns true this person or any of it's ancestors have been      |
  //|    added to the tree since the last time it was drawn.              |
  //|                                                                     |
  //+---------------------------------------------------------------------+

  public boolean shouldDrawParents()
  {
    Person p;

    if (!drawMe)
    {
      return true;  // this person is new to the tree, so there is no need
                    // to look any farther.
    }

    if (((p = plist.getPerson(father)) != null) &&
        p.shouldDrawParents())
      return true;

    if ((p = plist.getPerson(mother)) != null)
      return p.shouldDrawParents();

    return false;
  } // shouldDrawParents
} // class Person


//+-- Class ChildEnum --------------------------------------------------------+
//|                                                                           |
//| Syntax:       class ChildEnum                                             |
//|                                                                           |
//| Description:  This class contains data used to enumerate the children of  |
//|               an individual                                               |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

//class ChildEnum
//{
//  private PeopleList plist;
//  private Person     parent;
//  private int        familyIndex = 0;
//  private int        childIndex  = 0;

//  public ChildEnum (PeopleList plist,
//                    Person     parent)
//  {
//    this.plist  = plist;
//    this.parent = parent;
//  }

//  public Person nextChild()
//  {
//    Person result = null;
//    Family family = parent.getFamily(familyIndex);

//    if (family != null)
//    {
//      result = family.getChild(childIndex++);

//      if (result == null)
//      {
//        // Go to the next family
//        childIndex = 0;
//        family = parent.getFamily(++familyIndex);

//        if (family != null)
//          result = family.getChild(childIndex++);
//      }
//    }

//    return result;

//  } // end of method nextChild

//} // end of class ChildEnum


