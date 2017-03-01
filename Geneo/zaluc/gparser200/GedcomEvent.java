//+-- File GedcomEvent.java --------------------------------------------------+
//|                                                                           |
//| Description:  This is contains the code for implementing a GedcomEvent.   |
//|               A GedcomEvent contains the information for an event from    |
//|               the gedcom file.                                            |
//|                                                                           |
//| Classes:      class GedcomEvent                                           |
//|                                                                           |
//|---------------------------------------------------------------------------+

package zaluc.gparser200;

import java.util.*;

import zaluc.utils.*;


//+-- Class GedcomEvent ------------------------------------------------------+
//|                                                                           |
//| Syntax:       class GedcomEvent implements SortableObject                 |
//|                                                                           |
//| Description:  A GedcomEvent contains the information for an event from    |
//|               the gedcom file.  GedcomEvent's are sorted by date.         |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class GedcomEvent implements SortableObject
{
  public SortableHandle listHandle = new SortableHandle (this);

  private String         type;
  private String         value;
  private GedcomDate     date;
  private String         place;

  public GedcomEvent()
  {
  }

  public GedcomEvent(GedcomEvent source)
  {
    if (source.type != null)
      type  = new String(source.type);
    if (source.value != null)
      value = new String(source.value);
    if (source.date != null)
      date  = new GedcomDate(source.date);
    if (source.place != null)
      place = new String(source.place);
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public void setDate(String date)
  {
    this.date = new GedcomDate(date);
  }

  public void setPlace(String place)
  {
    this.place = place;
  }

  public String getValue()
  {
    return value;
  }

  public String getType()
  {
    return type;
  }

  public String getDate()
  {
    if (date != null)
      return date.toString();
    else
      return null;
  }

  public boolean hasYear()
  {
    if (date != null)
      return date.hasYear();
    else
      return false;
  }

  public int getYear()
  {
    if (date != null)
      return date.getYear();
    else
      return 0;
  }

  public String getPlace()
  {
    return place;
  }

  public int compareTo (SortableObject soOther, int compareToken)
  {
    GedcomEvent other = (GedcomEvent) soOther;

    if (date != null)
    {
      if (other != null)
        return date.compareTo(other.date);
      else
        return -1;
    }
    else
      if (other.date != null)
        return -1;    // null dates after real dates
      else
        return 0;    // both dates are null
  }

  public String toString()
  {
    String ret = "";

    if (value != null)
      ret += value;
    if (date != null)
    {
      if (value != null)
        ret += ", ";
      ret += getDate();
    }
    if (place != null)
    {
      if ((value != null) || (date != null))
        ret += ", ";
      ret += place;
    }
    if (type != null)
    {
      if ((value != null) || (date != null) || (place != null))
        ret = type + ": " + ret;
      else
        ret = type;
    }
    if (ret.length() == 0)
      ret = null;

    return ret;
  }
}
