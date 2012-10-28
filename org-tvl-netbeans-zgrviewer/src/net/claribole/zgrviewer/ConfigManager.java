/*
 * FILE: ConfigManager.java DATE OF CREATION: Thu Jan 09 14:14:35 2003 Copyright
 * (c) Emmanuel Pietriga, 2002. All Rights Reserved Copyright (c) INRIA,
 * 2004-2011. All Rights Reserved Licensed under the GNU LGPL. For full terms
 * see the file COPYING.
 *
 * $Id: ConfigManager.java 4276 2011-02-25 07:47:51Z epietrig $
 */
package net.claribole.zgrviewer;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

public class ConfigManager {

    private ConfigManager() {
    }

    public static final String PROP_ANTIALIASING = "antialiasing";

    public static final String zgrvURI = "http://zvtm.sourceforge.net/zgrviewer";
    public static final String MAIN_TITLE = "ZGRViewer";
//    static int mainViewW=800;
//    static int mainViewH=600;
//    static int mainViewX=0;
//    static int mainViewY=0;
    public static int rdW = 300;
    public static int rdH = 200;
    public static Font defaultFont = new Font("Dialog", 0, 12);
    public static final String _BLANK = "_blank";
    public static final String _SELF = "_self";
    @SuppressWarnings("StaticNonFinalUsedInInitialization")
    public static Font PIEMENU_FONT = defaultFont;
    public static Color backgroundColor = Color.WHITE;
    public static Color HIGHLIGHT_COLOR = Color.RED;
    public static Color OBSERVED_REGION_COLOR = new Color(186, 135, 186);
    public static Color OBSERVED_REGION_BORDER_COLOR = Color.getHSBColor(0.83519f, 0.28f, 0.45f); //rgb(299,28,45)
    public static Color OBSERVED_REGION_CROSSHAIR_COLOR = new Color(115, 83, 115);

    static {
        if (!(Utils.osIsWindows() || Utils.osIsMacOS())) {
            OBSERVED_REGION_BORDER_COLOR = OBSERVED_REGION_CROSSHAIR_COLOR = Color.RED;
        }
    }

    public static Color PIEMENU_FILL_COLOR = Color.BLACK;
    public static Color PIEMENU_BORDER_COLOR = Color.WHITE;
    public static Color PIEMENU_INSIDE_COLOR = Color.DARK_GRAY;
    public static final float PIEMENU_MAIN_ALPHA = 0.85f;
    public static final float PIEMENU_SUB_ALPHA = 0.95f;
    /*
     * Misc. Prefs
     */
//    static boolean SAVE_WINDOW_LAYOUT=false;
    public static boolean DELETE_TEMP_FILES = true;
    private static boolean antialiasing = false;
    public static boolean DYNASPOT = false;
    /*
     * add -q option in command line args, forcing dot/neato to remain silent
     * (do not issue warnings or errors that cause Java's runtime exec never to terminate)
     */
    public static boolean FORCE_SILENT = true;
//    static String CMD_LINE_OPTS="";
    //directories
    public static File m_TmpDir = new File(System.getProperty("java.io.tmpdir"));
    public static File m_PrjDir = new File("graphs");
    public static File m_DotPath = new File("dot");
    public static File m_NeatoPath = new File("neato");
    public static File m_CircoPath = new File("circo");
    public static File m_TwopiPath = new File("twopi");
    public static File m_GraphVizFontDir = new File("");
    public static File m_LastDir = null;
    public static File m_LastExportDir = null;
    /*
     * Plug in directory
     */
    public static File plugInDir = new File("plugins");
    static Plugin[] plugins;
    private static Map<String, Map<String, String>> tmpPluginSettings;
    static File lastFileOpened = null;

    /*
     * location of the configuration file - at init time, we look for it in the
     * user's home dir. If it is not there, we take the one in ZGRViewer dir.
     */
//    static String PREFS_FILE_NAME = ".zgrviewer";
//    static String OLD_PREFS_FILE_NAME = "zgrviewer.cfg";
    public static final int ANIM_MOVE_LENGTH = 300;

    /*
     * magnification factor when centering on a glyph - 1.0 (default) means that
     * the glyph will occupy the whole screen. mFactor < 1 will make the glyph
     * smaller (zoom out). mFactor > 1 will make the glyph appear bigger (zoom in)
     */
    public static float MAG_FACTOR = 2.0f;
//    /*External (platform-dependant) browser*/
//    //a class to access a platform-specific web browser (not initialized at startup, but only on demand)
//    static WebBrowser webBrowser;
//    //try to automatically detect browser (do not take browser path into account)
//    static boolean autoDetectBrowser=true;
//    //path to the browser's exec file
//    static File browserPath=new File("");
//    //browser command line options
//    static String browserOptions="";
//
//    /*proxy/firewall configuration*/
//    static boolean useProxy=false;
//    static String proxyHost="";    //proxy hostname
//    static String proxyPort="80";    //default value for the JVM proxyPort system property

