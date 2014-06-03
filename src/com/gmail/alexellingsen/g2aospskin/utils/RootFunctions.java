package com.gmail.alexellingsen.g2aospskin.utils;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RootFunctions {

    private static String execute(List<String> cmds) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(process.getOutputStream());

        if (cmds != null) {
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd + "\n");
            }
        }

        os.writeBytes("exit\n");
        os.flush();

        String input = readToEnd(process.getInputStream());

        os.close();

        process.waitFor();

        return input;
    }

    public static void killApp(final String packageName) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ArrayList<String> cmds = new ArrayList<String>();

                cmds.add("am kill " + packageName);

                try {
                    execute(cmds);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();
    }

    private static String readToEnd(InputStream stream) {
        StringBuilder sb = new StringBuilder();

        int b;
        DataInputStream is = new DataInputStream(stream);

        try {
            while ((b = is.read()) != -1) {
                sb.append((char) b);
            }
        } catch (IOException e) {
            Log.e("myTag", "Error", e);
        }

        return sb.toString();
    }
}
