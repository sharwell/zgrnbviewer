/*   FILE: Messages.java
 *   DATE OF CREATION:   Fri Jan 10 09:37:09 2003
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 * 
 *   $Id: Messages.java 4276 2011-02-25 07:47:51Z epietrig $
 */ 

package net.claribole.zgrviewer;

import org.openide.util.NbBundle;

@NbBundle.Messages({
    "antialiasingWarning=Antialiasing requires additional computing resources.\nSetting it ON will noticeably reduce the refresh rate.",
    "pngOnlyIn140FirstPart=This functionality is only available when running ZGRViewer using a JVM version 1.4.0 or later (it requires the ImageIO API).\nZGRViewer detected JVM version",
    "pngOnlyIn140SecondPart=\nDo you want to proceed anyway (this will probably cause an error)?",
    "VERSION=0.9.0-SNAPSHOT",
    "# {0} - Version",
    "about=ZGRViewer {0}\n"
        + "\n"
        + "A Visualization Tool for GraphViz based on ZVTM\n"
        + "http://zvtm.sourceforge.net/zgrviewer.html\n"
        + "\n"
        + "Written by Emmanuel Pietriga\n"
        + "(INRIA project In Situ)\n"
        + "emmanuel.pietriga@inria.fr",
    "customCallHelp=Use %s for source and %t for target:\ne.g. twopi -Tsvg -o %t %s",
    "customCallExprError=Command line expression is missing %s or %t.\n\nUse %s for source and %t for target:\ne.g. twopi -Tsvg -o %t %s",
    "customCallExprError2=An error occured while running the following command line:\n\n",
    "customCallFileError=The source file has not been specified",
    "# {0} - Source",
    "loadError=An error occured while loading from {0}",
    "webBrowserHelpText=--------------------------------------\n"
        + "AUTOMATIC DETECTION\n"
        + "--------------------------------------\n"
        + "ZGRViewer can try to automatically detect your default web browser.\n"
        + "This feature is currently supported under Windows and some POSIX environments.\n"
        + "\n"
        + "--------------------------------------\n"
        + "MANUAL CONFIGURATION\n"
        + "--------------------------------------\n"
        + "The Path value should be the full command line path to your browser's main executable file. It can also be just this file's name if its parent directory is in your PATH environment variable.\n"
        + "\n"
        + "Examples:\n"
        + "mozilla\n"
        + "/usr/bin/mozilla\n"
        + "C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE\n"
        + "\n"
        + "The Command Line Options value is an optional field where you can put command line switches, like -remote for the UNIX version of Netscape that will open URLs in an already existing Netscape process (if it exists).\n"
        + "\n"
        + "Under Mac OS X, you can simply use\n"
        + "open\n"
        + "or\n"
        + "open -a Safari",
    "proxyHelpText=If you are behind a firewall, you can manually set the proxy server to access remote resources.\n"
        + "\n"
        + "Hostname should be the full name of the proxy server.\n"
        + "\n"
        + "Port should be the port number used to access external resources. This is a number (default value is 80).",
    "notAFile=The specified path does not point to a file:\n",
    "notADirectory=The specified path does not point to a directory:\n",
    "fileDoesNotExist=This path does not point to any existing file or directory:\n",
    "PM_ENTRY=mpmE",
    "PM_SUBMN=mpmS",
    "PM_EXPSVG=Export (SVG)",
    "PM_EXPPNG=Export (PNG)",
    "PM_EXPPRINT=Print",
    "LOADING_SVG=Loading Graphviz/SVG...",
    "CMD_LINE_ERROR=Only -Pdot, -Pneato, -Pcirco, -Ptwopi and -Psvg are allowed",
    "PATH_NOT_SET=NOT SET",
    "FAILED_TO_BUILD_LOGICAL_STRUCT=Failed to build logical structure. Some tools are disabled.",
    "ERROR_LOADING_DOT_FILE=An error occured while parsing this DOT file.",
    "SVG_PARSING_ERROR=SVG parsing error",
    "ABOUT_PLUGINS=Installed ZGRViewer Plugins"
})
public class ZGRMessages {
    private ZGRMessages() {
    }
    
