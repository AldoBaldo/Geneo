package zaluc.utils;

//+-- Interface Callback -----------------------------------------------------+
//|                                                                           |
//| Syntax:       interface Callback                                          |
//|                                                                           |
//| Description:  This interface defines the methods that must be provided    |
//|               for an object that wishes to be notified of the outcome     |
//|               of a generic task.                                          |
//|                                                                           |
//|---------------------------------------------------------------------------+

public interface Callback
{
  void callback (int taskId, int result, String strVal1, String strVal2);
}

