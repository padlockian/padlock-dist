Padlock 2.X Series Release Log

April 19th, 2011 - 2.2
====================================
- Fixed a nasty bug with character encoding during the license signing and validation process.  Licenses may refuse to validate if they are validated in a different locale than they were generated.  All customers are urged to upgrade their back-office and end user versions of Padlock as soon as possible.

- Added the capability to view and modify the list of VM addresses in the DefaultMacAddressProvider.  See the documentation and API for more details.

Sept 20th, 2010 - 2.1.2
====================================
- This release fixes a bug in LicenseIO.importLicense(String), where importation may fail if the end user has a non-standard CharSet and one or more license properties contain special characters.

Sept 13th, 2010 - 2.1.1
====================================
- This release fixes a rare but serious validation bug affecting those attempting to validate a Padlock 1.x license while using the Bouncy Castle JCE.  It essentially was an uncaught cast exception only thrown by Bouncy Castle.

August 5th, 2010 - 2.1.0
====================================
- This release offers official support for Java 1.5 in the Padlock core module, including hardware locking.  In order to offer full 1.5 compatibility the License.addHardwareAddress(NetworkInterface) has been removed from the API.  

- Added a pluggable MacAddressProvider interface for those wishing to write their own methods for determining system MAC addresses.  

The Padlock Manager (Swing App) still requires Java 1.6+ to run.  

April 27, 2010 - 2.0.3
====================================
- Fix in Padlock core for two rare bugs: 1) On some environments, Padlock could take between several to 20 seconds to validate a license.  2) On environments where the current OS user had no home/temp folder, license validation was failing.

Both bugs are believed to have been fixed.  Please send any regressions to support@padlocksofware.net


March 29, 2010 - 2.0.2
====================================
- Fix in Padlock Core for two bugs 1) Padlock API did not offer a method to set the location of the Padlock.lic file and 2) KeyManager import was not throwing an exception when attempting to import an older Padlock (1.x series) key

- Fix in Padlock Manager to use the selected Padlock License file from chosen location when signing licenses.

March 20, 2010 - 2.0.1
====================================
- Fixed a small bug where Padlock's licensemaker command console utility refused to recognize 1.x versions of Padlock.lic.


March 12, 2010 - 2.0 Initial Release
====================================

For support please see http://www.javalicensemanager.com/support

For the most up to date documentation on Padlock 2.0, see:
http://support.padlocksoftware.net/display/padlock/Home
