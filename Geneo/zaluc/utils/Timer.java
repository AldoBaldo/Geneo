package zaluc.utils;


public class Timer implements Runnable
{
  private long     sleepTimeMSecs;
  private Callback wakeUp;
  private int      taskId;

  private Thread   thread = null;

  public Timer (long     sleepTimeMSecs,
                Callback wakeUp,
                int      taskId)
  {
    this.sleepTimeMSecs = sleepTimeMSecs;
    this.wakeUp         = wakeUp;
    this.taskId         = taskId;

    thread = new Thread(this, "Timer Thread");
    thread.start();

  } // Timer

  public void reset()
  {
    thread.interrupt();
  }

  public void run ()
  {
    do
    {
      try
      {
        Thread.sleep (sleepTimeMSecs);
      }
      catch (InterruptedException e)
      {
        // do nothing.
      }
    } while (Thread.interrupted());

    wakeUp.callback (taskId, 0, null, null);
  }

}
