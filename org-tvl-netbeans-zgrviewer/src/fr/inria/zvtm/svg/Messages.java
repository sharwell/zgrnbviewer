package fr.inria.zvtm.svg;

class Messages {

    private Messages() {
    }

    static final String EMPTY_STRING = "";
    static final String V = "v";
    static String VERSION;
    static final String AUTHORS = "Author: Emmanuel Pietriga";
    static final String APP_NAME = "ZVTM SVG Viewer";
    static final String CREDITS_NAMES = "Based on: ZVTM";
    static final String ABOUT_DEPENDENCIES = "Based upon: ZVTM (http://zvtm.sf.net)";
    static final String H_4_HELP = "--help for command line options";
    static final String LOAD_FILE = "Load file";
    static final String LOADING = "Loading ";
    static final String PROCESSING = "Processing ";
    static final String svgSpaceName = "SVG";
    static final String aboutSpaceName = "About layer";
    static final String mViewName = "SVG Viewer";

    protected static void printCmdLineHelp() {
        System.out.println("Usage:\n\tjava -jar target/zvtm-svg-" + VERSION + ".jar <path_to_file> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
        System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }

}
