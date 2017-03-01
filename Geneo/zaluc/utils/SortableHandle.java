package zaluc.utils;

/**
 * SortableHandle implements the SortableObject interface, which allows it to
 * be sorted by a SortableVector.  A SortableHandle is contained within a class
 * that wishes to be sortable.  The SortableVector would then contain
 * references to the SortableHandle, which would then contain a reference to
 * the object that contains it.  A class would contain a SortableHandle object
 * rather than inherit from SortableObject directly if it wanted to be sorted
 * in more than one SortableVector.  In such a case, the class would contain
 * a different SortableHandle for each SortableVector that it wanted to be
 * contained in.
 *
 * @author Don Baldwin
 * @see    zaluc.utils.SortableVector
 * @see    zaluc.utils.SortableObject
 */

public class SortableHandle
{
  private SortableObject container = null;
  private SortableHandle next      = null;
  private int            index     = 0;

  /**
   * Constructor.
   *
   * @param container  a reference to the containing object.
   */
  public SortableHandle (SortableObject container)
  {
    this.container = container;
  }

  /*=================================*/
  /* Methods provided for the client */
  /*=================================*/

  /**
   * Get container.
   */
  public SortableObject getContainer ()
  {
    return container;
  }

  /**
   * Get the index for the object.  This method is intended to be called
   * by the client.
   *
   * @return  the index of the SortableHandle.
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * Used to compare two SortableHandles.  This method simply calls the
   * compareTo method of the containing SortableObject.  This method is
   * intended by be called by the SortableVector but could be called by
   * the client as well.
   *
   * @param other         The other SortableHandle to compare with.
   * @param compareToken  A token that identifies what criteria to compare by.
   * @return  < zero if the comparing object is less than other.  == zero if
   *          the conparing object is equal to other.  > zero if the comparing
   *          object is greater than other.
   */
  public int compareTo (SortableHandle other, int compareToken)
  {
    return container.compareTo (other.container, compareToken);
  }

  /**
   * Set the index for the object.  This method should only be called from
   * the SortableVector and not from the client.
   *
   * @param index  the index value to set.
   */
  public void setIndex (int index)
  {
    this.index = index;
  }

  /**
   * Sets the next pointer.  This method is intended by be called by the
   * SortableVector class and not by the client.
   */
  public void setNext (SortableHandle next)
  {
    this.next = next;
  }

  /**
   * Get the next pointer.  This method is intended by be called by the
   * SortableVector class and not by the client.
   */
  public SortableHandle getNext ()
  {
    return next;
  }
}
