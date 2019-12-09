package de.gs_sec.gpg2.wrapper;

/*
    Copyright (C) 2002 John Anderson using GPL
       See: https://lists.gnupg.org/pipermail/gnupg-devel/2002-February/018098.html

    Copyright (C) 2019  Georg Schmidt <gs-develop@gs-sys.de>
    Copyright (C) 2004  Yaniv Yemini
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A class that implements PGP interface for Java.
 * <P>
 * It calls gpg (GnuPG) program to do all the PGP processing.
 * $Id:$
 *
 * @author	Georg Schmidt, Dezember 2019.
 * @author  Updated the code and tested with gpg (GnuPG) 2.2.17 - libgcrypt 1.8.4 on windows
 * @author  .
 * @author	Yaniv Yemini, January 2004.
 * @author	Please include this text with any copy of this code.
 * @author	.
 * @author	License: GPL v3
 * @author	Latest version of this code can be found at:
 * @author	http://www.macnews.co.il/mageworks/java/gnupg/
 * @author	.
 * @author	Based on a class GnuPG by John Anderson, which can be found
 * @author	at: http://lists.gnupg.org/pipermail/gnupg-devel/2002-February/018098.html
 * @version	0.5
 * @see     <a href="http://www.gnupg.org/">GnuPG</a>
 */

public class GnuPG2 {

    private String homeDir = null;
    private String binGPG = null;

    public static final String[] winPaths = new String[] {
            "C:\\Program Files (x86)\\GnuPG\\bin\\gpg.exe",
            "C:\\Program Files\\GnuPG\\bin\\gpg.exe",
    };

    public static final String[] linuxPaths = new String[] {
        "gpg2",
        "/usr/bin/gpg2",
        "gpg",
        "/usr/bin/gpg",
    };


    public static final String batchCommand = "--yes --batch";
    public static final String cli = "%1 %2 %3 %4 %5 %6";
    public static final String listKeys = "--list-keys";

    public static final String importKey = "--import";
    public static final String fingerprint = "--import-options show-only --fingerprint --import";
    public static final String enc = "-a --output - --encrypt -r"; // --throw-keyids
    public static final String encSign = "-a --output - --encrypt --sign -r"; // --throw-keyids
    public static final String dec = "-a --output - --decrypt"; // --try-all-secrets

    // todo: Does not ensure that a signature exists!
    public static final String decVerify = "-a --output - --decrypt"; // --try-all-secrets

    /**
     * @param binGPG  gpg binary
     * @param homeDir if null default is used
     * @see GnuPG2#GnuPG2(String)
     */
    public GnuPG2(String binGPG, String homeDir) {
        this.binGPG = binGPG;
        this.homeDir = homeDir;
    }

    /**
     *
     * @param homeDir path to gpg config home
     */
    public GnuPG2(String homeDir) {
        this.homeDir = homeDir;
        this.binGPG = findGPG();
    }

    public static String findGPG() {
        if (OS.isWindows())
        {
            for (String path:  winPaths) {
                if (Files.exists(Paths.get(path)))
                    return "\"" + path + "\"";
            }
        }

        if (OS.isLinux())
        {
            for (String path:  linuxPaths) {
                if (Files.exists(Paths.get(path)))
                    return path.replaceAll(" ","\\ ");
            }
        }

        return null;
    }

    public String importKeyFile(String file) throws GnuPGException {
        return runGnuPG(fullCommand(importKey, file), null);
    }

    public String listKeys() throws GnuPGException {
        return runGnuPG(fullCommand(listKeys), null).replaceFirst(".+\r?\n.+\r?\n","");
    }

    public String[] getKeyList() throws GnuPGException {
        String result = runGnuPG(fullCommand( listKeys) , null).replaceAll("\r\n","\n");
        result = result.replaceFirst(".+\n.+\n","");

        return result.split(" *\n\n *");
    }

    public String getCliInteractiveCommand() {
        return fullCommand(cli);
    }

    public String fingerprintKeyFile(String file) throws GnuPGException {
        String result = runGnuPG(fullCommand(fingerprint, file), null);
        return result.toUpperCase().replaceAll(
                "(?s).*([0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *" +
                        "[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4}).*", "$1")
                .replaceAll(" ", "");
    }

    public String fingerprint(String keydata) throws GnuPGException {
        String result = runGnuPG(fullCommand(fingerprint), keydata);
        return result.toUpperCase().replaceAll(
                "(?s).*([0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *" +
                        "[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4} *[0-9A-Z]{4}).*", "$1")
                .replaceAll(" ", "");
    }