    /*
     * speed-dependant autozoom data
     */
    public static double SD_ZOOM_THRESHOLD = 300;
    public static boolean SD_ZOOM_ENABLED = false;
    //default factor is 2
    public static double autoZoomFactor = 1;
    public static double autoUnzoomFactor = -0.5;
//    static List<String> LAST_COMMANDS;
//    static int COMMAND_LIMIT = 5;
//    GraphicsManager grMngr;

//    public ConfigManager(GraphicsManager gm, boolean applet) {
//        this.grMngr = gm;
////		LAST_COMMANDS = new ArrayList<String>();
//        if (!applet) {
//            m_TmpDir = new File(System.getProperty("java.io.tmpdir"));
//        }
//    }

    private static final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(ConfigManager.class);

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public static PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    public static PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    /*
     * load user prefs from config file (in theory, if the file cannot be found,
     * every variable should have a default value)
     */
    public static void loadConfig() {
        Preferences preferences = NbPreferences.forModule(ConfigManager.class);

//		File cfgFile = new File(System.getProperty("user.home") + File.separatorChar + PREFS_FILE_NAME);
//		if (!cfgFile.exists()){
//			cfgFile = new File(System.getProperty("user.home") + File.separatorChar + OLD_PREFS_FILE_NAME);
//		}
//		if (cfgFile.exists()){
//			System.out.println("Loading Preferences from : "+cfgFile.getAbsolutePath());
//			try {
//				Document d=Utils.parse(cfgFile, false);
//				d.normalize();
//				Element rt=d.getDocumentElement();
//				Element e=(Element)(rt.getElementsByTagNameNS(ConfigManager.zgrvURI,"directories")).item(0);
//				try {
//					ConfigManager.m_TmpDir=new File(e.getElementsByTagNameNS(ConfigManager.zgrvURI,"tmpDir").item(0).getFirstChild().getNodeValue());
//					ConfigManager.DELETE_TEMP_FILES=(Boolean.valueOf(((Element)e.getElementsByTagNameNS(ConfigManager.zgrvURI,"tmpDir").item(0)).getAttribute("value"))).booleanValue();
//				}
//				catch (Exception ex){}
//				try {ConfigManager.m_PrjDir=new File(e.getElementsByTagNameNS(ConfigManager.zgrvURI,"graphDir").item(0).getFirstChild().getNodeValue());}
//				catch (Exception ex){}
//				try {ConfigManager.m_DotPath=new File(e.getElementsByTagNameNS(ConfigManager.zgrvURI,"dot").item(0).getFirstChild().getNodeValue());}
//				catch (Exception ex){}
//				try {ConfigManager.m_NeatoPath=new File(e.getElementsByTagNameNS(ConfigManager.zgrvURI,"neato").item(0).getFirstChild().getNodeValue());}
//				catch (Exception ex){}
//				try {ConfigManager.m_CircoPath=new File(e.getElementsByTagNameNS(ConfigManager.zgrvURI,"circo").item(0).getFirstChild().getNodeValue());}
//				catch (Exception ex){}
//				try {ConfigManager.m_TwopiPath=new File(e.getElementsByTagNameNS(ConfigManager.zgrvURI,"twopi").item(0).getFirstChild().getNodeValue());}
//				catch (Exception ex){}
//				try {ConfigManager.m_GraphVizFontDir=new File(e.getElementsByTagNameNS(ConfigManager.zgrvURI,"graphvizFontDir").item(0).getFirstChild().getNodeValue());}
//				catch (Exception ex){}
//
//				//web browser settings
//				try {
//					e=(Element)(rt.getElementsByTagNameNS(ConfigManager.zgrvURI,"webBrowser")).item(0);
//					ConfigManager.autoDetectBrowser=Boolean.valueOf(e.getAttribute("autoDetect")).booleanValue();
//					ConfigManager.browserPath=new File(e.getAttribute("path"));
//					ConfigManager.browserOptions=e.getAttribute("options");
//				}
//				catch (Exception ex){}
//
//				//proxy settings
//				try {
//					e=(Element)(rt.getElementsByTagNameNS(ConfigManager.zgrvURI,"proxy")).item(0);
//					updateProxy(Boolean.valueOf(e.getAttribute("enable")).booleanValue(),
//						e.getAttribute("host"),e.getAttribute("port"));
//				}
//				catch (Exception ex){System.getProperties().put("proxySet","false");}

        m_TmpDir = new File(preferences.get("directories.tmpDir", m_TmpDir.getPath()));
        DELETE_TEMP_FILES = preferences.getBoolean("directories.tmpDir.deleteTempFiles", DELETE_TEMP_FILES);

        m_PrjDir = new File(preferences.get("directories.graphDir", m_PrjDir.getPath()));
        m_DotPath = new File(preferences.get("directories.dot", m_DotPath.getPath()));
        m_NeatoPath = new File(preferences.get("directories.neato", m_NeatoPath.getPath()));
        m_CircoPath = new File(preferences.get("directories.circo", m_CircoPath.getPath()));
        m_TwopiPath = new File(preferences.get("directories.twopi", m_TwopiPath.getPath()));
        m_GraphVizFontDir = new File(preferences.get("directories.graphvizFontDir", m_GraphVizFontDir.getPath()));

        //misc prefs
        setAntialiasing(preferences.getBoolean("antialiasing", isAntialiasing()));
        DYNASPOT = preferences.getBoolean("dynaspot", DYNASPOT);
        HIGHLIGHT_COLOR = new Color(preferences.getInt("highlightColor", HIGHLIGHT_COLOR.getRGB()));
        FORCE_SILENT = preferences.getBoolean("silent", FORCE_SILENT);

        setSDZoomEnabled(preferences.getBoolean("sdZoom", isSDZoomEnabled()));
        setSDZoomFactor(preferences.getDouble("sdZoomFactor", getSDZoomFactor()));
        setMagnificationFactor(preferences.getFloat("magFactor", getMagnificationFactor()));

//
//				try {
//					e=(Element)(rt.getElementsByTagNameNS(ConfigManager.zgrvURI,"preferences")).item(0);
//				}
//				catch (Exception ex){}
//				try {
//					ConfigManager.ANTIALIASING=((Boolean.valueOf(e.getAttribute("antialiasing"))).booleanValue());
//				}
//				catch (Exception ex){}
//				try {
//					ConfigManager.DYNASPOT=((Boolean.valueOf(e.getAttribute("dynaspot"))).booleanValue());
//				}
//				catch (Exception ex){}
//				try {
//					ConfigManager.HIGHLIGHT_COLOR = new Color((new Integer(e.getAttribute("highlightColor"))).intValue());
//				}
//				catch (Exception ex){}
//				try {
//					ConfigManager.SAVE_WINDOW_LAYOUT=(Boolean.valueOf(e.getAttribute("saveWindowLayout"))).booleanValue();
//				}
//				catch (Exception ex){}
//				try {
//					this.setSDZoomEnabled((Boolean.valueOf(e.getAttribute("sdZoom"))).booleanValue());
//				}
//				catch (Exception ex){}
//				try {
//					this.setSDZoomFactor(Integer.parseInt(e.getAttribute("sdZoomFactor")));
//				}
//				catch (Exception ex){}
//				try {
//					this.setMagnificationFactor(Float.parseFloat(e.getAttribute("magFactor")));
//				}
//				catch (Exception ex){}
//				try {
//					ConfigManager.CMD_LINE_OPTS=e.getAttribute("cmdL_options");
//				}
//				catch (Exception ex){}
//				try {
//					ConfigManager.FORCE_SILENT = ((Boolean.valueOf(e.getAttribute("silent"))).booleanValue());
//				}
//				catch (Exception ex){}
//				try {
//					if (ConfigManager.SAVE_WINDOW_LAYOUT){
//						//window layout preferences
//						e=(Element)(rt.getElementsByTagNameNS(ConfigManager.zgrvURI,"windows")).item(0);
//						mainViewX=(new Integer(e.getAttribute("mainX"))).intValue();
//						mainViewY=(new Integer(e.getAttribute("mainY"))).intValue();
//						mainViewW=(new Integer(e.getAttribute("mainW"))).intValue();
//						mainViewH=(new Integer(e.getAttribute("mainH"))).intValue();
//					}
//				}
//				catch (Exception ex){}
//				//plugin settings
//				try {
//					e = (Element)(rt.getElementsByTagNameNS(ConfigManager.zgrvURI, "plugins")).item(0);
//					if (e!=null){
//						loadPluginPreferences(e);
//					}
//				}
//				catch (Exception ex){System.err.println("Failed to set some plugin preferences");}
//				// stored command lines (for programs other than dot/neato)
//				LAST_COMMANDS.clear();
//				try {
//					NodeList nl = ((Element)(rt.getElementsByTagNameNS(ConfigManager.zgrvURI, "commandLines")).item(0)).getElementsByTagNameNS(ConfigManager.zgrvURI, "li");
//					for (int i=0;i<nl.getLength();i++){
//						if (i < COMMAND_LIMIT){LAST_COMMANDS.add(nl.item(i).getFirstChild().getNodeValue());}
//					}
//				}
//				catch (NullPointerException ex1){}
//
//
//			}
//			catch (Exception ex){
//				System.err.println("Error while loading ZGRViewer configuration file (" + cfgFile.getAbsolutePath() + "): ");
//				Exceptions.printStackTrace(ex);
//			}
//		}
//		else {System.out.println("No Preferences File Found in : "+System.getProperty("user.home"));}
    }

