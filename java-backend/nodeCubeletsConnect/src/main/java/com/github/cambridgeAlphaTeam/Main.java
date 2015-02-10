package com.github.cambridgeAlphaTeam;

import java.util.Arrays;

import java.io.IOException;

public class Main
{ public static void main (String[] args) throws NumberFormatException,
    IOException
  { if (args.length > 0)
    { CubeletsConnection conn = new ServerCubeletsConnection(
        Integer.parseInt(args[0]));

      while (true)
      { System.out.println("|---------------|");
        for (int value : conn.getCubeletValues())
        { System.out.println("|\t" + value + "\t|");
        }
        System.out.println("|---------------|");

        try
        { Thread.sleep(1000);
        }
        catch(InterruptedException e)
        { /* Don't really care.... */
        }
      }
    }
    else
    { System.out.println("Not enough arguments, expected program to " +
                         "execute and arguments (if any)");
    }
  }
}
