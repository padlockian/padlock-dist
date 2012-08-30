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

package net.padlocksoftware.padlock.licensevalidator;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import net.padlocksoftware.padlock.KeyManager;
import net.padlocksoftware.padlock.license.ImportException;
import net.padlocksoftware.padlock.license.License;
import net.padlocksoftware.padlock.license.LicenseIO;
import net.padlocksoftware.padlock.license.LicenseState;
import net.padlocksoftware.padlock.license.TestResult;
import net.padlocksoftware.padlock.validator.Validator;
import net.padlocksoftware.padlock.validator.ValidatorException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public class Main {

  static License license = null;
  static KeyPair pair = null;

  private static void showUsageAndExit() {
    System.err.println("Usage: LicenseVerifier -l <License File> -k <KeyPair>");
    System.exit(1);
  }

  private static void parseLicenseFile(String fileName) {
    try {
      license = LicenseIO.importLicense(new File(fileName));
    } catch (IOException ex) {
      System.err.println("Error reading license: " + ex.getMessage());
      System.exit(1);
    } catch (ImportException ex) {
      System.err.println("Error parsing license data: " + ex.getMessage());
      System.exit(1);
    }
  }

  private static void parseKeyPairFile(String fileName) {
    try {
      pair = KeyManager.importKeyPair(new File(fileName));
    } catch (IOException ex) {
      System.err.println("Error reading key file: " + ex.getMessage());
      System.exit(1);
    }
  }

  private static void parse(String[] args) {

    if (args.length < 4) {
      showUsageAndExit();
    }

    //
    // We stop at length - 1 because every argument has both a switch
    // and a parameter.
    //
    for (int x = 0; x < args.length - 1; x++) {
      String arg = args[x];

      if (arg.equals("-l")) {
        x++;
        parseLicenseFile(args[x]);
      } else if (arg.equals("-k")) {
        x++;
        parseKeyPairFile(args[x]);
      } else {
        showUsageAndExit();
      }

    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    parse(args);
    Date currentDate = new Date();

    Validator v = new Validator(license,
            new String(Hex.encodeHex(pair.getPublic().getEncoded())));
    v.setIgnoreFloatTime(true);

    boolean ex = false;
    LicenseState state;
    try {
      state = v.validate();
    } catch (ValidatorException e) {
      state = e.getLicenseState();
    }

    // Show test status
    System.out.println("\nValidation Test Results:");
    System.out.println("========================\n");
    for (TestResult result : state.getTests()) {
      System.out.println("\t" + result.getTest().getName() + "\t\t\t" + (result.passed() ? "Passed" : "Failed"));
    }

    System.out.println("\nLicense state: " + (state.isValid() ? "Valid" : "Invalid"));
    
    //
    // Cycle through any dates
    //
    Date d = license.getCreationDate();
    System.out.println("\nCreation date: \t\t" + d);

    d = license.getStartDate();
    System.out.println("Start date: \t\t" + d);

    d = license.getExpirationDate();
    System.out.println("Expiration date: \t" + d);

    Long floatPeroid = license.getFloatingExpirationPeriod();
    if (floatPeroid != null) {
      long seconds = floatPeroid / 1000L;
      System.out.println("\nExpire after first run: " + seconds + " seconds");

    }

    if (floatPeroid != null || license.getExpirationDate() != null) {
      long remaining = v.getTimeRemaining(currentDate) / 1000L;
      System.out.println("\nTime remaining: " + remaining + " seconds");

    }

    //
    // License properties
    //
    System.out.println("\nLicense Properties");
    Properties p = license.getProperties();
    if (p.size() == 0) {
      System.out.println("None");
    }

    for (final Enumeration propNames = p.propertyNames(); propNames.hasMoreElements();) {
      final String key = (String) propNames.nextElement();
      System.out.println("Property: " + key + " = " + p.getProperty(key));
    }

    //
    // Hardware locking
    //
    for (String address : license.getHardwareAddresses()) {
      System.out.println("\nHardware lock: " + address);
    }
    System.out.println("\n");
  }
}
