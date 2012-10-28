/*
 * FILE: DOTManager.java DATE OF CREATION: Thu Jan 09 14:14:35 2003 Copyright
 * (c) Emmanuel Pietriga, 2002. All Rights Reserved Copyright (c) INRIA,
 * 2004-2011. All Rights Reserved Licensed under the GNU LGPL. For full terms
 * see the file COPYING.
 *
 * $Id: DOTManager.java 4276 2011-02-25 07:47:51Z epietrig $
 */
package net.claribole.zgrviewer;

//import net.claribole.zgrviewer.dot.DOTLexer;
//import net.claribole.zgrviewer.dot.DOTParser;
//import net.claribole.zgrviewer.dot.DOTTreeParser;
//import antlr.CommonAST;
import fr.inria.zvtm.svg.SVGReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.claribole.zgrviewer.dot.Graph;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;

public class DOTManager {

    private static final Logger LOGGER = Logger.getLogger(DOTManager.class.getName());
    public static final short DOT_PROGRAM = 0;
    public static final short NEATO_PROGRAM = 1;
    public static final short CIRCO_PROGRAM = 2;
    public static final short TWOPI_PROGRAM = 3;
    public static final short SVG_FILE = 4;
    short lastProgramUsed = DOT_PROGRAM;
    GraphicsManager grMngr;
    File dotF;
    File svgF;
    Graph graph;

    public DOTManager(GraphicsManager gm) {
        this.grMngr = gm;
    }

    void load(File f, short prg, boolean parser) {
        // prg is the program to use DOTManager.*_PROGRAM
        if (grMngr.gp != null) {
            grMngr.gp.setMessage("Resetting...");
            grMngr.gp.setProgress(10);
            grMngr.gp.setVisible(true);
        }
        try {
            svgF = Utils.createTempFile(ConfigManager.m_TmpDir.toString(), "zgrv", (parser ? ".dot" : ".svg"));
            dotF = f;
            callGraphViz(prg, parser);
            if (grMngr.gp != null) {
                grMngr.gp.setMessage("Deleting Temp File...");
                grMngr.gp.setProgress(100);
                grMngr.gp.setVisible(false);
            }
        } catch (Exception ex) {
            if (grMngr.gp != null) {
                grMngr.gp.setVisible(false);
            }
            
            LOGGER.log(Level.WARNING, ZGRMessages.loadError(f.toString()), ex);
//            javax.swing.JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), ZGRMessages.loadError + f.toString());
        }
    }