    public String encrypt(String data, String receiver) throws GnuPGException {
        return runGnuPG(fullCommand(enc, receiver), data);

    }

    public String decrypt(String data) throws GnuPGException {
        return runGnuPG(fullCommand(dec), data);
    }

    public String encryptAndSign(String data, String receiver) throws GnuPGException {
        return runGnuPG(fullCommand(encSign, receiver), data);

    }

    /**
     * Does not ensure that a signature exists!
     * @param data
     * @return
     * @throws GnuPGException
     */
    public String decryptAndVerify(String data) throws GnuPGException {
        return runGnuPG(fullCommand(decVerify), data);
    }

    public String fullCommand(String command) {
        return binGPG + " " + batchCommand + " " + homeDir + " " + command;
    }

    public String fullCommand(String command, String sub) {
        return binGPG + " " + batchCommand + " " + homeDir + " " + command + " " + sub;
    }

    /**
     * Runs GnuPG external program
     *
     * @param fullCommand
     * @param stdIn
     * @return
     * @throws GnuPGException
     */
    private String runGnuPG(String fullCommand, String stdIn) throws GnuPGException {
        if (binGPG == null) {
            throw new GnuPGException("No GnuPG binary has found!");
        }

        Process p;

        try {
            p = Runtime.getRuntime().exec(fullCommand);
        } catch (IOException io) {
            throw new GnuPGException("io Error " + io.getMessage());
        }

        GnuPG2.ProcessStreamReader psr_stdout;
        GnuPG2.ProcessStreamReader psr_stderr;
        if(OS.isWindows())
        {
            try {
                psr_stdout = new ProcessStreamReader(p.getInputStream(), "CP437");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                psr_stdout = new GnuPG2.ProcessStreamReader(p.getInputStream());
            }
            try {
                psr_stderr = new ProcessStreamReader(p.getErrorStream(), "CP437");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                psr_stderr = new GnuPG2.ProcessStreamReader(p.getErrorStream());
            }

        }
        else {
            psr_stdout = new GnuPG2.ProcessStreamReader(p.getInputStream());
            psr_stderr = new GnuPG2.ProcessStreamReader(p.getErrorStream());
        }

        psr_stdout.start();
        psr_stderr.start();
        if (stdIn != null) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            try {
                out.write(stdIn);
                out.close();
            } catch (IOException io) {
                throw new GnuPGException("Exception at write! " + io.getMessage());
            }
        }

        try {
            p.waitFor();

            psr_stdout.join();
            psr_stderr.join();
        } catch (InterruptedException i) {
            throw new GnuPGException("Exception at waitfor! " + i.getMessage());
        }

        int gpg_exitCode = 0;
        try {
            gpg_exitCode = p.exitValue();
        } catch (IllegalThreadStateException itse) {
            throw new GnuPGException("Exit code fails " + itse.getMessage());
        }

        if (gpg_exitCode != 0) {
            throw new GnuPGException("Exit code is " + gpg_exitCode + " Msg: " + psr_stderr.getString());
        }

        // System.out.println("Error-Text: " + psr_stderr.getString());
        return psr_stdout.getString();
    }


    /**
     * Reads an output stream from an external process.
     * Imeplemented as a thred.
     */
    private static class ProcessStreamReader
            extends Thread {
        StringBuffer stream;
        InputStreamReader in;

        final static int BUFFER_SIZE = 1024;

        /**
         * Creates new ProcessStreamReader object.
         *
         * @param    in
         */
        ProcessStreamReader(InputStream in) {
            super();

            this.in = new InputStreamReader(in, StandardCharsets.UTF_8);
            this.stream = new StringBuffer();
        }

        ProcessStreamReader(InputStream in, String encoding) throws UnsupportedEncodingException {
            super();

            this.in = new InputStreamReader(in, encoding);
            this.stream = new StringBuffer();
        }

        ProcessStreamReader(InputStreamReader in) {
            super();

            this.in = in;
            this.stream = new StringBuffer();
        }

        public void run() {
            try {
                int read;
                char[] c = new char[BUFFER_SIZE];

                while ((read = in.read(c, 0, BUFFER_SIZE - 1)) > 0) {
                    stream.append(c, 0, read);
                    if (read < BUFFER_SIZE - 1) break;
                }
            } catch (IOException io) {
            }
        }

        String getString() {
            return stream.toString();
        }
    }
}