    public static boolean isAntialiasing() {
        return antialiasing;
    }

    public static void setAntialiasing(boolean value) {
        if (ConfigManager.antialiasing == value) {
            return;
        }

        ConfigManager.antialiasing = value;
        propertyChangeSupport.firePropertyChange(PROP_ANTIALIASING, !value, value);
    }

//    void loadPluginPreferences(Element pluginsEL){
//		NodeList nl = pluginsEL.getElementsByTagNameNS(ConfigManager.zgrvURI, "plugin");
//		Element pluginEL, settingEL;
//		Node txtVal;
//		String pluginName, settingName, settingValue;
//		Map<String, String> ht;
//		NodeList nl2;
//		tmpPluginSettings = new HashMap<String, Map<String, String>>();
//		for (int i=0;i<nl.getLength();i++){
//			pluginEL = (Element)nl.item(i);
//			pluginName = pluginEL.getAttribute("name");
//			ht = new HashMap<String, String>();
//			nl2 = pluginEL.getElementsByTagNameNS(ConfigManager.zgrvURI, "setting");
//			for (int j=0;j<nl2.getLength();j++){
//				settingEL = (Element)nl2.item(j);
//				try {
//					txtVal = settingEL.getFirstChild();
//					ht.put(settingEL.getAttribute("name"), (txtVal != null) ? txtVal.getNodeValue() : null);
//				}
//				catch (Exception ex){System.err.println("Failed to set some plugin preferences for "+pluginName);}
//			}
//			tmpPluginSettings.put(pluginName, ht);
//		}
//	}

