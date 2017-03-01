package zaluc.utils;

import java.io.*;

//+-- Class GenFile --------------------------------------------------------+
//|                                                                           |
//| Syntax:       class GenFile                                             |
//|                                                                           |
//| Description:  The GenFile class contains definitions used in version    |
//|               1.00 of the .gen data file that is produced by gparser and  |
//|               consumed by geneo.                                          |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

public class GenFile extends RandomAccessFile
{
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
  public static final int ALIVE        = 17;  // New for 2.00
  public static final int PASSWORD     = 18;  // New for 2.00
  public static final int MPASSWORD    = 19;  // New for 2.00

  // Type types

  public GenFile(String fileName) throws IOException
  {
    super (fileName, "rw");
  }

  public void write(int recordType,
                    int intValue) throws IOException
  {
    writeByte(recordType);
    writeShort(intValue);
  }

  public void write(int    recordType,
                    String stringValue) throws IOException
  {
    writeByte(recordType);
    writeUTF(stringValue);
  }

  public int readRecordType() throws IOException
  {
    return (int)readByte();
  }

  public int readIntValue() throws IOException
  {
    int ret = (int)readShort();

//    System.out.println("Reading: <" + ret + ">");
    return ret;
  }

  public String readStrValue() throws IOException
  {
    String ret = readUTF();

//    System.out.println("Reading: <" + ret + ">");
    return ret;
  }

  public void modifyRecord (String stringValue) throws IOException
  {
    writeUTF (stringValue);
  }
}
