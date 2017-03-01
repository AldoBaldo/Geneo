package zaluc.gparser200;

import java.awt.*;
import java.lang.*;
import java.io.*;
import java.util.*;

import zaluc.utils.*;

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
//|                                                                           |
//|---------------------------------------------------------------------------+

class PeopleList
{
  private SortableVector peopleVect;
  private Vector         familyVect;
  private int            familyCount;

  public PeopleList ()
  {
    peopleVect = new SortableVector (100, 100, Person.compareAlphabetically);
    familyVect = new Vector (100, 100);
  }

  //+--------------------------------------------------------------+
  //| Routines for adding and getting people to and from the list  |
  //+--------------------------------------------------------------+

  public void setPerson(Person person, int index)
  {
    if (index >= peopleVect.size())
      peopleVect.setSize(index+1);
    peopleVect.setElementAt(person.mainListHandle, index);
  }

  public void setFamily(Family family)
  {
    familyVect.addElement(family);
    family.index = familyCount++;
  }

  public Person getPerson(int index)
  {
    if (index < peopleVect.size())
      return (Person) ((SortableHandle) peopleVect.elementAt(index)) .getContainer();
    else
      return null;
  }

  public Family getFamily(int index)
  {
    if (index < familyVect.size())
      return (Family) familyVect.elementAt(index);
    else
      return null;
  }

  public void sort()
  {
    int    i;
    Person person;
    Family family;

    /* Sort the people in the list */
    peopleVect.sort();

    /* For each family, sort the children that they have */
    for (i = 0; i < familyCount; i++)
      if ((family = getFamily(i)) != null)
        family.children.sort();

    /* For each person, sort the families that they have */
    for (i = 0; i < peopleVect.size(); i++)
      if ((person = getPerson(i)) != null)
        person.families.sort();
  }

  public int getCount()
  {
    return peopleVect.size();
  }

  public void writeFrom(Person startPerson,
                        Record record) throws IOException
  {
    Person      person;
    Family      family;
    Person      childListHead;
    Person      childListTail;
    Person      parentListHead;
    Person      parentListTail;
    Enumeration enumFamily;
    Enumeration enumChildren;
    int         peopleCount = peopleVect.size();
    int         i;

    // Clear the written flags of all individuals
    for (i = 0; i < peopleCount; i++)
      if ((person = getPerson(i)) != null)
        person.written = false;

    // Clear the written flags of all families
    for (i = 0; i < familyCount; i++)
      if ((family = getFamily(i)) != null)
        family.written = false;

    // Write the people count to the file
    record.write(Record.PEOPLE_COUNT, peopleCount);
    record.write(Record.FAMILY_COUNT, familyCount);

    // Write the parents and children of the start person out in
    // breadth first order.  Do 10 parents, then 10 children, then
    // 10 parents, and so on until all parents and children are written.
    parentListHead = parentListTail = startPerson;
    childListHead  = childListTail  = startPerson;   // startPerson.write will be called twice, but it's smart enough to just write itself once.
    parentListHead.next = null;
    childListHead.next = null;
    while((parentListHead != null) ||
          (childListHead  != null))
    {
      if (parentListHead != null)
      {
        if (!parentListHead.written)
        {
          // Add parents to end of list, then write parentListHead
          parentListTail = parentListTail.append(parentListHead.father);
          parentListTail = parentListTail.append(parentListHead.mother);
          parentListHead.write(record);
        }
        parentListHead = parentListHead.next;
      }
      if (childListHead != null)
      {
        // We need to get the current person, each spouse, and each child
        // written, along with the family record that ties them together.
        // By writing both the mother and father of each family, we get
        // both the current person and the spouse.  Then we add the
        // children to the end of the list.
        if (!childListHead.written)
        {
          enumFamily = childListHead.families.elements();
          while (enumFamily.hasMoreElements())
          {
            family = (Family) enumFamily.nextElement();
            family.write(record);
            if (family.mother != null)
              family.mother.write(record);
            if (family.father != null)
              family.father.write(record);
            enumChildren = family.children.elements();
            while (enumChildren.hasMoreElements())
            {
              person = (Person) enumChildren.nextElement();
              childListTail = childListTail.append(person);
            }
          }
          childListHead.write(record);
        }
        childListHead = childListHead.next;
      }
    }

    // Now write the rest of the people
    for (i = 0; i < peopleCount; i++)
      if ((person = getPerson(i)) != null)
        person.write(record);

    // Now write the rest of the families
    for (i = 0; i < familyCount; i++)
      if ((family = getFamily(i)) != null)
        family.write(record);
  }
}
