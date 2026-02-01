package com.example;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Checks {

    public static CheckResult mattermostPing(String ip, int port, int timeoutMs) {
        String urlStr = "http://" + ip + ":" + port + "/api/v4/system/ping";

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            String body = readAll(conn);

            if (code != 200) {
                return new CheckResult("mm_ping", Level.CRIT, "HTTP " + code);
            }

            boolean statusOk = body.contains("\"status\"") && body.contains("\"OK\"");
            if (statusOk) {
                return new CheckResult("mm_ping", Level.OK, "HTTP 200 + status OK");
            } else {
                return new CheckResult("mm_ping", Level.WARN, "HTTP 200 but status not OK");
            }

        } catch (IOException e) {
            return new CheckResult("mm_ping", Level.CRIT, e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static CheckResult tcpPort(String ip, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeoutMs);
            return new CheckResult("tcp_" + port, Level.OK, "reachable");
        } catch (IOException e) {
            return new CheckResult("tcp_" + port, Level.CRIT, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static CheckResult sshAuth(
            String host,
            int port,
            String user,
            String keyPath,
            String passphrase,
            int timeoutMs
    ) {
        SSHClient ssh = new SSHClient();

        try {
            // Учебный режим: НЕ проверяем known_hosts (позже сделаем строго)
            ssh.setConnectTimeout(timeoutMs);

            ssh.connect(host, port);

            KeyProvider keys = (passphrase == null || passphrase.isEmpty())
                    ? ssh.loadKeys(keyPath)
                    : ssh.loadKeys(keyPath, passphrase);

            ssh.authPublickey(user, keys);

            if (ssh.isAuthenticated()) {
                return new CheckResult("ssh_auth", Level.OK, "authenticated");
            } else {
                return new CheckResult("ssh_auth", Level.CRIT, "not authenticated");
            }

        } catch (Exception e) {
            return new CheckResult("ssh_auth", Level.CRIT, e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            try { ssh.disconnect(); } catch (Exception ignored) {}
        }
    }

    private static String readAll(HttpURLConnection conn) throws IOException {
        InputStream is = (conn.getResponseCode() < 400) ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) return "";
        try (is) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
