package zaluc.utils;

/**
 * A sortable extension to java.util.Vector.
 *
 * The SortableXXX group of classes include zaluc.util.SortableVector,
 * zaluc.util.SortableHandle and zaluc.util.SortableObject.
 *
 * To use this collection of classes, a "client" object would implement
 * the SortableObject interface and would contain one or more SortableHandles,
 * each of which would be used in a different SortableVector.  A
 * reference to the containing SortableObject would be passed to each
 * SortableHandle in its constructor.  The SortableHandles are then placed
 * in their respective SortableVectors.
 *
 * When the client wishes to sort a SortableVector, it will call the compareTo
 * on the SortableHandle, passing it a compareToken.  This compareToken is then
 * passed in turn to the compareTo method of the SortableObject that the
 * SortableHandle represents.  The value of the compareToken is passed into
 * the SortableVector in its constructor.  The Sortable group of classes do not
 * use the compareToken themselves, instead, the client can use the value of the
 * compare token to determine which SortableVector is doing the sorting and can
 * then use a different sort criteria for each SortableVector.
 *
 * Note that it is the responsibility of the user to insure that
 * all objects in this vector are of type SortableHandle at the time
 * that the sort takes place.
 *
 * @author Don Baldwin
 * @see    java.util.Vector
 * @see    zaluc.utils.SortableHandle
 * @see    zaluc.utils.SortableObject
 */
public class SortableVector extends java.util.Vector
{
  private int compareToken;

  /**
   * Constructor.  The first two parameters are passed directly to the Vector
   * constructor.  The last parameter is used by the client object to 
   *
   * @param initialCapacity    passed to the Vector constructor
   * @param capacityIncrement  passed to the Vector constructor
   * @param compareToken       passed to the SortableObject compareTo method
   */
  public SortableVector(int initialCapacity,
                        int capacityIncrement,
                        int compareToken)
  {
    super(initialCapacity, capacityIncrement);
    this.compareToken = compareToken;
  }

  /**
   * Causes the SortableVector to sort utself
   */
  public void sort()
  {
    SortableHandle curObj = mergeSort(0, size()-1);
    int            index  = 0;

    while ((curObj != null) &&
           (index < size()))
    {
      setElementAt(curObj, index);
      curObj.setIndex(index);
      curObj = curObj.getNext();
      index++;
    }
    if (index < size())
    {
      setSize(index);
      trimToSize();
    }
  }

  private SortableHandle mergeSort(int start, int end)
  {
//    if (compareToken == 2)                                                         //zombie
//      System.out.println("SortableVector::mergeSort(" + start + "," + end + ")");  //zombie

    SortableHandle ret = null;
    SortableHandle startObj = null;
    SortableHandle endObj   = null;
    int            len = end - start + 1;

    if (len == 0)
      return null;

    startObj = getObj(start);
    endObj   = getObj(end);

    switch (len)
    {
      case 1:
        if (startObj != null)
          startObj.setNext(null);
        ret = startObj; // trivial case
        break;
      case 2:
        if (startObj == null)
          ret = endObj;
        else if (endObj == null)
          ret = startObj;
        else if (startObj.compareTo(endObj, compareToken) > 0)
        {
          // start is greater than end, so swap and return
          endObj.setNext(startObj);
          startObj.setNext(null);
          ret = endObj;
        }
        else
        {
          // start is less than or equal to end, return in order
          startObj.setNext(endObj);
          endObj.setNext(null);
          ret = startObj;
        }
        break;
      default:
      {
        SortableHandle list1;
        SortableHandle list2;
        SortableHandle curObj = null;

        list1 = mergeSort (start, (start + end) / 2);
        list2 = mergeSort ((start + end) / 2 + 1, end);

        // Now merge the two sorted lists
        while ((list1 != null) && (list2 != null))
        {
          if (list1.compareTo(list2, compareToken) <= 0)
          {
            // list1 <= list2, so add list1 to list
            if (ret == null)
              ret = curObj = list1;
            else
            {
              curObj.setNext(list1);
              curObj = curObj.getNext();
            }
            list1 = list1.getNext();
          }
          else
          {
            // list2 < list1, so add list2 to list
            if (ret == null)
              ret = curObj = list2;
            else
            {
              curObj.setNext(list2);
              curObj = curObj.getNext();
            }
            list2 = list2.getNext();
          }
        }

        if (list1 == null)
        {
          // If list1 ran out first, append list2 to the return list
          if (curObj != null)
            curObj.setNext(list2);
          else
            curObj = list2;
        }
        else
        {
          // else, list2 ran out first, append list1 to the return list
          if (curObj != null)
            curObj.setNext(list1);
          else
            curObj = list1;
        }

        if (ret == null)
          ret = curObj;
      }
    }

//    String debug = "";
//    SortableHandle temp;
//    for (temp = ret; temp != null; temp = temp.getNext())
//      debug += temp.fullName + "->";
//    System.out.println("SortableVector::sort(" + start + ","  + end + ") returning " + debug + "null");
//    System.out.println("SortableVector::sort(" + start + ","  + end + ") returning");

    return ret;
  }

  private SortableHandle getObj(int index)
  {
    return (SortableHandle) elementAt(index);
  }
}
