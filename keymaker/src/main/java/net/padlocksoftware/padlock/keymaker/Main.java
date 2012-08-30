/*
* Copyright (c) 2009-2012 Jason Nichols

* Permission is hereby granted, free of charge, to any person obtaining a copy 
* of this software and associated documentation files (the "Software"), to deal 
* in the Software without restriction, including without limitation the rights to 
* use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
* of the Software, and to permit persons to whom the Software is furnished to do 
* so, subject to the following conditions:

* The above copyright notice and this permission notice shall be included in all 
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
* SOFTWARE.
*/

package net.padlocksoftware.padlock.keymaker;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import net.padlocksoftware.padlock.KeyManager;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jason
 */
public final class Main {

    private static String[] formatOutput(String publicKey) {
      List<String> list = new ArrayList<String>();

      // Break the public key String into 60 character strings
      int LENGTH = 60;
      int start = -LENGTH;
      int end = 0;

      while (end < publicKey.length()) {
          end = Math.min(end + LENGTH, publicKey.length());
          start += LENGTH;
          list.add(publicKey.substring(start, end));
      }

      return list.toArray(new String[0]);
    }

    private static void showUsageAndExit() {
        System.out.println("Usage: KeyMaker <outputfile>");
        System.exit(1);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int keySize = 1024;
        File file = null;

        if (args.length < 1|| args.length > 3) {
            showUsageAndExit();
        }
        
        for (int x = 0 ; x < args.length ; x++) {
            String arg = args[x];

            if (arg.equals("-s")) {
                try {
                    x++;
                } catch(Exception e) {
                    showUsageAndExit();
                }
            } else {
                file = new File(arg);
            }
        }

        KeyPair pair = KeyManager.createKeyPair();
        try {
            KeyManager.exportKeyPair(pair, file);
            String[] lines = formatOutput(new String(Hex.encodeHex(pair.getPublic().getEncoded())));
            System.out.println("Your public key code: \n");
            for (int x = 0 ; x < lines.length ; x++) {
                String line = lines[x];
                if (x ==0 ) {
                    // First line
                    System.out.println("\t private static final String publicKey = \n\t\t\"" +
                            line + "\" + ");
                } else if (x == lines.length-1) {
                    // Last line
                    System.out.println("\t\t\"" + line + "\";\n");
                } else {
                    System.out.println("\t\t\"" + line + "\" + ");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
