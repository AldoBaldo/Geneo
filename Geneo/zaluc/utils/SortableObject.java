package zaluc.utils;

/**
 * This interface is used by zaluc.util.SortableVector and
 * zaluc.util.SortableHandle to compare two objects.  The
 * SortableHandle class will take a reference to objects
 * that implement this interface.
 *
 * @author Don Baldwin
 * @see    zaluc.util.SortableVector
 * @see    zaluc.util.SortableHandle
 */
public interface SortableObject
{
  /**
   * Used to compare two objects that implement the SortableObject interface.
   *
   * Since an object may be in more than one SortableVector, each of which
   * might use different sort criteria, the compareToken can be used to to
   * determine what criteria to use in comparing the two objects.  The values
   * of the compareToken are passed into the SortableVector class when it is
   * created.
   *
   * @param other         The other SortableObject to compare with.
   * @param compareToken  A token that identifies what criteria to compare by.
   * @return  < zero if the comparing object is less than other.  == zero if
   *          the conparing object is equal to other.  > zero if the comparing
   *          object is greater than other.  I.e.  comparer - other
   */
  public int compareTo (SortableObject other, int compareToken);
}
