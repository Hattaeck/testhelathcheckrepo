package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        boolean jsonMode = false;
        boolean verbose = false;

        for (String a : args) {
            if (a.equals("--json")) {
                jsonMode = true;
            } else if (a.equals("--verbose")) {
                verbose = true;
            }
        }


        Properties cfg = loadConfig();

        String ip = cfg.getProperty("server.ip");
        int mmPort = Integer.parseInt(cfg.getProperty("mattermost.port", "8065"));
        int sshPort = Integer.parseInt(cfg.getProperty("ssh.port", "50222"));
        String sshUser = cfg.getProperty("ssh.user");
        String sshKeyPath = cfg.getProperty("ssh.key.path");
        String sshKeyPass = cfg.getProperty("ssh.key.passphrase", "");

        int timeoutMs = Integer.parseInt(cfg.getProperty("timeout.ms", "2000"));

        List<CheckResult> results = new ArrayList<>();
        results.add(Checks.mattermostPing(ip, mmPort, timeoutMs));
        results.add(Checks.tcpPort(ip, sshPort, timeoutMs));

        Level overall = calcOverall(results);

        if (!jsonMode) {
            System.out.println("== External checker ==");

            if (verbose) {
                System.out.println("Target: http://" + ip + ":" + mmPort);
                System.out.println("SSH port: " + sshPort);
            } else {
                System.out.println("Target: configured");
                System.out.println("SSH port: configured");
            }

            System.out.println();


            for (CheckResult r : results) {
                System.out.println("[" + r.level + "] " + r.name + " - " + r.message);
            }

            System.out.println();
            System.out.println("Overall: " + overall);
        } else {
            // Одна строка JSON (удобно парсить)
            System.out.println(toJson(overall, results));
        }

        // Exit code по стандарту: OK=0, WARN=1, CRIT=2
        if (overall == Level.OK) {
            System.exit(0);
        } else if (overall == Level.WARN) {
            System.exit(1);
        } else {
            System.exit(2);
        }
    }

    private static Level calcOverall(List<CheckResult> results) {
        Level overall = Level.OK;
        for (CheckResult r : results) {
            if (r.level == Level.CRIT) {
                overall = Level.CRIT;
            } else if (r.level == Level.WARN && overall != Level.CRIT) {
                overall = Level.WARN;
            }
        }
        return overall;
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                System.out.println("{\"overall\":\"CRIT\",\"error\":\"config.properties not found. Copy src/main/resources/config.example.properties to config.properties\"}");
                System.exit(2);
            }
            p.load(is);
        } catch (IOException e) {
            System.out.println("{\"overall\":\"CRIT\",\"error\":\"cannot read config.properties\"}");
            System.exit(2);
        }
        return p;
    }

    private static String toJson(Level overall, List<CheckResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"ts\":\"").append(escapeJson(Instant.now().toString())).append("\",");
        sb.append("\"overall\":\"").append(overall).append("\",");
        sb.append("\"items\":[");
        for (int i = 0; i < results.size(); i++) {
            CheckResult r = results.get(i);
            sb.append("{");
            sb.append("\"name\":\"").append(escapeJson(r.name)).append("\",");
            sb.append("\"level\":\"").append(r.level).append("\",");
            sb.append("\"message\":\"").append(escapeJson(r.message)).append("\"");
            sb.append("}");
            if (i < results.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
