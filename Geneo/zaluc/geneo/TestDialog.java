package zaluc.geneo;

import java.applet.*;
import java.awt.*;
import java.lang.*;
import java.io.*;

import zaluc.utils.*;

// This class is used to test various classes in Geneo.
// No other objects access this class, so even if it is
// in the Geneo directory, it will never be downloaded.
// Also, it does not need to be present.

public class TestDialog extends Applet
{
  Frame a_window;
//  user_dialog dialog;
//  InfoDlg     dialog;
  EntryDlg    dialog;
  Button show_dialog;

  public void init()
  {
    Container  parent;
//    Globals    globals;
//    MainFrame  frame;
//    TreeCanvas tree;

    System.out.println("Geneo Test Dialog Applet");

    this.setLayout(new BorderLayout(0,0));

    for (parent=getParent();
         (parent != null) && !(parent instanceof Frame);
         parent = parent.getParent())
      ;
    //a_window = new Frame("Testing Dialogs");
    a_window = (Frame)parent;
    show_dialog = new Button("Show Dialog");
    add("South", show_dialog);
//    globals = new Globals (this,"baldwin.gen",null,null,null,null,null,null,null);
//    tree = new TreeCanvas (globals, (MainFrame)null);
//    add("Center", tree);
//    frame = new MainFrame(globals, 400, 400);
//    globals.appletFrameParent = frame;
  }

  public boolean action(Event the_event, Object the_arg)
  {
    if (the_event.target instanceof Button)
    {
//      dialog = new user_dialog(a_window, "The Sky Is Falling!",true);
//      dialog = new InfoDlg (a_window,
//                            (AppletContext)getAppletContext(),
//                            (Component)this,
//                            "This is the title",
//                            "This is the message",
//                            true);
        dialog = new EntryDlg(a_window,
                              "Entry Dialog",
                              "Enter Yo Password",
                              true,
                              null,
                              null,
                              1);
      dialog.show();
    }
    return true;
  }
}

class user_dialog extends Dialog
{
  Button b_ok, b_cancel;

  user_dialog(Frame a_frame, String the_title, boolean modal)
  {
    super(a_frame, the_title, modal);
    FlowLayout fl;

    b_ok = new Button("OK");
    fl = new FlowLayout();
    setLayout(fl);
    add(b_ok);
    resize(200,40);
  }

  public boolean action(Event the_event, Object the_arg)
  {
    if (the_event.target instanceof Button)
    {
      hide();
      dispose();
    }
    return true;
  }
}