    /*
     * save user prefs to config file
     */
    public static void saveConfig() {
        Preferences preferences = NbPreferences.forModule(ConfigManager.class);

        //m_TmpDir = new File(preferences.get("directories.tmpDir", m_TmpDir.getPath()));
        //DELETE_TEMP_FILES = preferences.getBoolean("directories.tmpDir.deleteTempFiles", DELETE_TEMP_FILES);
        //
        //m_PrjDir = new File(preferences.get("directories.graphDir", m_PrjDir.getPath()));
        //m_DotPath = new File(preferences.get("directories.dot", m_DotPath.getPath()));
        //m_NeatoPath = new File(preferences.get("directories.neato", m_NeatoPath.getPath()));
        //m_CircoPath = new File(preferences.get("directories.circo", m_CircoPath.getPath()));
        //m_TwopiPath = new File(preferences.get("directories.twopi", m_TwopiPath.getPath()));
        //m_GraphVizFontDir = new File(preferences.get("directories.graphvizFontDir", m_GraphVizFontDir.getPath()));
        //
        ////misc prefs
        //ANTIALIASING = preferences.getBoolean("antialiasing", ANTIALIASING);
        //DYNASPOT = preferences.getBoolean("dynaspot", DYNASPOT);
        //HIGHLIGHT_COLOR = new Color(preferences.getInt("highlightColor", HIGHLIGHT_COLOR.getRGB()));
        //FORCE_SILENT = preferences.getBoolean("silent", FORCE_SILENT);
        //
        //setSDZoomEnabled(preferences.getBoolean("sdZoom", isSDZoomEnabled()));
        //setSDZoomFactor(preferences.getDouble("sdZoomFactor", getSDZoomFactor()));
        //setMagnificationFactor(preferences.getFloat("magFactor", getMagnificationFactor()));

        preferences.put("directories.tmpDir", m_TmpDir.getPath());
        preferences.putBoolean("directories.tmpDir.deleteTempFiles", DELETE_TEMP_FILES);
        preferences.put("directories.graphDir", m_PrjDir.getPath());
        preferences.put("directories.dot", m_DotPath.getPath());
        preferences.put("directories.neato", m_NeatoPath.getPath());
        preferences.put("directories.circo", m_CircoPath.getPath());
        preferences.put("directories.twopi", m_TwopiPath.getPath());
        preferences.put("directories.graphvizFontDir", m_GraphVizFontDir.getPath());

        preferences.putBoolean("antialiasing", isAntialiasing());
        preferences.putBoolean("dynaspot", DYNASPOT);
        preferences.putInt("highlightColor", HIGHLIGHT_COLOR.getRGB());
        preferences.putBoolean("silent", FORCE_SILENT);

        preferences.putBoolean("sdZoom", isSDZoomEnabled());
        preferences.putDouble("sdZoomFactor", getSDZoomFactor());
        preferences.putFloat("magFactor", getMagnificationFactor());

        if (plugins != null) {
            for (int i = 0; i < plugins.length; i++) {
                plugins[i].savePreferences(preferences.node("plugins/" + plugins[i].getName()));
            }
        }
        
//		DOMImplementation di=new DOMImplementationImpl();
//		//DocumentType dtd=di.createDocumentType("isv:config",null,"isv.dtd");
//		Document cfg=di.createDocument(ConfigManager.zgrvURI,"zgrv:config",null);
//		//generate the XML document
//		Element rt=cfg.getDocumentElement();
//		rt.setAttribute("xmlns:zgrv",ConfigManager.zgrvURI);
//		//save directory preferences
//		Element dirs=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:directories");
//		rt.appendChild(dirs);
//		Element aDir=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:tmpDir");
//		aDir.appendChild(cfg.createTextNode(ConfigManager.m_TmpDir.toString()));
//		aDir.setAttribute("value",String.valueOf(ConfigManager.DELETE_TEMP_FILES));
//		dirs.appendChild(aDir);
//		aDir=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:graphDir");
//		aDir.appendChild(cfg.createTextNode(ConfigManager.m_PrjDir.toString()));
//		dirs.appendChild(aDir);
//		aDir=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:dot");
//		aDir.appendChild(cfg.createTextNode(ConfigManager.m_DotPath.toString()));
//		dirs.appendChild(aDir);
//		aDir=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:neato");
//		aDir.appendChild(cfg.createTextNode(ConfigManager.m_NeatoPath.toString()));
//		dirs.appendChild(aDir);
//		aDir = cfg.createElementNS(ConfigManager.zgrvURI, "zgrv:circo");
//		aDir.appendChild(cfg.createTextNode(ConfigManager.m_CircoPath.toString()));
//		dirs.appendChild(aDir);
//		aDir = cfg.createElementNS(ConfigManager.zgrvURI, "zgrv:twopi");
//		aDir.appendChild(cfg.createTextNode(ConfigManager.m_TwopiPath.toString()));
//		dirs.appendChild(aDir);
//		aDir=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:graphvizFontDir");
//		aDir.appendChild(cfg.createTextNode(ConfigManager.m_GraphVizFontDir.toString()));
//		dirs.appendChild(aDir);
//		//web settings
//		Element consts=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:webBrowser");
//		consts.setAttribute("autoDetect",String.valueOf(ConfigManager.autoDetectBrowser));
//		consts.setAttribute("path",ConfigManager.browserPath.toString());
//		consts.setAttribute("options",ConfigManager.browserOptions);
//		rt.appendChild(consts);
//		consts=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:proxy");
//		consts.setAttribute("enable",String.valueOf(ConfigManager.useProxy));
//		consts.setAttribute("host",ConfigManager.proxyHost);
//		consts.setAttribute("port",ConfigManager.proxyPort);
//		rt.appendChild(consts);
//		//save misc. constants
//		consts=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:preferences");
//		rt.appendChild(consts);
//		// 	consts.setAttribute("graphOrient",ConfigManager.GRAPH_ORIENTATION);
//		consts.setAttribute("antialiasing",String.valueOf(ConfigManager.ANTIALIASING));
//		consts.setAttribute("dynaspot",String.valueOf(ConfigManager.DYNASPOT));
//		consts.setAttribute("highlightColor", Integer.toString(HIGHLIGHT_COLOR.getRGB()));
//		consts.setAttribute("silent", String.valueOf(ConfigManager.FORCE_SILENT));
////		consts.setAttribute("saveWindowLayout",String.valueOf(ConfigManager.SAVE_WINDOW_LAYOUT));
//		consts.setAttribute("sdZoom",String.valueOf(SD_ZOOM_ENABLED));
//		consts.setAttribute("sdZoomFactor",String.valueOf(this.getSDZoomFactor()));
//		consts.setAttribute("magFactor", String.valueOf(this.getMagnificationFactor()));
////		consts.setAttribute("cmdL_options",ConfigManager.CMD_LINE_OPTS);
//		//window locations and sizes
//		if (ConfigManager.SAVE_WINDOW_LAYOUT){
//			//first update the values
//			updateWindowVariables();
//			consts=cfg.createElementNS(ConfigManager.zgrvURI,"zgrv:windows");
//			consts.setAttribute("mainX",String.valueOf(mainViewX));
//			consts.setAttribute("mainY",String.valueOf(mainViewY));
//			consts.setAttribute("mainW",String.valueOf(mainViewW));
//			consts.setAttribute("mainH",String.valueOf(mainViewH));
//			rt.appendChild(consts);
//		}
//		Element pluginsEL = cfg.createElementNS(ConfigManager.zgrvURI, "zgrv:plugins");
//		rt.appendChild(pluginsEL);
//		Map<String, String> pluginSettings;
//		Element pluginEL, settingEL;
//		String settingName, settingValue;
//		for (int i=0;i<plugins.length;i++){
//			pluginSettings = plugins[i].savePreferences();
//			if (pluginSettings != null && pluginSettings.size() > 0){
//				pluginEL = cfg.createElementNS(ConfigManager.zgrvURI, "zgrv:plugin");
//				pluginsEL.appendChild(pluginEL);
//				pluginEL.setAttribute("name", plugins[i].getName());
//				for (Iterator<String> e=pluginSettings.keySet().iterator();e.hasNext();){
//					settingName = e.next();
//					settingValue = pluginSettings.get(settingName);
//					settingEL = cfg.createElementNS(ConfigManager.zgrvURI, "zgrv:setting");
//					settingEL.setAttribute("name", settingName);
//					settingEL.appendChild(cfg.createTextNode(settingValue));
//					pluginEL.appendChild(settingEL);
//				}
//			}
//		}
//		// command lines
//		consts = cfg.createElementNS(ConfigManager.zgrvURI, "zgrv:commandLines");
//		rt.appendChild(consts);
//		if (LAST_COMMANDS != null){
//			for (int i=0;i<LAST_COMMANDS.size();i++){
//				Element aCommand = cfg.createElementNS(ConfigManager.zgrvURI, "zgrv:li");
//				aCommand.appendChild(cfg.createTextNode(LAST_COMMANDS.get(i)));
//				consts.appendChild(aCommand);
//			}
//		}
//		File cfgFile = new File(System.getProperty("user.home") + File.separatorChar + PREFS_FILE_NAME);
//		if (cfgFile.exists()){cfgFile.delete();}
//		Utils.serialize(cfg, FileUtil.toFileObject(cfgFile));
    }

