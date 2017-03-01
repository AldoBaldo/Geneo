package zaluc.geneo;

import java.awt.*;
import java.lang.*;
import java.util.*;

//+-- Class Family -----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class Family                                                |
//|                                                                           |
//| Description:  The Family class is a connector class that maintains a link |
//|               link between a man, a women, and their children.            |
//|                                                                           |
//| Methods:      Family : constructor.                                       |
//|                                                                           |
//|---------------------------------------------------------------------------+

class Family
{
  Globals    globals;
  PeopleList plist;
  int        index;

  boolean isComplete;
  int     father;
  int     mother;
  boolean hasChildren;
  Vector  children;

  public Family (Globals    inGlobals,
                 PeopleList inPList,
                 int        inIndex)
  {
    globals     = inGlobals;
    plist       = inPList;
    index       = inIndex;
    isComplete  = false;
    father      = mother= -1;
    hasChildren = false;
    children    = new Vector(6,6);
  }

  public void complete()
  {
//    if (father == -1)
//    {
//      father = plist.appendPerson();
//    }
//    if (mother == -1)
//    {
//      mother = plist.appendPerson();
//    }

    isComplete = true;
  }

  public void addChild (int index)
  {
    Integer vectValue = new Integer(index);
    children.addElement(vectValue);
    hasChildren = true;
  }

  public Person getChild (int index)
  {
    Person  result = null;
    Integer vectValue;

    if (children != null)
    {
      try
      {
        vectValue = (Integer)children.elementAt(index);
        if (vectValue != null)
          result = plist.getPerson(vectValue.intValue());
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
      }
    }

    return result;
  }
}
