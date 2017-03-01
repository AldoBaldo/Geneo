package zaluc.gparser200;

import java.io.*;

//+-- Class Record100 --------------------------------------------------------+
//|                                                                           |
//| Syntax:       class Record100                                             |
//|                                                                           |
//| Description:  The Record100 class contains definitions used in version    |
//|               1.00 of the .gen data file that is produced by gparser and  |
//|               consumed by geneo.                                          |
//|                                                                           |
//| Methods:                                                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

public class Record100 extends Record
{
  public Record100(Record r)
  {
    super(r);
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
    outputStream.writeByte(recordType);
    outputStream.writeUTF(stringValue);
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
    String ret = inputStream.readUTF();

//    System.out.println("Reading: <" + ret + ">");
    return ret;
  }
}
