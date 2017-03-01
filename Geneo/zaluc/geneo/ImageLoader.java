package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

//+-- Class ImageLoader ------------------------------------------------------+
//|                                                                           |
//| Syntax:       class ImageLoader implements Runnable                       |
//|                                                                           |
//| Description:  ImageLoader loads an image file and then notifies an        |
//|               ImageUser when the image has been loaded.  The image is     |
//|               loaded asynchronously in another thread.                    |
//|                                                                           |
//| Methods:      public      ImageLoader (Image     inImage,                 |
//|                                        ImageUser inUser,                  |
//|                                        Component inComponent)             |
//|                                                                           |
//|               public void run         ()                                  |
//|                                                                           |
//|---------------------------------------------------------------------------+

class ImageLoader implements Runnable
{
  Image        image;
  ImageUser    user;
  MediaTracker tracker;
  Thread       thread;

  public ImageLoader (Image     inImage,
                      ImageUser inUser,
                      Component inComponent)
  {
    image = inImage;
    user  = inUser;

    tracker = new MediaTracker(inComponent);
    tracker.addImage(inImage, 0);

    thread = new Thread(this, "ImageLoader Thread");
    thread.start();
  }

  public void run ()
  {
    try
    {
      tracker.waitForID(0);
      if (!tracker.isErrorID(0))
        user.setImage (image);
    }
    catch (InterruptedException e)
    {
      // Do nothing, image will simply not be displayed
      System.out.println("Couldn't load image");
    }
  }
}
