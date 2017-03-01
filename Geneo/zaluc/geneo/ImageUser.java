package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

//+-- Interface ImageUser ----------------------------------------------------+
//|                                                                           |
//| Syntax:       interface ImageUser                                         |
//|                                                                           |
//| Description:  This interface is used by an object that wishes to be       |
//|               notified when an image has been loaded.                     |
//|                                                                           |
//| Methods:      void setImage (Image inImage)                               |
//|                                                                           |
//|---------------------------------------------------------------------------+

interface ImageUser
{
  void setImage (Image inImage);
}

