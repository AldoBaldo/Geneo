package zaluc.gparser200;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.text.*;

import zaluc.utils.*;

//+-- Class Parser -----------------------------------------------------------+
//|                                                                           |
//| Syntax:       public class Parser                                         |
//|                                                                           |
//| Description:  The Parser class is responsible for parsing a gedcom file.  |
//|               It pulls a file name from the params, and a ParserUpdates   |
//|               object, and updates the ParserUpdates object with the       |
//|               progress of the parsing and, ultimately, with a sorted list |
//|               of Person objects.                                          |
//|                                                                           |
//| Methods:      public void parseFile (Params        inParams,              |
//|                                      ParserUpdates inCaller)              |
//|                                                                           |
//|               void parsePerson      () throws IOException                 |
//|                                                                           |
//|               void parseName        (Person person)                       |
//|                                                                           |
//|               void parseBirth       (Person person) throws IOException    |
//|                                                                           |
//|               void parseDeath       (Person person) throws IOException    |
//|                                                                           |
//|               void parseFamily      () throws IOException                 |
//|                                                                           |
//|               Person sort           (int     start,                       |
//|                                      int     end)                         |
//|                                                                           |
//|---------------------------------------------------------------------------+

public class Parser
{
  static BufferedReader   SrcStream;   // Source stream
  static StringTokenizer  tokenizer;
  static String           curLine;          // Used in error reporting only
  static int              curLineNum = 0;   // Used in error reporting only
  static int              curLevel;
  static PeopleList       people;
  static boolean          createHtmlDetails = false;  // Create HTML details
  static boolean          includeDetails    = true;   // Include details in data file
  static String           password   = null;  // Password
  static boolean          verbose    = false; // used for debug output

  // Return Codes:
  static final int retOkay              = 0;
  static final int retBadParam          = 1;
  static final int retSystemError       = 2;
  static final int retNumberFormatError = 3;
  static final int retUnknownError      = 4;

//  static boolean doDebug = false;

  private static final String paramMsg =  // This message describes the allowable parameters.
  "The following parameters are allowable:                                \n\n" +
  "     filename:      This parameter must be first and must exist.  It is  \n" +
  "                    the name of the gedcom file that is to be parsed.    \n" +
  "                                                                         \n" +
  "     start person:  This parameter must be second and must exist.  It is \n" +
  "                    the gedcom ID number of the person that will         \n" +
  "                    initially be displayed as the center person.         \n" +
  "                                                                         \n" +
  "     The remaining parameters may be entered in any order:               \n" +
  "                                                                         \n" +
  "     D:             This parameter indicates that an individuals details \n" +
  "                    should be placed in an html file in the specified    \n" +
  "                    directory.  If the directory does not exist, it will \n" +
  "                    be created.  No details will be placed in the        \n" +
  "                    datafile.                                            \n" +
  "                                                                         \n" +
  "     S:             This parameter tells the parser to generate a short  \n" +
  "                    data file, i.e. one that doesn't contain any details.\n" +
  "                    It should be used if details are not wanted, or if   \n" +
  "                    details will be provided through an HTML page.       \n" +
  "                                                                         \n" +
  "     Paaaaaaaa:     This parameter indicates a password to be used to    \n" +
  "                    view people who are alive.  It has no meaning if the \n" +
  "                    L parameter is not specified.                        \n" +
  "                                                                         \n" +
  "                    The password may be any length and may contain any   \n" +
  "                    printable characters.                                \n" +
  "                                                                         \n" +
  "     L0000:         This parameter tells the parser how to handle living.\n" +
  "                    A person is considered to be still alive if they do  \n" +
  "                    not have a death record and they were born after a   \n" +
  "                    particular year.  The year is specified as a four    \n" +
  "                    digit number after the 'L'.  For example, \"L1916\"  \n" +
  "                                                                         \n" +
  "                    If a password is specified, living people will be    \n" +
  "                    included in the data but will only be viewable if    \n" +
  "                    the password is given by the user.  If no password   \n" +
  "                    is specified, living people will not be included in  \n" +
  "                    the data and will not be viewable.                   \n" +
  "                                                                         \n" +
  "     I000:          This parameter may exist any number of times after   \n" +
  "                    the previous parameters.  It has no meaning unless   \n" +
  "                    the L0000 parameter is specified.  It indicates      \n" +
  "                    that a particular individual should be included in   \n" +
  "                    the resulting tree even if they are \"alive\" by the \n" +
  "                    preceding criteria.  The number following the 'I'    \n" +
  "                    indicates the gedcom ID number of the individual to  \n" +
  "                    include.  For example \"I25\".                       \n" +
  "                                                                         \n" +
  "     X000:          This parameter may exist any number of times after   \n" +
  "                    the first three parameters.  It has no meaning       \n" +
  "                    unless the L0000 parameter is specified.  It         \n" +
  "                    indicates that a particular individual should be     \n" +
  "                    excluded from the resulting tree even if they are    \n" +
  "                    \"dead\" by the preceding criteria.  The number      \n" +
  "                    following the 'X' indicates the gedcom ID number of  \n" +
  "                    the individual to exclude.  For example \"X25\".   \n\n" +
  "Note that people who have no birth year will be considered to be dead    \n" +
  "and will be included in the resulting file.";