    /*
     * save command lines on exit, without modifying user settings if he did not
     * ask to do so
     */
//	void saveCommandLines(){
//		try {
//			Document d;
//			Element rt;
//			Element cLines;
//			File cfgFile = new File(System.getProperty("user.home") + File.separatorChar + PREFS_FILE_NAME);
//			if (!cfgFile.exists()){
//				cfgFile = new File(System.getProperty("user.home") + File.separatorChar + OLD_PREFS_FILE_NAME);
//			}
//			if (cfgFile.exists()){
//				d = Utils.parse(cfgFile, false);
//				d.normalize();
//				rt = d.getDocumentElement();
//				if ((rt.getElementsByTagNameNS(ConfigManager.zgrvURI, "commandLines")).getLength()>0){
//					rt.removeChild((rt.getElementsByTagNameNS(ConfigManager.zgrvURI, "commandLines")).item(0));
//				}
//				cLines = d.createElementNS(ConfigManager.zgrvURI, "zgrv:commandLines");
//				if (LAST_COMMANDS != null){
//					for (int i=0;i<LAST_COMMANDS.size();i++){
//						Element aCmdLine = d.createElementNS(ConfigManager.zgrvURI, "zgrv:li");
//						aCmdLine.appendChild(d.createTextNode(LAST_COMMANDS.get(i)));
//						cLines.appendChild(aCmdLine);
//					}
//				}
//			}
//			else {
//				DOMImplementation di = new DOMImplementationImpl();
//				d = di.createDocument(ConfigManager.zgrvURI, "zgrv:config", null);
//				rt = d.getDocumentElement();
//				rt.setAttribute("xmlns:zgrv", ConfigManager.zgrvURI);
//				cLines = d.createElementNS(ConfigManager.zgrvURI, "zgrv:commandLines");
//				if (LAST_COMMANDS != null){
//					for (int i=0;i<LAST_COMMANDS.size();i++){
//						Element aCmdLine = d.createElementNS(ConfigManager.zgrvURI, "zgrv:li");
//						aCmdLine.appendChild(d.createTextNode(LAST_COMMANDS.get(i)));
//						cLines.appendChild(aCmdLine);
//					}
//				}
//			}
//			rt.appendChild(cLines);
//			cfgFile = new File(System.getProperty("user.home") + File.separatorChar + PREFS_FILE_NAME);
//			Utils.serialize(d, FileUtil.toFileObject(cfgFile));
//		}
//		catch (Exception ex){}
//	}
    static boolean checkProgram(short prg) {
        switch (prg) {
        case DOTManager.DOT_PROGRAM: {
            return (m_TmpDir.exists() && ConfigManager.m_DotPath.exists());
        }
        case DOTManager.NEATO_PROGRAM: {
            return (m_TmpDir.exists() && ConfigManager.m_NeatoPath.exists());
        }
        case DOTManager.TWOPI_PROGRAM: {
            return (m_TmpDir.exists() && ConfigManager.m_TwopiPath.exists());
        }
        case DOTManager.CIRCO_PROGRAM: {
            return (m_TmpDir.exists() && ConfigManager.m_CircoPath.exists());
        }
        default: {
            return false;
        }
        }
    }