    /*warning, error, help and other messages*/

    public static String antialiasingWarning() {
        return Bundle.antialiasingWarning();
    }
    
    public static String pngOnlyIn140FirstPart() {
        return Bundle.pngOnlyIn140FirstPart();
    }
    
    public static String pngOnlyIn140SecondPart() {
        return Bundle.pngOnlyIn140SecondPart();
    }

    public static String VERSION() {
        return Bundle.VERSION();
    }

    public static String about() {
        return Bundle.about(Bundle.VERSION());
    }

    public static final String commands="Under Mac OS X, replace Ctrl by the Command key\n\nMisc. Commands\n"
	+"* Press right mouse button to display the pie menu\n\n"
	+"* Ctrl+D = open a file with dot (SVG pipeline)\n"
	+"* Ctrl+N = open a file with neato (SVG pipeline)\n"
	+"* Ctrl+Z = open a file with another program (SVG pipeline)\n"
	+"* Ctrl+R = Reload current file (SVG pipeline)\n"
	+"* Ctrl+P = Print current view\n"
	+"* Ctrl+Q = Exit ZGRViewer\n\n"

	+"Navigation (left mouse button corresponds to the single button for Mac users)\n"
	+"* Press left mouse button and drag to move in the graph\n"
	+"* Hold Shift, press left mouse button and drag vertically to zoom-in/zoom-out\n"
	+"* Click left mouse button on a node or arc to center on it\n"
	+"* Hold Alt, press left mouse button and drag to select a region of interest\n"
	+"* Home (or G) = get a global view of the graph\n"
	+"* Ctrl+L = (de)activate distortion lens\n"
	+"* Ctrl+F = pop up search window\n"
	+"* Page Down = Zoom In\n"
	+"* Page Up = Zoom Out\n"
	+"* Mouse wheel =  zoom in/out"
	+"* Arrow Keys = Translation\n"
	+"* B = Back to previous location\n"
	+"* L or Space bar or Middle mouse button = load associated URL in a Web browser\n"
	;

    public static String customCallHelp() {
        return Bundle.customCallHelp();
    }

    public static String customCallExprError() {
        return Bundle.customCallExprError();
    }

    public static String customCallExprError2() {
        return Bundle.customCallExprError2();
    }

    public static String customCallFileError() {
        return Bundle.customCallFileError();
    }

    public static String loadError(String fileName) {
        return Bundle.loadError(fileName);
    }

    public static String webBrowserHelpText() {
        return Bundle.webBrowserHelpText();
    }

    public static String proxyHelpText() {
        return Bundle.proxyHelpText();
    }

    public static String notAFile() {
        return Bundle.notAFile();
    }

    public static String notADirectory() {
        return Bundle.notADirectory();
    }

    public static String fileDoesNotExist() {
        return Bundle.fileDoesNotExist();
    }

    public static String PM_ENTRY() {
        return Bundle.PM_ENTRY();
    }

    public static String PM_SUBMN() {
        return Bundle.PM_SUBMN();
    }

    public static String PM_EXPSVG() {
        return Bundle.PM_EXPSVG();
    }

    public static String PM_EXPPNG() {
        return Bundle.PM_EXPPNG();
    }

    public static String PM_EXPPRINT() {
        return Bundle.PM_EXPPRINT();
    }

    public static String LOADING_SVG() {
        return Bundle.LOADING_SVG();
    }

    public static String CMD_LINE_ERROR() {
        return Bundle.CMD_LINE_ERROR();
    }

    public static String PATH_NOT_SET() {
        return Bundle.PATH_NOT_SET();
    }

    public static String FAILED_TO_BUILD_LOGICAL_STRUCT() {
        return Bundle.FAILED_TO_BUILD_LOGICAL_STRUCT();
    }
    
    public static String ERROR_LOADING_DOT_FILE() {
        return Bundle.ERROR_LOADING_DOT_FILE();
    }

    public static String SVG_PARSING_ERROR() {
        return Bundle.SVG_PARSING_ERROR();
    }

	public static String ABOUT_PLUGINS() {
        return Bundle.ABOUT_PLUGINS();
    }

}