  //+-- Method main ----------------------------------------------------------+
  //|                                                                         |
  //| Syntax:                                                                 |
  //|                                                                         |
  //|   public statc void main(String argv[])                                 |
  //|                                                                         |
  //| Description:                                                            |
  //|                                                                         |
  //|   The parameters that can appear on the command line are:               |
  //|                                                                         |
  //|     filename:      This parameter must be first and must exist.  It is  |
  //|                    the name of the gedcom file that is to be parsed.    |
  //|                                                                         |
  //|     start person:  This parameter must be second and must exist.  It is |
  //|                    the gedcom ID number of the person that will be      |
  //|                    displayed first as the center person.                |
  //|                                                                         |
  //|     D:             This parameter indicates that an individuals details |
  //|                    should be placed in an html file in the specified    |
  //|                    directory.  If the directory does not exist, it will |
  //|                    be created.  No details will be placed in the        |
  //|                    datafile.                                            |
  //|                                                                         |
  //|     S:             This parameter tells the parser to generate a short  |
  //|                    data file, i.e. one that doesn't contain any details.|
  //|                    It should be used if details are not wanted, or if   |
  //|                    details will be provided through an HTML page.       |
  //|                                                                         |
  //|     L0000:         This parameter, if it exists, must be third.  It     |
  //|                    tells the parser not to include living people.       |
  //|                    A person is considered to be still alive if they do  |
  //|                    not have a death record and they were born after a   |
  //|                    particular year.  The year is specified as a four    |
  //|                    digit number after the 'L'.  For example, "L1916"    |
  //|                                                                         |
  //|     I000:          This parameter may exist any number of times after   |
  //|                    the previous parameters.  It has no meaning unless   |
  //|                    the L0000 parameter is specified.  It indicates      |
  //|                    that a particular individual should be included in   |
  //|                    in the resulting tree even if they are "alive" by    |
  //|                    the preceding criteria.  The number following the    |
  //|                    'I' indicates the gedcom ID number of the individual |
  //|                    to include.  For example "I25".                      |
  //|                                                                         |
  //|     X000:          This parameter may exist any number of times after   |
  //|                    the first three parameters.  It has no meaning       |
  //|                    unless the L0000 parameter is specified.  It         |
  //|                    indicates that a particular individual should be     |
  //|                    excluded from the resulting tree even if they are    |
  //|                    "dead" by the preceding criteria.  The number        |
  //|                    following the 'X' indicates the gedcom ID number of  |
  //|                    the individual to exclude.  For example "X25".       |
  //|                                                                         |
  //| Parameters:                                                             |
  //|                                                                         |
  //|   String argv[]:  An array of strings from the command line.  The first |
  //|                   entry must be the filename, the second must be the    |
  //|                   start person.  After that comes a list of L, X or I   |
  //|                   parameters.  These parameters consist of one of the   |
  //|                   above letters, in either upper or lower case,         |
  //|                   followed by a number (with no intervening space).     |
  //|                   The L parameter tells the parser not to               |
  //|                                                                         |
  //|                                                                         |
  //| Returns:                                                                |
  //|                                                                         |
  //|-------------------------------------------------------------------------+
  public static void main(String argv[])
  {
    int          retCode = retOkay; // Return code
    int          i;                 // Loop index
    Restrictions restrict;          // List of restrictions on who will be in the tree
    String       param;             // Temp variable for holding a parameter

    dumpParams (argv);

    try
    {
      if (argv.length >= 2)
      {
        // The first parameter must be the file name.
        String src = argv[0].toLowerCase();
        String dst = src.substring(0, src.lastIndexOf(".ged"));

        // The second parameter must be the start person.
        int    startPersonIndex = Integer.parseInt(argv[1]);
        Person startPerson = null;
        int    count;

        restrict = new Restrictions(argv.length - 3);

        if (argv.length > 2)
        {
          for (i = 2; (retCode == retOkay) && (i < argv.length); i++)
          {
            param = argv[i];
            switch(param.charAt(0))
            {
              case 'd':
              case 'D':
                createHtmlDetails = true;
                includeDetails    = false;
                break;
              case 's':
              case 'S':
                includeDetails = false;
                break;
              case 'p':
              case 'P':
                password = param.substring(1);
                break;
              case 'l':
              case 'L':
                restrict.setCutoff(Integer.parseInt(param.substring(1)));
                break;
              case 'i':
              case 'I':
                restrict.include(Integer.parseInt(param.substring(1)));
                break;
              case 'x':
              case 'X':
                restrict.exclude(Integer.parseInt(param.substring(1)));
                break;
              case 'v':
              case 'V':
                verbose = true;
                break;
              default:
                retCode = retBadParam;
            } // switch param
          } // for 2 to argv.length

          // Verify that the L parameter was specified.  The rest of these
          // parameters have no meaning without it.
          if (!restrict.valid())
          {
            System.out.println ("No L0000 parameter was specified");
            retCode = retBadParam;
          }

        } // if argv.length > 2

        if ((retCode == retOkay) && parseFile(src, restrict))
        {
          count = people.getCount();
          for (i = 0; i < count; i++)
          {
            if (people.getPerson(i).id == startPersonIndex)
            {
              startPerson = people.getPerson(i);
              break;
            }
          }

          if (startPerson != null)
          {
            writeFile(dst, startPerson);
          }
          else
          {
            System.out.println("Could not find start person " + startPersonIndex);
            retCode = retBadParam;
          }

          if (createHtmlDetails)
          {
            writeDetails();
          }
        }
      }
      else
      {
        System.out.println("Please specify a filename and a start person as parameters");
        retCode = retBadParam;
      }
    }
    catch (FileNotFoundException e)
    {
      System.out.println("parseFile: File Not Found: " + e.getMessage());
      e.printStackTrace();
      retCode = retBadParam;
    }
    catch(IOException e)
    {
      System.out.println("IOException occurred: " + e.getMessage());
      e.printStackTrace();
      retCode = retSystemError;
    }
    catch (NumberFormatException e)
    {
      retCode = retNumberFormatError;
    }
    catch (Exception e)
    {
      System.out.println("Exception occurred: " + e.getMessage());
      System.out.println("  On line " + curLineNum + ": <" + curLine + ">");
      e.printStackTrace();
      retCode = retUnknownError;
    }
    finally
    {
      if (verbose)
      {
        System.out.println("Memory Usage: total memory: "
                           + Runtime.getRuntime().totalMemory()
                           + " bytes, free memory: "
                           + Runtime.getRuntime().freeMemory()
                           + " bytes");
      }
      if (retCode == 0)
      {
        System.out.println("All done");
        System.exit(retCode);
      }
      else
      {
        System.out.println("An error occurred.");
        System.out.println(paramMsg);
        System.exit(retCode);
      }
    }
  }

