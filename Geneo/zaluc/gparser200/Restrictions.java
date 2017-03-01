package zaluc.gparser200;

import java.lang.*;
import java.io.*;
import java.util.*;

//+-- Class Restrictions -----------------------------------------------------+
//|                                                                           |
//| Syntax:       class Restrictions                                          |
//|                                                                           |
//| Description:  The Restrictions class is responsible for enforcing the     |
//|               restrictions on who can go into the data file and who can't |
//|                                                                           |
//| Methods:                                                                  |
//|---------------------------------------------------------------------------+

class Restrictions
{
  boolean noAlives = false; // Do we show alive people?
  int     cutoffYear = 0;   // People born before this year are considered to be dead
  int     incCount = 0;     // Count of entries in include list
  int     incList[];        // List of people to include
  int     excCount = 0;     // Count of entries in exclude list
  int     excList[];        // List of people to exclude

  public Restrictions(int count)
  {
    if (count > 0)
    {
      incList = new int[count];
      excList = new int[count];
    }
  }

  public boolean valid()
  {
    return (cutoffYear != 0) ||
           ((incCount == 0) && (excCount == 0));
  }

  public void setCutoff(int cutoffYear)
  {
    this.noAlives   = true;
    this.cutoffYear = cutoffYear;
  }

  public void include (int incID)
  {
    incList[incCount++] = incID;
  }

  public void exclude (int excID)
  {
    excList[excCount++] = excID;
  }

  public boolean hide(Person person)
  {
    boolean ret;

    if (noAlives)
    {
      if ((person.birth != null) && (person.birth.getYear() >= cutoffYear))
      {
        // The person is alive and we will reject him/her unless
        // he/she is in the include list.
        ret = true;
        for (int i = 0; i < incCount; i++)
        {
          if (person.id == incList[i])
          {
            ret = false;
            break;
          }
        }
      }
      else
      {
        // The person is dead and we will include him/her unless
        // he/she is in the exclude list.
        ret = false;
        for (int i = 0; i < excCount; i++)
        {
          if (person.id == excList[i])
          {
            ret = true;
            break;
          }
        }
      }
    }
    else
      ret = false;

    return ret;
  }
}
