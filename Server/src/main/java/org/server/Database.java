package org.server;

import com.mongodb.client.*;
import com.mongodb.client.result.InsertOneResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

public class Database {
    static MongoClient mongoClient;
    static MongoDatabase db;
    static MongoCollection<Document> userCollection;
    static MongoCollection<Document> roomCollection;
    static MongoCollection<Document> chatCollection;
    public static void init() {
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("MONGO_URI");
        try {
            mongoClient = MongoClients.create(connectionString);
            db = mongoClient.getDatabase("ChatApp");
            userCollection = db.getCollection("users");
            roomCollection = db.getCollection("rooms");
            chatCollection = db.getCollection("chats");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean registerUser(String username, String password) throws NoSuchAlgorithmException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            String hashedPassword = bytesToHex(encodedHash);
            Document doc = userCollection.find(eq("username", username)).first();
            if (doc != null) return false;
            Document newUser = new Document().append("username", username).append("password", hashedPassword);
            InsertOneResult result = userCollection.insertOne(newUser);
            BsonValue id = result.getInsertedId();
            return id != null;
        } catch (Exception e) {
            return false;
        }
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

    public static boolean loginUser(String username, String password) throws NoSuchAlgorithmException {
        Document user = userCollection.find(eq("username", username)).first();
        if (user == null) return false;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
        String hashedPassword = bytesToHex(encodedHash);
        return user.getString("password").equals(hashedPassword);
    }

    public static ArrayList<String> getAllUser() {
        ArrayList<String> users = new ArrayList<>();
        try(MongoCursor<Document> cursor = userCollection.find()
                .iterator())
        {
            while(cursor.hasNext()) {
                users.add(cursor.next().getString("username"));
            }
        }
        return users;
    }
    public static boolean userExists(String username) {
        Document doc = userCollection.find(eq("username", username)).first();
        return doc != null;
    }
    public static boolean saveChat(String from, String to, String content) {
        if (!userExists(to)) return false;
        Document newChat = new Document().append("from", from).append("to", to).append("content", content);
        InsertOneResult result = chatCollection.insertOne(newChat);
        BsonValue id = result.getInsertedId();
        return id != null;
    }

    public static ArrayList<ArrayList<String>> getChatLog(String self, String other) {
        if (!userExists(other)) return null;
        ArrayList<ArrayList<String>> logs = new ArrayList<>();
        try(MongoCursor<Document> cursor = chatCollection.find(or(and(eq("from", self), eq("to", other)), and(eq("from", other), eq("to", self))))
                .iterator())
        {
            while(cursor.hasNext()) {
                BsonDocument doc = cursor.next().toBsonDocument();
                String from = doc.get("from").asString().getValue();
                String content = doc.get("content").asString().getValue();
                logs.add(new ArrayList<>(List.of(from, content)));
            }
        }
        return logs;
//        Document newChat = new Document().append("from", from).append("to", to).append("content", content);
//        InsertOneResult result = chatCollection.insertOne(newChat);
//        BsonValue id = result.getInsertedId();
//        return id != null;
    }
}