  public static boolean parseFile (String       source,
                                   Restrictions restrict) throws FileNotFoundException, IOException
  {
    boolean ret = false;
    Person  curPerson;
    int     i;

    try
    {
      String token;

      people = new PeopleList();

      SrcStream = new BufferedReader (new FileReader (source));
      nextLine();

      while (tokenizer != null)
      {
        if (curLevel == 0)
        {
          // This is the start of a new person, family, or something
          token = tokenizer.nextToken();
          if (token.startsWith ("@I"))
            parsePerson (indexFromIdToken(token), restrict);
          else if (token.startsWith ("@F"))
            parseFamily (indexFromIdToken(token));
          else
            nextLine();
        }
        else
          nextLine();
      }

      people.sort();
      ret = true;
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      System.out.println("parseFile: ArrayIndexOutOfBoundsException: " + e.getMessage() + ", curLine(" + curLineNum + ") = <" + curLine + ">");
      e.printStackTrace();
    }
    catch (NullPointerException e)
    {
      System.out.println("parseFile: NullPointerException: " + e.getMessage() + ", curLine = <" + curLine + ">");
      e.printStackTrace();
    }
    catch (NumberFormatException e)
    {
      System.out.println("parseFile: NumberFormatException: " + e.getMessage() + ", curLine = <" + curLine + ">");
      e.printStackTrace();
    }

    return ret;
  }

