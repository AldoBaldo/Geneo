package zaluc.gpassword;

import java.lang.*;
import java.io.*;
import java.util.*;
import zaluc.gparser200.Record;

public class GPassword
{
  private static final String paramMsg =  // This message describes the allowable parameters.
  "GPassword creates a password file that can be used by InterneTree to     \n" +
  "protect some of the data in the that is downloaded.                      \n" +
  "                                                                         \n" +
  "The following parameters are allowable:                                \n\n" +
  "     filename:      This parameter must be first and must exist.  It is  \n" +
  "                    the name of the password file that will be created,  \n" +
  "                    without the .gpw suffix.  It should be the same as   \n" +
  "                    the name of the GEDCOM file that the password is     \n" +
  "                    protecting.                                          \n" +
  "                                                                         \n" +
  "     password:      This parameter must be second and must exist.  It is \n" +
  "                    the new password that will be used to protect the    \n" +
  "                    data in the GEDCOM file.                             \n" +
  "                                                                         \n" +
  "                    The password may be any length and may contain any   \n" +
  "                    printable characters.\n";

  //+-- Method main ----------------------------------------------------------+
  //|                                                                         |
  //| Syntax:                                                                 |
  //|                                                                         |
  //|   public statc void main(String argv[])                                 |
  //|                                                                         |
  //|-------------------------------------------------------------------------+
  public static void main(String argv[])
  {
    try
    {
      if (argv.length == 2)
      {
        // The first parameter must be the file name.
        String           file      = argv[0].toLowerCase() + ".gpw";
        String           password  = argv[1];
        DataOutputStream pwdStream = new DataOutputStream (new FileOutputStream(file));
        Record           pwdRecord = new Record(pwdStream);

        pwdRecord.write(Record.PASSWORD, password);
        pwdStream.close();
      }
      else
      {
        System.out.println(paramMsg);
      }
    }
    catch (IOException e)
    {
      System.out.println("IOException: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
