package zaluc.gparser200;

import java.awt.*;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.*;

import zaluc.utils.*;


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
//| Methods:      public            Person             (Params     inParams,  |
//|                                                     PeopleList inPList)   |
//|                                                                           |
//|               public int        compareTo          (Person other)         |
//|                                                                           |
//|                                                                           |
//|---------------------------------------------------------------------------+

class Person implements SortableObject
{
  public static final int unknown = 0;
  public static final int male    = 1;
  public static final int female  = 2;

  public static final int compareAlphabetically = 1;
  public static final int compareByAge          = 2;

  public  Person         next;
  public  SortableHandle mainListHandle   = new SortableHandle (this);
  public  SortableHandle familyListHandle = new SortableHandle (this);
  public  int            id;
  public  boolean        includeDetails = true;
  public  String         firstName;
  public  String         lastName;
  public  String         title;
  public  String         nameSuffix;
  public  String         fullName;
  public  GedcomEvent    birth;
  public  GedcomEvent    death;
  private SortableVector events;
  private String         lifeDates;
  public  int            sex;
  public  boolean        hide = false;
  public  Person         father;
  public  Person         mother;
  public  Family         childOfFamily;
  public  SortableVector families = new SortableVector(3, 3, 0);
  public  int            preferredFamily = -1;

  private String    details;

  public boolean   written = false;

  public void addNote (String note)
  {
  }

  public void addFamily (Family family)
  {
    if (sex == male)
      families.addElement(family.husbandsListHandle);
    else
      families.addElement(family.wifesListHandle);
  }

  public void addEvent (GedcomEvent event)
  {
    if (events == null)
      events = new SortableVector(10, 10, 0);
    events.addElement(event.listHandle);
  }

  public GedcomEvent getEvent (int index)
  {
    if (index < events.size())
      return (GedcomEvent) ((SortableHandle) events.elementAt(index)) .getContainer();
    else
      return null;
  }

  public void sortEvents ()
  {
    if (events != null)
      events.sort();
  }

  public String getDetails()
  {
    if (details == null)
    {
      String temp;
      int    i, count;

//      details = fullName;

      if (events != null)
      {
        sortEvents();

        count = events.size();
        for (i = 0; i < count; i++)
        {
          temp = getEvent(i).toString();
          if (temp != null)
          {
            if (details == null)
              details = temp;
            else
              details += "\n" + temp;
          }
        }
      }
    }

    return details;
  }

  public String getLifeDates()
  {
    if (lifeDates == null)
    {
      // Combine the birth and death dates into a single string
      //old way: lifeDates = ((birth != null) ? birth.getDate() : "???") + " - " +
      //old way:              ((death != null) ? death.getDate() : "???");

      // New way:

      if (((birth != null) && birth.hasYear()) &&
          ((death == null) || !death.hasYear()))
      {
        // We have a birth date but no death date
        lifeDates = "b. " + birth.getYear();
      }
      else if (((death != null) && death.hasYear()) &&
               ((birth == null) || !birth.hasYear()))
      {
        // We have a death date but no birth date
        lifeDates = "d. " + death.getYear();
      }
      else if ((birth != null) && birth.hasYear() &&
               (death != null) && death.hasYear())
      {
        // We have both dates
        lifeDates = birth.getYear() + " - " + death.getYear();
      }
      // else, we have no dates, just return null
    }

    return lifeDates;
  }

  //+-----------------------------------------+
  //| Routine for implementing SortableObject |
  //+-----------------------------------------+

  // Compares two people for sorting purposes.
  // returns <  0 if this is less than other
  // returns == 0 if this == other
  // returns >  0 if this is greater than other

  public int compareTo (SortableObject soOther, int compareToken)
  {
    Person  other = (Person)soOther;
    int     ret = 0;

    if (compareToken == compareAlphabetically)
    {
      // Compare last names:
      if ((lastName == null) && (other.lastName == null))
        ret = 0;
      else if (lastName == null)
        ret = 1;
      else if (other.lastName == null)
        ret = -1;
      else
        ret = lastName.compareTo(other.lastName);

      if (ret != 0)
        return ret;

      // Last names are identical, compare first names
      if ((firstName == null) && (other.firstName == null))
        ret = 0;
      else if (firstName == null)
        ret = 1;
      else if (other.firstName == null)
        ret = -1;
      else
        ret = firstName.compareTo(other.firstName);

      if (ret != 0)
        return ret;

    } /* end compare alphabetically */

    /* If we're comparing alphabetically and the the names are the same, */
    /* or if we're comparing by age:                                     */

    if ((birth == null) &&
        ((other == null) || (other.birth == null)))
    {
      ret = 0;
    }
    else if (birth == null)
    {
      ret = 1;
    }
    else if ((other == null) ||
             (other.birth == null))
    {
      ret = -1;
    }
    else
    {
      ret = birth.compareTo(other.birth, compareToken);
    }

    return ret;
  }

