package zaluc.gparser200;

import java.io.*;
import java.lang.*;

//+-- Class Record -----------------------------------------------------------+
//|                                                                           |
//| Syntax:       class Record                                                |
//|                                                                           |
//| Description:  The Record class contains definitions used in               |
//|                    of the .gen data file that is produced by gparser and  |
//|               consumed by geneo.                                          |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

public class Record
{
  // Version Labels
  public static final int VERSION_100  = 100;
  public static final int VERSION_200  = 200;

  // Record Types

  public static final int VERSION      =  0;
  public static final int PEOPLE_COUNT =  1;
  public static final int FAMILY_COUNT =  2;
  public static final int PERSON       =  3;
  public static final int ID           =  4;
  public static final int FIRST_NAME   =  5;
  public static final int LAST_NAME    =  6;
  public static final int TITLE        =  7;
  public static final int NAME_SUFFIX  =  8;
  public static final int DETAILS      =  9;
  public static final int LIFE_DATES   = 10;
  public static final int SEX          = 11;
  public static final int FATHER       = 12;
  public static final int MOTHER       = 13;
  public static final int FAMILY_LINK  = 14;
  public static final int FAMILY       = 15;
  public static final int CHILD        = 16;
  public static final int HIDE         = 17;  // New for 2.00
  public static final int PASSWORD     = 18;  // New for 2.00

  // Type types

  protected DataOutputStream outputStream;
  protected DataInputStream  inputStream;

  public Record(DataOutputStream d)
  {
    outputStream = d;
  }

  public Record(DataInputStream s)
  {
    inputStream = s;
  }

  public Record(Record r)
  {
    inputStream  = r.inputStream;
    outputStream = r.outputStream;
  }

  public void write(int recordType) throws IOException
  {
    outputStream.writeByte(recordType);
  }

  public void write(int recordType,
                    int intValue) throws IOException
  {
    outputStream.writeByte(recordType);
    outputStream.writeShort(intValue);
  }

  public void write(int    recordType,
                    String stringValue) throws IOException
  {
    StringBuffer strBuf = new StringBuffer(stringValue);
    char         ch;
    int          i;

    for (i = 0; i < strBuf.length(); i++)
    {
      ch = strBuf.charAt(i);
      ch ^= 0xFF;
      strBuf.setCharAt(i, ch);
    }

    outputStream.writeByte(recordType);
    outputStream.writeUTF(new String(strBuf));
  }

  public int readRecordType() throws IOException
  {
    return (int)inputStream.readByte();
  }

  public int readIntValue() throws IOException
  {
    int ret = (int)inputStream.readShort();

//    System.out.println("Reading: <" + ret + ">");
    return ret;
  }

  public String readStrValue() throws IOException
  {
    String        str    = inputStream.readUTF();
    StringBuffer  strBuf = new StringBuffer (str);
    char          ch;
    int           i;

    for (i = 0; i < strBuf.length(); i++)
    {
      ch = strBuf.charAt(i);
      ch ^= 0xFF;
      strBuf.setCharAt(i, ch);
    }

//    System.out.println("Reading: <" + ret + ">");
    return new String(strBuf);
  }
}