    private void callGraphViz(short prg, boolean parser) throws Exception {
        // prg is the program to use DOTManager.*_PROGRAM
        try {
            if (grMngr.gp != null) {
                grMngr.gp.setMessage("Preparing " + (parser ? "Augmented DOT" : "SVG")
                    + " Temp File");
                grMngr.gp.setProgress(10);
            }
            if (parser) {
                if (!generateDOTFile(dotF.getPath(), svgF.getPath(), prg)) {
                    deleteTempFiles();
                    return;
                }
                displayDOT();
                if (ConfigManager.DELETE_TEMP_FILES) {
                    deleteTempFiles();
                }
            } else {
                if (!generateSVGFile(dotF.getPath(), svgF.getPath(), prg)) {
                    deleteTempFiles();
                    return;
                }
                displaySVG(dotF.getParentFile());
                if (ConfigManager.DELETE_TEMP_FILES) {
                    deleteTempFiles();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception generating graph", e);
            Exceptions.printStackTrace(e);
            throw e;
        }
    }

    void deleteTempFiles() {
        if (svgF != null) {
            svgF.delete();
        }
    }

    protected String getProgram(short prg) {
        // prg is the program to use DOTManager.*_PROGRAM
        switch (prg) {
        case DOT_PROGRAM: {
            return ConfigManager.m_DotPath.toString();
        }
        case NEATO_PROGRAM: {
            return ConfigManager.m_NeatoPath.toString();
        }
        case TWOPI_PROGRAM: {
            return ConfigManager.m_TwopiPath.toString();
        }
        case CIRCO_PROGRAM: {
            return ConfigManager.m_CircoPath.toString();
        }
        default: {
            return ConfigManager.m_DotPath.toString();
        }
        }
    }

    private boolean generateDOTFile(String dotFilePath, String tmpFilePath, short prg) {
        List<String> cmdArray = new ArrayList<String>();
        cmdArray.add(getProgram(prg));
        cmdArray.add("-Tdot");
        if (ConfigManager.FORCE_SILENT) {
            cmdArray.add("-q");
        }
//        cmdArray[2] = checkOptions(ConfigManager.CMD_LINE_OPTS);
        cmdArray.add("-o");
        cmdArray.add(tmpFilePath);
        cmdArray.add(dotFilePath);
        Runtime rt = Runtime.getRuntime();
        if (grMngr.gp != null) {
            grMngr.gp.setMessage("Computing Graph Layout (GraphViz)...");
            grMngr.gp.setProgress(40);
        }
        try {
            try {
                File execDir = (new File(dotFilePath)).getParentFile();
                Process p = rt.exec(cmdArray.toArray(new String[0]), null, execDir);
                executeProcess(p);
            } catch (IOException ex) {
                Process p = rt.exec(cmdArray.toArray(new String[0]));
                executeProcess(p);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            LOGGER.log(Level.WARNING, "Error generating output file.", e);
            return false;
        }
        return true;
    }

    /**
     * Invokes the GraphViz program to create a graph image from the the given
     * DOT data file
     *
     * @param dotFilePath the name of the DOT data file
     * @param svgFilePath the name of the output data file
     * @param prg program to use (dot or neato)
     * @return true if success; false if any failure occurs
     */
    private boolean generateSVGFile(String dotFilePath, String svgFilePath, short prg) {
        List<String> cmdArray = new ArrayList<String>();
        cmdArray.add(getProgram(prg));
        cmdArray.add("-Tsvg");
        if (ConfigManager.FORCE_SILENT) {
            cmdArray.add("-q");
        }
//            cmdArray.add(checkOptions(ConfigManager.CMD_LINE_OPTS));
        cmdArray.add("-o");
        cmdArray.add(svgFilePath);
        cmdArray.add(dotFilePath);
        Runtime rt = Runtime.getRuntime();
        if (grMngr.gp != null) {
            grMngr.gp.setMessage("Computing Graph Layout (GraphViz)...");
            grMngr.gp.setProgress(40);
        }
        try {
            try {
                File execDir = (new File(dotFilePath)).getParentFile();
                final Process p = rt.exec(cmdArray.toArray(new String[0]), null, execDir);
                executeProcess(p);
            } catch (IOException ex) {
                Process p = rt.exec(cmdArray.toArray(new String[0]));
                p.waitFor();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error generating output file.", e);
            return false;
        }
        return true;
    }

    protected void executeProcess(Process p) throws InterruptedException, IOException {
        Thread consumer = new ProcessOutputConsumer(p);
        consumer.start();
        p.waitFor();
        p.destroy();
        consumer.interrupt();
    }

    /*
     * load a file using a program other than dot/neato for computing the layout
     * (e.g. twopi)
     */
    void loadCustom(String srcFile, String cmdLineExpr) {
        if (grMngr.gp != null) {
            grMngr.gp.setMessage("Resetting SVG...");
            grMngr.gp.setProgress(10);
            grMngr.gp.setVisible(true);
        }
        try {
            svgF = Utils.createTempFile(ConfigManager.m_TmpDir.toString(), "zgrv", ".svg");
            if (!generateSVGFileFOP(srcFile, svgF.getPath(), cmdLineExpr)) {
                deleteTempFiles();
                return;
            }
            displaySVG(new File(srcFile).getParentFile());
            if (ConfigManager.DELETE_TEMP_FILES) {
                deleteTempFiles();
            }
            if (grMngr.gp != null) {
                grMngr.gp.setMessage("Deleting Temp File...");
                grMngr.gp.setProgress(100);
                grMngr.gp.setVisible(false);
            }
        } catch (Exception ex) {
            if (grMngr.gp != null) {
                grMngr.gp.setVisible(false);
            }

            LOGGER.log(Level.WARNING, ZGRMessages.loadError(srcFile), ex);
//            javax.swing.JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), ZGRMessages.loadError + srcFile);
        }
    }

    /**
     * Invokes a program to create an SVG image from a source file using a
     * program other than dot/neato for computing the layout (e.g. twopi)
     *
     * @return true if success; false if any failure occurs
     */
    private boolean generateSVGFileFOP(String srcFilePath, String svgFilePath, String commandLine) {
        StringTokenizer st = new StringTokenizer(commandLine, " ");
        int nbTokens = st.countTokens();
        String[] cmdArray = new String[nbTokens];
        for (int i = 0; i < nbTokens; i++) {
            cmdArray[i] = st.nextToken();
            if (cmdArray[i].equals("%s")) {
                cmdArray[i] = srcFilePath;
            } else if (cmdArray[i].equals("%t")) {
                cmdArray[i] = svgFilePath;
            }
        }
        Runtime rt = Runtime.getRuntime();
        if (grMngr.gp != null) {
            grMngr.gp.setMessage("Computing layout...");
            grMngr.gp.setProgress(40);
        }
        try {
            try {
                File execDir = (new File(srcFilePath)).getParentFile();
                Process p = rt.exec(cmdArray, null, execDir);
                p.waitFor();
            } catch (IOException ex) {
                Process p = rt.exec(cmdArray);
                p.waitFor();
            }
        } catch (Exception e) {
//            JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), ZGRMessages.customCallExprError2 + Utils.join(cmdArray, " "),
//                                          "Command line call error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.WARNING, "{0}{1}", new Object[]{ZGRMessages.customCallExprError2(), Utils.join(cmdArray, " ")});
            LOGGER.log(Level.WARNING, "Error generating output SVG file.", e);
            return false;
        }
        return true;
    }

    void displaySVG(File sourceDotFileParentDir) {
        if (grMngr.gp != null) {
            grMngr.gp.setMessage("Parsing SVG...");
            grMngr.gp.setProgress(60);
        }

        Document svgDoc = Utils.parse(svgF, false);

        if (grMngr.gp != null) {
            grMngr.gp.setMessage("Displaying...");
            grMngr.gp.setProgress(80);
        }

        try {
            // going through URI and then URL as advised in JDK 1.6
            SVGReader.load(svgDoc, grMngr.mSpace, true, svgF.toURI().toURL().toString(), sourceDotFileParentDir.toURI().toURL().toString());
            grMngr.seekBoundingBox();
            grMngr.buildLogicalStructure();
        } catch (MalformedURLException ex) {
//            JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), svgF.getPath(),
//                                          ZGRMessages.SVG_PARSING_ERROR, JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.WARNING, ZGRMessages.SVG_PARSING_ERROR(), ex);
            System.err.println("Error loading SVG file.\n");
        }
    }

    void displayDOT() throws Exception {
//        try {
        throw new UnsupportedOperationException("Not implemented yet.");
        /*
         * grMngr.gp.setMessage("Parsing Augmented DOT...");
         * grMngr.gp.setProgress(60); DataInputStream graphInput = new
         * DataInputStream(new FileInputStream( svgF)); DOTLexer graphLexer =
         * new DOTLexer(graphInput); DOTParser graphParser = new
         * DOTParser(graphLexer); graphParser.graph(); CommonAST ast =
         * (CommonAST) graphParser.getAST(); DOTTreeParser graphWalker = new
         * DOTTreeParser(); graph = graphWalker.graph(ast);
         * grMngr.gp.setMessage("Displaying..."); grMngr.gp.setProgress(80);
         * ZgrReader.load(graph, grMngr.vsm, grMngr.mSpace, true);
         */
//        } catch (NullPointerException ex) {
//            JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), ZGRMessages.ERROR_LOADING_DOT_FILE, "Error", JOptionPane.ERROR_MESSAGE);
//        }
    }

//    /*checks that the command line options do not contain a -Txxx */
//    static String checkOptions(String options){
//	int i = options.indexOf("-T");
//	if (i!=-1){
//	    String res=options.substring(0,i);
//	    while (i<options.length() && options.charAt(i)!=' '){i++;}
//	    res+=options.substring(i);
//	    return res;
//	}
//	else return options;
//    }
    /**
     * A simple thread that will consume the stdout and stderr streams of a
     * process, to prevent deadlocks.
     *
     * @author David J. Hamilton <hamilton37@llnl.gov>
     */
    class ProcessOutputConsumer extends Thread {

        // Wrapping the inpustreams in readers because on my system
        // FileInputStream#skip would not actually consume any of the available
        // input
        private BufferedReader pout, perr;
        private long waitTime = 200;

        /**
         * @param p The process whose stdout and stderr streams are to be
         * consumed.
         * @throws IOException
         */
        public ProcessOutputConsumer(Process p) throws IOException {
            p.getOutputStream().close();

            pout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            perr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            setDaemon(true);
        }

        /**
         * @param p The process whose stdout and stderr streams are to be
         * consumed.
         * @param waitTime How long to wait (in ms) between checks for output to
         * consume.
         * @throws IOException
         */
        public ProcessOutputConsumer(Process p, long waitTime) throws IOException {
            this(p);
            this.waitTime = waitTime;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    while (pout.ready()) {
                        pout.readLine();
                    }
                    while (perr.ready()) {
                        perr.readLine();
                    }

                    if (waitTime > 0) {
                        sleep(waitTime);
                    }
                }
            } catch (IOException e) { /*
                 * do nothing
                 */ } catch (InterruptedException e) { /*
                 * do nothing
                 */

            }
        }

    }
}