  static void parsePerson (int          personNum,
                           Restrictions restrict) throws IOException
  {
    try
    {
      Person person;

      person = new Person();
      people.setPerson(person, personNum);
      person.id = personNum;
      person.includeDetails = includeDetails;

      if (verbose)
        System.out.println("Parsing person " + personNum);

      nextLine();

      while ((tokenizer != null) &&
             (curLevel  != 0))
      {
        if (curLevel == 1)
        {
          String dataType = tokenizer.nextToken();

          if (dataType.compareTo("NAME") == 0)
          {
            parseName(person);
          }
          else if (dataType.compareTo("BIRT") == 0)
          {
            person.addEvent(person.birth = parseEvent("Birth", null));
            person.hide = restrict.hide(person);
          }
          else if (dataType.compareTo("DEAT") == 0)
          {
            person.addEvent(person.death = parseEvent("Death", null));
          }
          else if (dataType.compareTo("CHR") == 0)
          {
            person.addEvent(parseEvent("Christened", null));
          }
          else if (dataType.compareTo("NOTE") == 0)
          {
            person.addNote(parseNote(restOfLine()));
          }
          else if (dataType.compareTo("TITL") == 0)
          {
            person.title = restOfLine();
            nextLine();
          }
          else if (dataType.compareTo("FAMC") == 0)
          {
            if (person.preferredFamily == -1)
            {
              // This is the first FAMC record, so it represents the
              // preferred family.

              String famId;

              famId = tokenizer.nextToken();
              if (famId.startsWith ("@F"))
              {
                person.preferredFamily = indexFromIdToken(famId);
              }
            }
            nextLine();
          }
          else if (dataType.compareTo("SEX") == 0)
          {
            String sex = tokenizer.nextToken();
            if (sex.startsWith ("F"))
              person.sex = Person.female;
            else
              person.sex = Person.male;
            nextLine();
          }
          else if (dataType.startsWith("FAM"))
          {
            nextLine();   // Skips these.
          }
          else
          {
            person.addEvent(parseEvent(null, restOfLine()));
          }
        }
        else
          nextLine();
      }

      // Combine first and last name into a single string
      person.fullName = ((person.firstName != null) ? person.firstName : "???") + " " +
                        ((person.lastName  != null) ? person.lastName  : "???");
    }
    catch (NumberFormatException e)
    {
      String description;
      description =  "parsePerson: NumberFormatException: " + e.getMessage();
      description += "curLine = <" + curLine + ">";
      System.out.println(description);
      e.printStackTrace();
      nextLine();         // Recover by skipping to the next line and returning
    }
  }

  // This routine breaks a full name into a first name and a last name
  static void parseName (Person person) throws IOException
  {
    String name        = null;
    int    firstSlash  = 0;
    int    secondSlash = 0;

    try
    {
      name        = restOfLine();
      firstSlash  = name.indexOf("/");
      secondSlash = name.indexOf("/", firstSlash+1);

      person.lastName  = name.substring(firstSlash+1, secondSlash);
      while ((firstSlash > 0) &&
             (name.charAt(firstSlash - 1) == ' '))  // trim off some spaces that some gedcom files have
        firstSlash--;
      person.firstName = name.substring(0, firstSlash);

      if (person.firstName.length() == 0)
        person.firstName = null;
      if (person.lastName.length() == 0)
        person.lastName = null;

      nextLine();

      while (curLevel >= 2)
      {
        if (curLevel == 2)
        {
          String dataType = tokenizer.nextToken();

          if (dataType.compareTo("NSFX") == 0)
            person.nameSuffix = restOfLine();
        }
        nextLine();
      }
    }
    catch (StringIndexOutOfBoundsException e)
    {
      System.out.println("parseName: StringIndexOutOfBoundsException: " + e.getMessage());
      System.out.println("  curLine = <" + curLine + ">");
      System.out.println("  name    = <" + name + ">");
      System.out.println("  firstSlash  = " + firstSlash);
      System.out.println("  secondSlash = " + secondSlash);
      e.printStackTrace();
    }
    catch (NullPointerException e)
    {
      System.out.println("parseName: NullPointerException: " + e.getMessage());
      e.printStackTrace();
    }
  }

