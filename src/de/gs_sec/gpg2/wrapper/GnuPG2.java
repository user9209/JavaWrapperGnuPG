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

import il.co.macnews.www.mageworks.java.gnupg.GnuPG;

import java.io.*;

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
 * @see        GnuPG - http://www.gnupg.org/
 */

public class GnuPG2 {

    private String homeDir = null;
    private String binGPG = null;

    public static final String batchCommand = "--yes --batch";
    public static final String cli = "%1 %2 %3 %4 %5 %6";
    public static final String listKeys = "--list-keys";

    public static final String importKey = "--import";
    public static final String fingerprint = "--import-options show-only --fingerprint --import";
    public static final String enc = "-a --output - --encrypt -r";
    public static final String dec = "-a --output - --decrypt";

    /**
     * @param binGPG  gpg binary
     * @param homeDir if null default is used
     */
    public GnuPG2(String binGPG, String homeDir) {
        this.binGPG = binGPG;
        this.homeDir = homeDir;
    }

    /**
     * Do not use, allays throwing an GnuPGException
     * @param homeDir path to gpg config home
     */
    @Deprecated
    public GnuPG2(String homeDir) {
        this.homeDir = homeDir;
        // todo: implement autoDetection
    }

    public String importKeyFile(String file) throws GnuPGException {
        return runGnuPG(fullCommand(importKey, file), null);
    }

    public String listKeys() throws GnuPGException {
        return runGnuPG(fullCommand(listKeys), null);
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

        GnuPG2.ProcessStreamReader psr_stdout = new GnuPG2.ProcessStreamReader(p.getInputStream());
        GnuPG2.ProcessStreamReader psr_stderr = new GnuPG2.ProcessStreamReader(p.getErrorStream());
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

            this.in = new InputStreamReader(in);

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
