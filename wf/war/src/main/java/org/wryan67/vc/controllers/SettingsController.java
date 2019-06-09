package org.wryan67.vc.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsController {

    public static String getRelease() {
        String release=null;

        try {
            Process process = new ProcessBuilder("unzip","-p","../standalone/deployments/vc.ear","META-INF/maven/org.wryan67.vc/ear/pom.xml").start();

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            boolean skip=false;
            String line;
            while ((line=in.readLine())!=null) {
//                System.out.println(line);
                if (line.contains("parent")) skip=!skip;

                if (!skip && release==null && line.contains("<version>")) {
                    Pattern pattern = Pattern.compile("<version>(.*?)</version>");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find())
                    {
                        release=matcher.group(1);
                    }
                }
            }
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (release==null)?"unknown":release;
    }

    public static boolean process(HttpServletRequest request, HttpServletResponse response) {

        return false;
    }
}