  static GedcomEvent parseEvent(String eventType, String eventValue) throws IOException
  {
    GedcomEvent event = new GedcomEvent();

    event.setType(eventType);
    event.setValue(eventValue);
    nextLine();

    while (curLevel >= 2)
    {
      if (curLevel == 2)
      {
        String dataType = tokenizer.nextToken();

        if (dataType.compareTo("DATE") == 0)
          event.setDate(restOfLine());
        else if (dataType.compareTo("PLAC") == 0)
          event.setPlace(restOfLine());
        else if (dataType.compareTo("TYPE") == 0)
          event.setType(restOfLine());
      }
      nextLine();
    }

    return event;
  }

  static String parseNote(String note) throws IOException
  {
    String ret = note;

    nextLine();

    while (curLevel >= 2)
    {
      // Notes are not to be implemented until later.
//      if (curLevel == 2)
//      {
//        String dataType = tokenizer.nextToken();

//        if (dataType.compareTo("CONT") == 0)
//          ret += '\n' + restOfLine();
//      }
      nextLine();
    }

    return ret;
  }

  // parseFamily: This routine parses a family record in a GEDCOM file.  It
  //              assumes that the husband and wife are listed before any of
  //              the children.
  static void parseFamily (int familyNum) throws IOException
  {
    Family family = null;
    Person father = null;
    Person mother = null;
    Person child  = null;

    if (verbose)
      System.out.println("Parsing family " + familyNum);

    family = new Family();
    people.setFamily(family);
    family.id = familyNum;

    try
    {
//      doDebug = true;
      nextLine();

      while ((tokenizer != null) &&
             (curLevel  != 0))
      {
        if (curLevel == 1)
        {
          String dataType = tokenizer.nextToken();

          if (dataType.compareTo("HUSB") == 0)
          {
            father = family.father = people.getPerson(indexFromIdToken(tokenizer.nextToken()));
            if (father != null)
            {
              father.addFamily (family);
            }
            nextLine();
          }
          else if (dataType.compareTo("WIFE") == 0)
          {
            mother = family.mother = people.getPerson(indexFromIdToken(tokenizer.nextToken()));
            if (mother != null)
            {
              mother.addFamily(family);
            }
            nextLine();
          }
          else if (dataType.compareTo("CHIL") == 0)
          {
            child = people.getPerson(indexFromIdToken(tokenizer.nextToken()));
            if (child != null)
            {
              // Just in case a preferred family isn't specified, or the
              // specified preferred family doesn't exist, if the child has
              // not already been assigned to a family, assign it now.
              // However, even if the child has already been assigned to a
              // family, if this is the child's preferred family, re-assign it.
              if ((child.preferredFamily == family.id) ||
                  (child.childOfFamily   == null))
              {
                child.childOfFamily = family;
                child.father = father;
                child.mother = mother;
              }
              family.addChild (child);

              //dead-code  else
              //dead-code  {
              //dead-code    Enumeration enumChildren;
              //dead-code    Person      tempChild;
              //dead-code  
              //dead-code    // The child is a child of two different families.
              //dead-code    // Print out an error message
              //dead-code  
              //dead-code    System.err.println("\nError: " + child.fullName + "(" + child.id + ") is a child of more than one family:\n");
              //dead-code  
              //dead-code    // Family 1
              //dead-code    System.err.println("       Family(" + child.childOfFamily.id + ")");
              //dead-code    if (child.father != null)
              //dead-code      System.err.println("          Father: " + child.father.fullName);
              //dead-code    if (child.mother != null)
              //dead-code      System.err.println("          Mother: " + child.mother.fullName);
              //dead-code    enumChildren = child.childOfFamily.children.elements();
              //dead-code    while (enumChildren.hasMoreElements())
              //dead-code    {
              //dead-code      tempChild = (Person) ((SortableHandle)enumChildren.nextElement()).getContainer();
              //dead-code      System.err.println("          Child:  " + tempChild.fullName);
              //dead-code    }
              //dead-code    System.err.println("");  // blank line
              //dead-code  
              //dead-code    // Family 2
              //dead-code    System.err.println("       Family(" + family.id + ")");
              //dead-code    if (father != null)
              //dead-code      System.err.println("          Father: " + father.fullName);
              //dead-code    if (mother != null)
              //dead-code      System.err.println("          Mother: " + mother.fullName);
              //dead-code    enumChildren = family.children.elements();
              //dead-code    while (enumChildren.hasMoreElements())
              //dead-code    {
              //dead-code      tempChild = (Person) ((SortableHandle)enumChildren.nextElement()).getContainer();
              //dead-code      System.err.println("          Child:  " + tempChild.fullName);
              //dead-code    }
              //dead-code    System.err.println("          Child:  " + child.fullName);
              //dead-code  } // end of child of two families error
            } // end of if child != null
            nextLine();
          } // end of if dataType == CHIL
          else if (dataType.compareTo("MARR") == 0)
          {
            GedcomEvent marriage;

            marriage = parseEvent("Marriage", null);

            family.marriage = marriage;

            if (father != null)
            {
              marriage.setValue((mother != null) ? mother.fullName : null);
              father.addEvent(marriage);
              if (mother != null)
                marriage = new GedcomEvent(marriage);
            }

            if (mother != null)
            {
              marriage.setValue((father != null) ? father.fullName : null);
              mother.addEvent(marriage);
            }
          }
          else
            nextLine();
        }
        else
          nextLine();
      }
    }
    catch (NumberFormatException e)
    {
      String description;
      description  = "parseFamily: NumberFormatException: " + e.getMessage();
      description += "curLine = <" + curLine + ">";
      System.out.println(description);
      e.printStackTrace();
      curLine = SrcStream.readLine();  // Recover by skipping to the next line and returning
    }
  }

