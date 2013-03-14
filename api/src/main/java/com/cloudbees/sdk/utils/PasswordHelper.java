/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk.utils;

import java.io.*;

/**
 * @author Fabian Donze
 */
public class PasswordHelper {

    public static String prompt(String prompt) {
        try {
            return prompt1(prompt);
        } catch (Error e) {
            return prompt2(prompt);
        }
    }

    private static String prompt1(String prompt) {
        Console con = System.console();
        if (con != null) {
            return String.valueOf(con.readPassword(prompt));
        }
        return null;
    }

    /**
     * @param prompt The prompt to display to the user
     * @return The password as entered by the user
     */
    private static String prompt2(String prompt) {
        MaskingThread et = new MaskingThread(prompt);
        Thread mask = new Thread(et);
        mask.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String password = "";

        try {
            password = in.readLine();
        } catch (IOException ignored) {
        }
        // stop masking
        et.stopMasking();
        // return the password entered by the user
        return password;
    }
}


/**
 * This class attempts to erase characters echoed to the console.
 */

class MaskingThread extends Thread {
    private volatile boolean stop;
    private char echochar = ' ';

    /**
     * @param prompt The prompt displayed to the user
     */
    public MaskingThread(String prompt) {
        System.out.print(prompt);
    }

    /**
     * Begin masking until asked to stop.
     */
    public void run() {

        int priority = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        try {
            stop = true;
            while (stop) {
                System.out.print("\010" + echochar);
                try {
                    // attempt masking at this rate
                    Thread.currentThread().sleep(2);
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } finally { // restore the original priority
            Thread.currentThread().setPriority(priority);
        }
    }

    /**
     * Instruct the thread to stop masking.
     */
    public void stopMasking() {
        this.stop = false;
    }
}