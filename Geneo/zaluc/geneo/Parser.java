package zaluc.geneo;

import java.io.*;
import java.net.*;
import java.util.zip.GZIPInputStream;

import zaluc.gparser200.Record;
import zaluc.gparser200.Record100;

//+-- Class Parser -----------------------------------------------------------+
//|                                                                           |
//| Syntax:       public class Parser                                         |
//|                                                                           |
//| Description:  The Parser class is responsible for parsing a gedcom file.  |
//|               It pulls a file name from the globals, and a ParserUpdates  |
//|               object, and updates the ParserUpdates object with the       |
//|               progress of the parsing and, ultimately, with a sorted list |
//|               of Person objects.                                          |
//|                                                                           |
//| Methods:      public void parseFile (Globals       inGlobals,             |
//|                                      ParserUpdates inCaller)              |
//|                                                                           |
//|---------------------------------------------------------------------------+

public class Parser
{
  static final int parsingFamily = 1;
  static final int parsingPerson = 2;

  public static void parseFile (Globals       globals,
                                ParserUpdates caller)
  {
    DataInputStream  d;
    String           fileNameBase;
    PeopleList       people = null;
    Person           curPerson = null;
    Family           curFamily = null;
    Person           firstPerson = null;
    int              i;
    Record           record;
    String           password = null;
    int              recordCount = 0;
    int              peopleCount = 0;
    int              familyCount = 0;
    int              context;

    int lastIndex = globals.sourceFile.lastIndexOf(".gen");
    if (lastIndex > 0)
      fileNameBase = globals.sourceFile.substring(0, lastIndex);
    else
      fileNameBase = globals.sourceFile;

    try
    {
      URL    url;
      String token;
      int    recType;
      int    version;

      try
      {
        // First try to find and open the compressed data file.  If anything
        // goes wrong, throw an exception and try to open the non-compressed
        // file.
        url    = new URL (globals.documentBase, fileNameBase + ".gec");
        d      = new DataInputStream (new GZIPInputStream (url.openStream()));
        record = new Record(d);
      }
      catch (Exception e)
      {
        // If anything goes wrong trying to get the compressed file, try
        // getting the uncompressed file.
        url    = new URL (globals.documentBase, fileNameBase + ".gen");
        d      = new DataInputStream (url.openStream());
        record = new Record(d);
      }

      caller.notifyFileName(url.toString());

      if (globals.dumpStats)
      {
        System.out.println("Before loading tree: total memory: "
                           + Runtime.getRuntime().totalMemory()
                           + " bytes, free memory: "
                           + Runtime.getRuntime().freeMemory()
                           + " bytes");
      }

      if ((recType = record.readRecordType()) == Record.VERSION)
      {
        switch (record.readIntValue())
        {
          case Record.VERSION_100:
            // Create a version 100 version of the record reader.
            record = new Record100(record);
            // Fall through to next case

          case Record.VERSION_200:
            while ((recType = record.readRecordType()) != Record.PERSON)
            {
              switch (recType)
              {
                case Record.PASSWORD:
                {
                  String          pwdFile = fileNameBase + ".gpw";
                  URL             pwdUrl  = new URL (globals.documentBase, pwdFile);
                  DataInputStream pwdStream;
                  Record          pwdRecord;

                  if (pwdUrl != null)
                  {
                    pwdStream = new DataInputStream (pwdUrl.openStream());
                    if (pwdStream != null)
                    {
                      pwdRecord = new Record (pwdStream);
                      if (pwdRecord.readRecordType() == Record.PASSWORD)
                      {
                        password = pwdRecord.readStrValue();
                      }
                      else
                      {
                        System.out.println("Invalid format for file " + pwdUrl.toString());
                      }
                    }
                    else
                    {
                      System.out.println("Couldn't find file " + pwdUrl.toString());
                    }
                  }
                  break;
                }
                case Record.PEOPLE_COUNT:
                  peopleCount = record.readIntValue();
                  if (globals.dumpStats)
                  {
                    System.out.println(url.toString() + " contains " + peopleCount + " individuals");
                  }
                  break;
                case Record.FAMILY_COUNT:
                  familyCount = record.readIntValue();
                  people = new PeopleList(globals, peopleCount, familyCount);
                  caller.notifySetup (people, password, peopleCount + familyCount);
                  break;
                default:
                  System.out.println("Unknown record type (" + recType + ") seen");
              }
            }

            if (people != null)
            {
              if (recType == Record.PERSON)
              {
                // Make sure we get a person record first, and
                // then make that person the center person.
                curPerson = people.newPerson(record.readIntValue());
                context = parsingPerson;

                // Save the first person to set as center person if the
                // center person doesn't get set during parsing.
                firstPerson = curPerson;

                // Only set this person as the center person if the user
                // has not overridden the original center person with
                // a new one.
                if (globals.primary == -1)
                {
                  people.setCenterPerson(curPerson);
                }

                // Loop until an error or EOFException occurrs
                while (globals.statusCode == Globals.statusOK)
                {
                  switch(record.readRecordType())
                  {
                    case Record.PERSON:
                      if (curPerson != null)
                      {
                        curPerson.complete();
                        curPerson = null;
                      }
                      if (curFamily != null)
                      {
                        curFamily.complete();
                        curFamily = null;
                      }
                      context = parsingPerson;
                      caller.updateRecordCount(++recordCount);
                      curPerson = people.newPerson(record.readIntValue());
                      break;
                    case Record.ID:
                      curPerson.id = record.readIntValue();
                      if (curPerson.id == globals.primary)
                      {
                        people.setCenterPerson (curPerson);
                      }
                      break;
                    case Record.HIDE:
                      curPerson.hidden = true;
                      break;
                    case Record.FIRST_NAME:
                      curPerson.firstName = record.readStrValue();
                      break;
                    case Record.LAST_NAME:
                      curPerson.lastName = record.readStrValue();
                      break;
                    case Record.TITLE:
                      curPerson.title = record.readStrValue();
                      break;
                    case Record.NAME_SUFFIX:
                      curPerson.nameSuffix = record.readStrValue();
                      break;
                    case Record.DETAILS:
                      curPerson.details = record.readStrValue();
                      break;
                    case Record.LIFE_DATES:
                      curPerson.lifeDates = record.readStrValue();
                      break;
                    case Record.SEX:
                      curPerson.sex = record.readIntValue();
                      break;
                    case Record.FATHER:
                      if (context == parsingPerson)
                        curPerson.father = record.readIntValue();
                      else
                        curFamily.father = record.readIntValue();
                      break;
                    case Record.MOTHER:
                      if (context == parsingPerson)
                        curPerson.mother = record.readIntValue();
                      else
                        curFamily.mother = record.readIntValue();
                      break;
                    case Record.FAMILY_LINK:
                      curPerson.addFamily(record.readIntValue());
                      break;
                    case Record.FAMILY:
                      if (curPerson != null)
                      {
                        curPerson.complete();
                        curPerson = null;
                      }
                      if (curFamily != null)
                      {
                        curFamily.complete();
                        curFamily = null;
                      }
                      context = parsingFamily;
                      caller.updateRecordCount(++recordCount);
                      curFamily = people.newFamily(record.readIntValue());
                      break;
                    case Record.CHILD:
                      curFamily.addChild(record.readIntValue());
                      break;
                    default:
                      globals.statusCode = Globals.statusError;
                      globals.statusDesc += "Invalid Data File: " + url.toString();
                  }
                }
              } // If record type is PERSON
              else
              {
                globals.statusCode = Globals.statusError;
                globals.statusDesc += "Invalid Data File: " + url.toString();
              }
            } // If people != null
            else
            {
              globals.statusCode = Globals.statusError;
              globals.statusDesc += "Invalid Data File: " + url.toString();
            }
            break;

          default:
            globals.statusCode = Globals.statusError;
            globals.statusDesc += "Invalid Data File: " + url.toString();
        } // switch version
      } // If record type is VERSION
      else
      {
        globals.statusCode = Globals.statusError;
        globals.statusDesc += "Invalid Data File: " + url.toString();
      }
    }
    catch (EOFException e)
    {
      // This is the normal way to terminate.
      if (curPerson != null)
        curPerson.complete();
      if (curFamily != null)
        curFamily.complete();

      if (firstPerson != null)
      {
        // Make the first person in the file the center person
        // if one hasn't been set yet.
        if (people.getCenterPerson() == null)
          people.setCenterPerson(firstPerson);
      }
      else
      {
        globals.statusCode = Globals.statusError;
        globals.statusDesc += "This tree contains no people.";
      }

      caller.updateRecordCount(++recordCount);
    }
    catch (FileNotFoundException e)
    {
      System.out.println("parseFile: File Not Found: " + e.getMessage());
      e.printStackTrace();
      globals.statusCode = Globals.statusError;
      globals.statusDesc += "Data file could not be found: " + e.getMessage();
    }
    catch (IOException e)
    {
      System.out.println("parseFile: IO Exception: " + e.getMessage());
      e.printStackTrace();
      globals.statusCode = Globals.statusError;
      //globals.statusDesc += "parseFile: IO Exception: " + e.getMessage();
      globals.statusDesc += "This tree is currently password protected.";
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      System.out.println("parseFile: ArrayIndexOutOfBoundsException: " + e.getMessage());
      e.printStackTrace();
      globals.statusCode = Globals.statusError;
      globals.statusDesc += "parseFile: ArrayIndexOutOfBoundsException: " + e.getMessage();
    }
    catch (NullPointerException e)
    {
      System.out.println("parseFile: NullPointerException: " + e.getMessage());
      e.printStackTrace();
      globals.statusCode = Globals.statusError;
      globals.statusDesc += "parseFile: NullPointerException: " + e.getMessage();
    }
    catch (NumberFormatException e)
    {
      System.out.println("parseFile: NumberFormatException: " + e.getMessage());
      e.printStackTrace();
      globals.statusCode = Globals.statusError;
      globals.statusDesc += "parseFile: NumberFormatException: " + e.getMessage();
    }
    catch (Exception e)
    {
      System.out.println("parseFile: Exception: " + e.getMessage());
      e.printStackTrace();
      globals.statusCode = Globals.statusError;
      globals.statusDesc += "parseFile: Exception: " + e.getMessage();
    }
    finally
    {
      if (globals.dumpStats)
      {
        System.out.println("After loading tree: total memory: "
                           + Runtime.getRuntime().totalMemory()
                           + " bytes, free memory: "
                           + Runtime.getRuntime().freeMemory()
                           + " bytes");
      }
      caller.notifyDone ();
    }
  }
}
