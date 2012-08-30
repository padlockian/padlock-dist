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

package net.padlocksoftware.padlock.licensemaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.interfaces.DSAPrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import net.padlocksoftware.padlock.KeyManager;
import net.padlocksoftware.padlock.license.License;
import net.padlocksoftware.padlock.license.LicenseFactory;
import net.padlocksoftware.padlock.license.LicenseIO;
import net.padlocksoftware.padlock.license.LicenseSigner;

/**
 *
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public class Main {

    //
    // If not specified, this format is used.
    //
    static String defaultDateFormat = "yyyy/MM/dd";
    static Properties properties = new Properties();
    static KeyPair keyPair = null;
    static File licenseFile = null;
    static License license = null;
    static Date startDate = null;
    static Date expirationDate = null;
    static Long floatingExpirationPeriod = null;
    static Set<String> hardwareAddresses = new HashSet<String>();
    static boolean stdOut = false;

    private static void showUsageAndExit() {

        System.err.println("Usage:");
        System.err.println("LicenseMaker [options] -k <KeyPair File> (-o <Output License File> | -O)\n");
        System.err.println("Options:");
        System.err.println("   -O                       Send output to standard out instead of a file\n");
        System.err.println("   -s <Start>               The start of the license validity period, if\n" +
                "                            different than the current date.  In ms since\n" +
                "                            the epoch (1/1/1970)");
        System.err.println("   -S <Start> [format]      The start of the license validity period, if\n" +
                "                            different then the current date.  <Start> is of the\n" +
                "                            format specified in [format], or defaults to yyyy/MM/dd.\n" +
                "                            see Java's SimpleDateFormat for all formatting options.");
        System.err.println("   -e <Expiration>          License expiration date, expressed as above. If\n" +
                "                            this option is omitted the license is perpetual");
        System.err.println("   -E <Expiration> [format] The end of the license validity period. <Expiration> \n" +
                "                            is of the format specified in [format], or defaults to \n" +
                "                            yyyy/MM/dd.\n" +
                "                            see Java's SimpleDateFormat for all formatting options.");
        System.err.println("   -x <Expiration Float>    Number of ms to expire after the initial run");
        System.err.println("   -p <Properties>          License properties, expressed as a single string\n" +
                "                            in the form of \"key1=value1, key2=value2\"");
        System.err.println("   -h <Addresses>           Hardware locked addresses, expressed as a single string\n" +
                "                            in the form of \"mac1, mac2, mac3\"");
        System.exit(1);

    }

    private static void parseStartDate(String arg) {
        try {
            long l = Long.parseLong(arg);
            startDate = new Date(l);
        } catch (Exception e) {
            System.err.println("\nInvalid Start date: " + arg + "\n");
            System.exit(1);
        }
    }

    private static void parseStartDate(String date, String format) {
        if (format == null) {
            format = defaultDateFormat;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            startDate = dateFormat.parse(date);
        } catch (ParseException ex) {
            System.err.println("\nInvalid Start date: " + date + "[" + format + "]\n ");
            System.exit(1);
        }
    }

    private static void parseExpirationDate(String arg) {
        try {
            long l = Long.parseLong(arg);
            expirationDate = new Date(l);
        } catch (Exception e) {
            System.err.println("\nInvalid Expiration date: " + arg + "\n");
            System.exit(1);
        }
    }

    private static void parseExpirationDate(String date, String format) {
        if (format == null) {
            format = defaultDateFormat;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            expirationDate = dateFormat.parse(date);
        } catch (ParseException ex) {
            System.err.println("\nInvalid Expiration date: " + date + "[" + format + "]\n");
            System.exit(1);
        }
    }

    private static void parseExpireAfterRun(String arg) {
        try {
            floatingExpirationPeriod = Long.parseLong(arg);
        } catch (Exception e) {
            System.err.println("\nInvalid Expire after first run: " + arg + "\n");
            System.exit(1);
        }
    }

    private static void parseProperties(String arg) {
        //
        // Properties should be of the form key=value,key=value
        //

        String[] pairs = arg.split(",");

        for (String pair : pairs) {
            String[] str = pair.split("=");
            if (str.length != 2) {
                System.err.println("\nInvalid properties string: " + arg + "\n");
                System.exit(1);
            }
            properties.setProperty(str[0].trim(), str[1].trim());
        }
    }

    private static void parseLicenseHardware(String arg) {
        String[] addresses = arg.split(",");

        for (String address : addresses) {
            address = address.trim();
            hardwareAddresses.add(address);
        }
    }

    private static void parseKeyPairFile(String arg) {
        File keyFile = new File(arg);
        try {
            keyPair = KeyManager.importKeyPair(keyFile);
        } catch (IOException ex) {
            System.err.println("\nError: Unable to read key file: " + keyFile + "\n");
            System.exit(1);
        }
    }

    private static void parseLicenseFile(String arg) {
        licenseFile = new File(arg);
    }

    private static void parseArguments(String[] args) {

        if (args.length < 3) {
            System.err.println("\nError: Insufficient arguments\n");
            showUsageAndExit();
        }

        //
        // Only loop until length-1 since some arguments have both a
        // switch and a parameter
        //
        for (int x = 0; x < args.length - 1; x++) {
            String arg = args[x];

            if (arg.equals("-s")) {
                x++;
                parseStartDate(args[x]);
            } else if (arg.equals("-S")) {
                x++;
                String d = args[x];

                //
                // Check for the optional date format.
                //
                String format = null;
                if (x < args.length - 1) {
                    x++;

                    if (!args[x].startsWith("-")) {
                        format = args[x];
                    } else {
                        x--;
                    }
                }
                parseStartDate(d, format);
            } else if (arg.equals("-e")) {
                x++;
                parseExpirationDate(args[x]);
            } else if (arg.equals("-E")) {
                x++;
                String d = args[x];

                //
                // Check for the optional date format.
                //
                String format = null;
                if (x < args.length - 1) {
                    x++;

                    if (!args[x].startsWith("-")) {
                        format = args[x];
                    } else {
                        x--;
                    }
                }
                parseExpirationDate(d, format);
            } else if (arg.equals("-x")) {
                x++;
                parseExpireAfterRun(args[x]);
            } else if (arg.equals("-p")) {
                x++;
                parseProperties(args[x]);
            } else if (arg.equals("-k")) {
                x++;
                parseKeyPairFile(args[x]);
            } else if (arg.equals("-o")) {
                x++;
                parseLicenseFile(args[x]);
            } else if (arg.equals("-h")) {
                x++;
                parseLicenseHardware(args[x]);
            } else if (arg.equals("-O")){
                stdOut = true;
                x++;
            } else {
                System.err.println("\nError: Uknown Argument\n");
                showUsageAndExit();
            }
        }

        // Check to see if the last argument is -O, since that has
        // can be missed with the above loop
        if (args[args.length-1].equals("-O")) {
            stdOut = true;
        }
        
        //
        // Verify that the license name and key are not null
        //
        if ((licenseFile == null && !stdOut) || keyPair == null) {
            System.err.println("\nError: No output specified\n");
            showUsageAndExit();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        parseArguments(args);

        //
        // Create the license information
        //
        license = LicenseFactory.createLicense();

        if (startDate != null) {
            license.setStartDate(startDate);
        }

        if (expirationDate != null) {
            license.setExpirationDate(expirationDate);
        }

        if (floatingExpirationPeriod != null) {
            license.setFloatingExpirationPeriod(floatingExpirationPeriod);
        }

        for (final Enumeration propNames = properties.propertyNames(); propNames.hasMoreElements();) {
            final String key = (String) propNames.nextElement();
            license.addProperty(key, properties.getProperty(key));
        }

        for (String address : hardwareAddresses) {
            license.addHardwareAddress(address);
        }

        //
        // Finally, sign the file
        //

        LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey) keyPair.getPrivate());
        signer.sign(license);

        try {
            //
            // Export to file
            //
            if (stdOut) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                LicenseIO.exportLicense(license, os);
                System.out.println(os.toString());
                os.close();
            } else {
                LicenseIO.exportLicense(license, licenseFile);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }
}