  public void write(Record record) throws IOException
  {
    String      data;
    Enumeration enum;
    Family      family;

    if (!written)
    {
      written = true;
      record.write(Record.PERSON, mainListHandle.getIndex());
      record.write(Record.ID    , id);
      record.write(Record.SEX   , sex);
      if (hide)               record.write(Record.HIDE);
      if (firstName  != null) record.write(Record.FIRST_NAME , firstName);
      if (lastName   != null) record.write(Record.LAST_NAME  , lastName);
      if (title      != null) record.write(Record.TITLE      , title);
      if (nameSuffix != null) record.write(Record.NAME_SUFFIX, nameSuffix);
      if (includeDetails)
      {
        data = getDetails();
        if (data       != null) record.write(Record.DETAILS    , data);
      }
      data = getLifeDates();
      if (data       != null) record.write(Record.LIFE_DATES , data);
      if (father     != null) record.write(Record.FATHER     , father.mainListHandle.getIndex());
      if (mother     != null) record.write(Record.MOTHER     , mother.mainListHandle.getIndex());
      enum = families.elements();
      while (enum.hasMoreElements())
      {
        family = (Family) ((SortableHandle)enum.nextElement()).getContainer();
        record.write(Record.FAMILY_LINK, family.index);
      }
    }
  }

  public String toHtml ()
  {
    String      ret = null;
    String      data;
    Enumeration enumFamily;
    Enumeration enumChildren;
    Family      family;
    Person      child;
    DecimalFormat format = new DecimalFormat("0000");
    String        idString = null;

    ret = "<a href='javascript:launch(" + id +
          ");'><img src=../../gifs/smaltree.gif border=0 alt='View tree for: '></a>&nbsp;" +
          fullName + " (" + id + ")</h3>\n";
    ret += "   <ul>\n";

    if (father != null)
    {
      idString = format.format(father.id);
      ret += "      <p>Father: <a href=UHP-" + idString + ".html>" + father.fullName + "</a></p>\n";
    }

    if (mother != null)
    {
      idString = format.format(mother.id);
      ret += "      <p>Mother: <a href=UHP-" + idString + ".html>" + mother.fullName + "</a></p>\n";
    }

    enumFamily = families.elements();
    while (enumFamily.hasMoreElements())
    {
      family = (Family) ((SortableHandle)enumFamily.nextElement()).getContainer();

      if ((sex == male) &&
          (family.mother != null))
      {
        idString = format.format(family.mother.id);
        ret += "      <p>Spouse: <a href=UHP-" + idString + ".html>" + family.mother.fullName + "</a></p>\n";
      }
      if ((sex == female) &&
          (family.father != null))
      {
        idString = format.format(family.father.id);
        ret += "      <p>Spouse: <a href=UHP-" + idString + ".html>" + family.father.fullName + "</a></p>\n";
      }

      ret += "      <ul>\n";
      enumChildren = family.children.elements();
      while (enumChildren.hasMoreElements())
      {
        child = (Person) ((SortableHandle)enumChildren.nextElement()).getContainer();

        idString = format.format(child.id);
        ret += "         <p>Child: <a href=UHP-" + idString + ".html>" + child.fullName + "</a></p>\n";
      }
      ret += "      </ul>\n";
    }
    ret += "   </ul>\n";

    return ret;
  }

  public Person append(Person next)
  {
    if (next != null)
    {
      this.next = next;
      next.next = null;
      return next;
    }
    return this;
  }

//  public Person nextSib(Person curSib)
//  {
//    // This is confusing because 'F' could stand for either "Father" or
//    // "Female" and 'M' could be either "Mother" or "Male".  In this case
//    // I'm using "Father" and "Mother".
//    return (sex == male) ? curSib.nextSibF : curSib.nextSibM;
//  }
}
