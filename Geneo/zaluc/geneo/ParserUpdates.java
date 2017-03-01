package zaluc.geneo;

//+-- Interface ParserUpdates ------------------------------------------------+
//|                                                                           |
//| Syntax:       interface ParserUpdates                                     |
//|                                                                           |
//| Description:  This interface defines the methods that must be provided    |
//|               for an object that wishes to be notified of the progress    |
//|               of parsing a gedcom file.                                   |
//|                                                                           |
//|---------------------------------------------------------------------------+

interface ParserUpdates
{
  void notifyFileName    (String     fileName);
  void notifySetup       (PeopleList people,
                          String     password,
                          int        recordCount);
  void updateRecordCount (int        recordCount);
  void updateStatus      (String     newStatus);
  void notifyDone        ();
}

