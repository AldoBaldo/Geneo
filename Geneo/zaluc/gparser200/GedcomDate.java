//+-- File GedcomDate.java ---------------------------------------------------+
//|                                                                           |
//| Description:  This is contains the code for implementing a GedcomDate.    |
//|                                                                           |
//| Classes:      class GedcomDate                                            |
//|                                                                           |
//|---------------------------------------------------------------------------+

package zaluc.gparser200;

import java.util.*;

//+-- Class GedcomDate -------------------------------------------------------+
//|                                                                           |
//| Syntax:       class GedcomDate implements SortableObject                  |
//|                                                                           |
//| Description:  A GedcomDate contains code for storing and comparing dates. |
//|               Dates can also be retrieved in string format.               |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class GedcomDate
{
  private String date;
  private int    day;
  private int    month;
  private int    year;

  private static final int UNUSED = 3000;

  private static final int DAY_FOUND   = 1;
  private static final int MONTH_FOUND = 2;
  private static final int YEAR_FOUND  = 4;
  private static final int ALL_FOUND   = 7;
  private static final int NONE_FOUND  = 0;

  public GedcomDate(GedcomDate source)
  {
    if (source.date != null)
      date  = new String(source.date);
    day   = source.day;
    month = source.month;
    year  = source.year;
  }

  public GedcomDate(String date)
  {
    StringTokenizer t;
    String          curToken;
    String          intStr;
    int             found = NONE_FOUND;
    int             tempInt;

    this.date = date;
    day = month = year = UNUSED;    // Initialize these values to real large
                                    // so that, in later compares, unused
                                    // values will appear later than used values.

    // Now break out the year, day and month into integers for easy use
    // in sorting.

    if (date != null)
    {
      t = new StringTokenizer(date, "\n ", false);

      while (((found & ALL_FOUND) != ALL_FOUND) &&
             t.hasMoreTokens())
      {
        curToken = t.nextToken();
        intStr  = getInt(curToken);

        if (intStr != null)
        {
          // It's a year or a day, find out which and save it in the
          // appropriate spot.
          tempInt = Integer.parseInt(intStr);

          if (((found & YEAR_FOUND) == 0) &&
              (tempInt > 31))
          {
            found |= YEAR_FOUND;
            year = tempInt;
          }
          else if ((found & DAY_FOUND) == 0)
          {
            found |= DAY_FOUND;
            day  = tempInt;
          }
        }
        else
        {
          // It must be a month, now we must give a number to the month.
          // Have we already seen a month?  We don't want to do this twice.
          if ((found & MONTH_FOUND) == 0)
          {
            curToken = curToken.toLowerCase();
            if (curToken.startsWith("jan"))
            {
              found |= MONTH_FOUND;
              month = 0;
            }
            else if (curToken.startsWith("feb"))
            {
              found |= MONTH_FOUND;
              month = 1;
            }
            else if (curToken.startsWith("mar"))
            {
              found |= MONTH_FOUND;
              month = 2;
            }
            else if (curToken.startsWith("apr"))
            {
              found |= MONTH_FOUND;
              month = 3;
            }
            else if (curToken.startsWith("may"))
            {
              found |= MONTH_FOUND;
              month = 4;
            }
            else if (curToken.startsWith("jun"))
            {
              found |= MONTH_FOUND;
              month = 5;
            }
            else if (curToken.startsWith("jul"))
            {
              found |= MONTH_FOUND;
              month = 6;
            }
            else if (curToken.startsWith("aug"))
            {
              found |= MONTH_FOUND;
              month = 7;
            }
            else if (curToken.startsWith("sep"))
            {
              found |= MONTH_FOUND;
              month = 8;
            }
            else if (curToken.startsWith("oct"))
            {
              found |= MONTH_FOUND;
              month = 9;
            }
            else if (curToken.startsWith("nov"))
            {
              found |= MONTH_FOUND;
              month = 10;
            }
            else if (curToken.startsWith("dec"))
            {
              found |= MONTH_FOUND;
              month = 11;
            }
//            else
//              System.out.println("Invalid month: <" + curToken + ">");
          }
        }
      }
    }
//    System.out.println("Date <" + date + "> resolved to day = " + day + ", month = " + month + ", year = " + year);
  }

  public int compareTo(GedcomDate other)
  {
    if (other != null)
    {
      if (year != other.year)
        return year - other.year;
      if (month != other.month)
        return month - other.month;
      return day - other.day;
    }
    else
      return -1;  // null years always come after valid years
  }

  public boolean hasYear()
  {
    if (year != UNUSED)
      return true;
    else
      return false;
  }

  public int getYear()
  {
    if (year != UNUSED)
      return year;
    else
      return 0;
  }

  public String toString()
  {
    return (date != null) ? date : "???";
  }

  private String getInt(String token)
  {
    String ret = null;
    char c[] = new char[4];
    int  s,d;

    s = d = 0;

    try
    {
      while ((d < 4) && (s < token.length()))
      {
        c[d] = token.charAt(s);
        if ((c[d] >= '0') && (c[d] <= '9'))
        {
          d++;
        }
        s++;
      }
    }
    catch (StringIndexOutOfBoundsException e)
    {
      // Do nothing, this just means we reached the end of the string
    }

    if (d > 0)
    {
      ret = new String(c, 0, d);
    }

    return ret;
  }
}