    static String getDirStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Temp Directory (required): ");
        sb.append((m_TmpDir.exists()) ? m_TmpDir.toString() : ZGRMessages.PATH_NOT_SET());
        sb.append("\n");
        sb.append("Absolute Path to dot (required if using dot): ");
        sb.append((m_DotPath.exists()) ? m_DotPath.toString() : ZGRMessages.PATH_NOT_SET());
        sb.append("\n");
        sb.append("Absolute Path to neato (required if using neato): ");
        sb.append((m_NeatoPath.exists()) ? m_NeatoPath.toString() : ZGRMessages.PATH_NOT_SET());
        sb.append("\n");
        sb.append("Absolute Path to circo (required if using circo): ");
        sb.append((m_CircoPath.exists()) ? m_CircoPath.toString() : ZGRMessages.PATH_NOT_SET());
        sb.append("\n");
        sb.append("Absolute Path to twopi (required if using twopi): ");
        sb.append((m_TwopiPath.exists()) ? m_TwopiPath.toString() : ZGRMessages.PATH_NOT_SET());
        sb.append("\n");
        sb.append("GraphViz Font Directory (optional): ");
        sb.append((m_GraphVizFontDir.exists()) ? m_GraphVizFontDir.toString() : ZGRMessages.PATH_NOT_SET());
        sb.append("\n");
        sb.append("Are you sure you want to continue?");
        return sb.toString();
    }

