package org.server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Auth {
    private static final String DATA_FOLDER = "./data";
    private static final String DATA_FILE = "users.txt";

    private static HashMap<String, String> users;
    public Auth() { }
    public static void init() throws IOException {
        users = new HashMap<>();
        Files.createDirectories(Paths.get(DATA_FOLDER));
        File f = new File(String.format("%s/%s", DATA_FOLDER, DATA_FILE));
        if(!f.exists() || f.isDirectory()) return;
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(":", 2);
            if (tokens.length != 2) continue;
            String username = new String(Base64.getDecoder().decode(tokens[0]));
            String hashedPassword = tokens[1];
            users.put(username, hashedPassword);
        }
        reader.close();
    }
    public static boolean login(String username, String password) throws NoSuchAlgorithmException {
        if (!users.containsKey(username)) return false;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
        String hashedPassword = bytesToHex(encodedHash);
        return users.get(username).equals(hashedPassword);
    }
    public static boolean register(String username, String password) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
        String hashedPassword = bytesToHex(encodedHash);
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, hashedPassword);
        String b64Username = Base64.getEncoder().encodeToString(username.getBytes());
        File file = new File(String.format("%s/%s", DATA_FOLDER, DATA_FILE));
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(String.format("%s:%s", b64Username, hashedPassword));
        bw.newLine();
        bw.close();
        return true;
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public static ArrayList<String> getUsers() {
        return new ArrayList<>(users.keySet());
    }
}