  static private void nextLine() throws IOException
  {
    curLineNum++;
    while (((curLine = SrcStream.readLine()) != null) &&
           !(tokenizer = new StringTokenizer(curLine)).hasMoreTokens())
      curLineNum++;

    if (curLine != null)
    {
      try
      {
        curLevel = Integer.parseInt(tokenizer.nextToken());
      }
      catch (NumberFormatException e)
      {
        nextLine();
      }
    }
    else
    {
      curLevel = 0;
      tokenizer = null;
    }
  }

  static private int indexFromIdToken(String idToken)
  {
    String temp = idToken.substring (2, idToken.indexOf('@', 2));
    return Integer.parseInt (temp);
  }

  static private String restOfLine()
  {
    String ret = null;

    while (tokenizer.hasMoreTokens())
    {
      if (ret == null)
        ret = tokenizer.nextToken();
      else
        ret += " " + tokenizer.nextToken();
    }

    return ret;
  }



  //+-------------------------------------------------------------------------+
  //| These methods are used to write the data file                           |
  //+-------------------------------------------------------------------------+

  public static void writeFile (String dest,
                                Person startPerson)
  {
    DataOutputStream dstStream;   // Destination stream
    DataOutputStream zipStream;   // Compressed stream
    DataOutputStream pwdStream;   // Password stream
    Record           dstRecord;
    Record           zipRecord;
    Record           pwdRecord;

    try
    {
      dstStream = new DataOutputStream (new FileOutputStream(dest + ".gen"));
      zipStream = new DataOutputStream (new GZIPOutputStream (new FileOutputStream(dest + ".gec")));
      pwdStream = new DataOutputStream (new FileOutputStream(dest + ".gpw"));
      dstRecord = new Record(dstStream);
      zipRecord = new Record(zipStream);
      pwdRecord = new Record(pwdStream);

      // Write version info to the stream
      dstRecord.write(Record.VERSION, Record.VERSION_200);
      zipRecord.write(Record.VERSION, Record.VERSION_200);

      if (password != null)
      {
        // Write password info to the password stream

        dstRecord.write(Record.PASSWORD);  // This goes into the .gen stream
                                           // to tell Geneo that a password exists
        zipRecord.write(Record.PASSWORD);  // Ditto
        pwdRecord.write(Record.PASSWORD, password);
        pwdStream.close();
      }

      people.writeFrom(startPerson, dstRecord);
      people.writeFrom(startPerson, zipRecord);

      dstStream.close();
      zipStream.close();
    }
    catch (IOException e)
    {
      System.out.println("IOException: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void writeDetails ()
  {
    FileWriter curFile;
    int        numPeople = people.getCount();
    int        i;
    Person     person;
    DecimalFormat format = new DecimalFormat("0000");
    String        idString = null;

    try
    {
      for (i = 0; i < numPeople; i++)
      {
        person = people.getPerson(i);

        if (person != null)
        {
          idString = format.format(person.id);
          curFile = new FileWriter ("UHP-" + idString + ".html");

          if (curFile != null)
          {
            curFile.write (buildDetailsHtmlHeader (person));
            curFile.write (buildDetailsHtmlBody   (person));
            curFile.close();
          }
          else
            System.out.println ("In writeDetails(), couldn't create file");
        }
      }
    } // try
    catch (IOException e)
    {
      System.out.println ("writeDetails: IO Exception: " + e);
    }
  } // writeDetails

  private static String buildDetailsHtmlHeader (Person person)
  {
    return
          "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">   \n" +
          "<HTML>   \n" +
          "<HEAD>   \n" +
          "   <TITLE>Genealogy Page for " + person.fullName + "</TITLE>   \n" +
          "   \n" +
          "  <SCRIPT LANGUAGE='JavaScript'>   \n" +
          "  <!--   \n" +
          "    function launch (primary)   \n" +
          "    {   \n" +
          "      if (document.InterneTree.isLoaded() == false)   \n" +
          "      {   \n" +
          "        window.status = \"Launching InterneTree with \" + primary + \" as primary\";   \n" +
          "        document.InterneTree.begin(\"detailsloc\",                // source file   \n" +
          "                                   600,                          // width   \n" +
          "                                   360,                          // height   \n" +
          "                                   \"000000\",                     // foreground   \n" +
          "                                   \"E0E0E0\",                     // background   \n" +
          "                                   \"E0E0E0\",                     // people box background   \n" +
          "                                   \"2\",                          // people box border width   \n" +
          "                                   \"../../gifs/bckgrnd7_32.jpg\", // background image   \n" +
          "                                   \"1\",                          // background image layout   \n" +
          "                                   \"0\",                          // clear background   \n" +
					"                                   primary,                      // primary individual   \n" +
          "                                   \".\",                    // detail location   \n"  +
          "                                   \"_parent\",                     // html target   \n" +
          "                                   null,                     // initial zoom   \n" +
          "                                   null);                    // help URL   \n" +
          "      }   \n" +
          "      else   \n" +
          "      {   \n" +
          "        window.status = \"Setting primary to \" + primary;   \n" +
          "        document.InterneTree.setPrimary(parseInt(primary));   \n" +
          "        document.InterneTree.showWindow();   \n" +
          "      }   \n" +
          "    }   \n" +
          "   \n" +
          "  //-->   \n" +
          "  </SCRIPT>   \n" +
          "   \n" +
          "</HEAD>   \n";
  }

  private static String buildDetailsHtmlBody (Person person)
  {
    return
    "<BODY>   \n" +
    "   \n" +
//    "<SCRIPT LANGUAGE='JavaScript'>   \n" +
//    "  window.name = 'GooGooBar';   \n" +
//    "</SCRIPT>   \n" +
//    "   \n" +
    person.toHtml() +
    "   \n" +
    "<APPLET code     = \"zaluc.geneo.Geneo.class\"   \n" +
    "        codebase = \"../../javabin.20\"   \n" +
    "        archive  = \"geneo.jar\"   \n" +
    "        name     = \"InterneTree\"   \n" +
    "        width    = \"2\"   \n" +
    "        height   = \"2\">   \n" +
    "</APPLET>   \n" +
    "   \n" +
    "</BODY>\n";
  }

  //+-------------------------------------------------------------------------+
  //| These methods are for debugging                                         |
  //+-------------------------------------------------------------------------+

  public static void dumpParams(String argv[])
  {
    int i;

    System.out.println ("The following parameters were specified:");

    for (i = 0; i < argv.length; i++)
    {
      System.out.println ("   " + argv[i]);
    }
  }
}