//    /*update window position and size variables prior to saving them in the config file*/
//    void updateWindowVariables(){
//		mainViewX = grMngr.mainView.getFrame().getX();
//		mainViewY = grMngr.mainView.getFrame().getY();
//		mainViewW = grMngr.mainView.getFrame().getWidth();
//		mainViewH = grMngr.mainView.getFrame().getHeight();
//	}

    /*
     * set speed-dependent automatic zooming
     */
    public static void setSDZoomEnabled(boolean b) {
        SD_ZOOM_ENABLED = b;
    }

    /*
     * speed-dependent automatic zooming status
     */
    public static boolean isSDZoomEnabled() {
        return SD_ZOOM_ENABLED;
    }

    /*
     * amount of autozoom ; f belongs to [2.0, 10.0] <- values allowed by Pref
     * window slider
     */
    public static void setSDZoomFactor(double f) {
        autoZoomFactor = f - 1;
        autoUnzoomFactor = (1 - f) / f;
    }

    /*
     * amount of autozoom ; res belongs to [2.0, 10.0]
     */
    public static int getSDZoomFactor() {
        return (int)autoZoomFactor + 1;
    }

    /*
     * threshold beyond which autozooming is triggered (usually a percentage of
     * the View's size in pixels)
     */
    public static void setSDZoomThreshold(double t) {
        SD_ZOOM_THRESHOLD = t;
    }

    public static double getSDZoomThreshold() {
        return SD_ZOOM_THRESHOLD;
    }

    public static void setMagnificationFactor(float f) {
        MAG_FACTOR = f;
    }

    public static float getMagnificationFactor() {
        return MAG_FACTOR;
    }

