package de.gs_sec.gpg2.wrapper;

public class OS {

    //private static
    private static os OS = null;
    private static String osName = null;

    enum os {WINDOWS, LINUX, MAC,UNKNOWN, SOLARIS}

    public static boolean isWindows() {
        check();
        return OS == os.WINDOWS;
    }

    public static String getOsName() {
        if(osName == null)
            osName = getOsNameInt();

        return osName;
    }

    private static String getOsNameInt() {
        return System.getProperty("os.name").toLowerCase();
    }

    private static void check() {
        if(OS == null)
        {
            String osString = getOsNameInt();

            if(osString.contains("win")) {
                OS = os.WINDOWS;
            }
            else if(osString.contains("mac")) {
                OS = os.MAC;
            }
            else if(osString.contains("nix") || osString.contains("nux") || osString.contains("aix")) {
                OS = os.LINUX;
            }
            else if(osString.contains("sunos")) {
                OS = os.SOLARIS;
            }
            else {
                OS = os.UNKNOWN;
            }
        }
    }

    public static boolean isLinux() {
        return OS == os.LINUX;
    }

    public static boolean isMac() {
        return OS == os.MAC;
    }

    public static boolean isSolaris() {
        return OS == os.SOLARIS;
    }
}