//    /*remember command lines input in CallBox*/
//    void rememberCommandLine(String cmdLine){
//		boolean exists = false;
//		for (int i=0;i<LAST_COMMANDS.size();i++){
//			if ((LAST_COMMANDS.get(i)).equals(cmdLine)){
//				if (i > 0){
//					String tmp = LAST_COMMANDS.get(0);
//					LAST_COMMANDS.set(0, cmdLine);
//					LAST_COMMANDS.set(i, tmp);
//				}
//				return;
//			}
//		}
//		LAST_COMMANDS.add(0, cmdLine);
//		if (LAST_COMMANDS.size() > COMMAND_LIMIT){LAST_COMMANDS.remove(COMMAND_LIMIT);}  //we limit the list to COMMAND_LIMIT elements
//	}
//    /*could also be set at runtime from command line
//      java -DproxySet=true -DproxyHost=proxy_host -DproxyPort=proxy_port*/
//    static void updateProxy(boolean use,String hostname,String port){
//		ConfigManager.useProxy=use;
//		ConfigManager.proxyHost=hostname;
//		ConfigManager.proxyPort=port;
//		if (ConfigManager.useProxy){
//			System.getProperties().put("proxySet","true");
//			System.getProperties().put("proxyHost",ConfigManager.proxyHost);
//			System.getProperties().put("proxyPort",ConfigManager.proxyPort);
//		}
//		else {
//			System.getProperties().put("proxySet","false");
//		}
//	}

    /*
     * ------------------------ Plugins -----------------------------
     */
    static File[] pluginJARs = null;

    static void setPlugInJARs(String[] list) {
        pluginJARs = new File[list.length];
        for (int i = 0; i < list.length; i++) {
            pluginJARs[i] = new File(list[i]);
        }
    }

    public static void initPlugins(ZGRApplication application) {
        if (true) {
            plugins = new Plugin[0];
            return;
        }

        List<Plugin> plgs = new ArrayList<Plugin>();
        //list all files in 'plugins' dir
        if (pluginJARs == null) {
            pluginJARs = ConfigManager.plugInDir.listFiles();
        }
        if (pluginJARs != null && pluginJARs.length > 0) {
            URL[] urls = new URL[pluginJARs.length];
            //store path to each JAR file in plugins dir as a URL so that they can be added
            //later dynamically to the classpath (through a new ClassLoader)
            for (int i = 0; i < pluginJARs.length; i++) {
                try {
                    // going through URI and then URL as advised in JDK 1.6
                    urls[i] = pluginJARs[i].toURI().toURL();
                } catch (MalformedURLException mue) {
                    System.err.println("Failed to instantiate a class loader for plug-ins: " + mue);
                }
            }
            //instantiate a new class loader with a classpath containing all JAR files in plugins directory
            URLClassLoader ucl = new URLClassLoader(urls);
            JarFile jf;
            String s;
            //for each of these JAR files
            for (int i = 0; i < pluginJARs.length; i++) {
                try {
                    jf = new JarFile(pluginJARs[i]);
                    //get all CLASS entries
                    for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
                        s = e.nextElement().getName();
                        if (s.endsWith(".class")) {
                            //replace directory / by package .
                            s = Utils.replaceString(s, "/", ".");
                            //get rid of .class at the end of the jar entry
                            s = s.substring(0, s.length() - 6);
                            try {
                                //for each class entry, get the Class definition
                                Class<?> c = ucl.loadClass(s);
                                if (Plugin.class.isAssignableFrom(c)) {
                                    try {
                                        //find out if it implements Plugin (if it does, instantiate and store it)
                                        Plugin plgInstance = (Plugin)c.newInstance();
                                        System.out.print("Loading plugin: " + plgInstance.getName() + " " + plgInstance.getVersion() + "... ");
                                        plgInstance.setApplication(application);
                                        plgs.add(plgInstance);
                                        System.out.println("OK");
                                    } catch (InstantiationException ie) {
                                        System.err.println("Unable to create plug-in object for class "
                                            + s + ": " + ie.getMessage());
                                        Exceptions.printStackTrace(ie);
                                    } catch (IllegalAccessException ie) {
                                        System.err.println("Unable to create plug-in object for class "
                                            + s + ": " + ie.getMessage());
                                        Exceptions.printStackTrace(ie);
                                    }
                                }
                            } catch (ClassNotFoundException ex) {
                                System.err.println("Failed to load plug-in class " + s);
                            }
                        }
                    }
                } catch (IOException ex2) {
                    System.err.println("Failed to load plug-in from JAR file " + pluginJARs[i].getPath());
                } catch (NoClassDefFoundError ex2) {
                    System.err.println("One or more plugins might have failed to initialize because of the following error:\nNo Class Definition Found for " + ex2.getMessage());
                } catch (ClassFormatError ex2) {
                    System.err.println("One or more plugins might have failed to initialize because of the following error:\nClass Format Error for " + ex2.getMessage());
                }
            }
        }

        Preferences rootPreferences = NbPreferences.forModule(ConfigManager.class);

        //store the plugins in arrays instead of vectors
        plugins = new Plugin[plgs.size()];
        for (int i = 0; i < plgs.size(); i++) {
            plugins[i] = plgs.get(i);
            try {
                plugins[i].loadPreferences(rootPreferences.node("plugins/" + plugins[i].getName()));
            } catch (NullPointerException ex) {
            }
        }
    }

    // event should be one of Plugin.NOTIFY_PLUGIN_*;
    public static void notifyPlugins(short event) {
        for (int i = 0; i < plugins.length; i++) {
            plugins[i].eventOccured(event);
        }
    }

    public static void terminatePlugins() {
        for (int i = 0; i < plugins.length; i++) {
            plugins[i].terminate();
        }
    }

    static void showPluginInfo() {
        StringBuffer info = new StringBuffer();
        for (int i = 0; i < plugins.length; i++) {
            info.append(plugins[i].getName()).append("\nVersion: ").append(plugins[i].getVersion()).append("\nAuthors: ").append(plugins[i].getAuthor()).append("\n");

            if (plugins[i].getURL() != null) {
                info.append("More information:").append(plugins[i].getURL().toString()).append("\n");
            }

            info.append("\n");
        }

        new TextViewer(info, ZGRMessages.ABOUT_PLUGINS(), 0, 0, 0, 400, 300, false);
    }

}
