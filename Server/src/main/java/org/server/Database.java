package org.server;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

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
    static MongoCollection<Document> groupCollection;
    static MongoCollection<Document> chatCollection;
    public static void init() {
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("MONGO_URI");
        try {
            mongoClient = MongoClients.create(connectionString);
            db = mongoClient.getDatabase("ChatApp");
            userCollection = db.getCollection("users");
            groupCollection = db.getCollection("groups");
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
    public static String saveChat(String from, String to, String content, String type) {
        if (!userExists(to)) return null;
        Document newChat = new Document().append("type", type).append("from", from).append("to", to).append("content", content);
        InsertOneResult result = chatCollection.insertOne(newChat);
        BsonObjectId bson_id = (BsonObjectId) result.getInsertedId();
        if (bson_id == null) {
            return null;
        }
        return bson_id.getValue().toString();
    }

    public static ArrayList<ArrayList<String>> getChatLog(String self, String other) {
        if (!userExists(other)) return null;
        ArrayList<ArrayList<String>> logs = new ArrayList<>();
        try(MongoCursor<Document> cursor = chatCollection.find(or(and(eq("from", self), eq("to", other)), and(eq("from", other), eq("to", self))))
                .iterator())
        {
            while(cursor.hasNext()) {
                BsonDocument doc = cursor.next().toBsonDocument();
                BsonObjectId bson_id = (BsonObjectId) doc.get("_id");
                String id = bson_id.getValue().toString();
                String from = doc.get("from").asString().getValue();
                String content = doc.get("content").asString().getValue();
                String type = doc.get("type").asString().getValue();
                logs.add(new ArrayList<>(List.of(from, content, id, type)));
            }
        }
        return logs;
    }
    public static boolean removeMessage(String username, String id) {
        DeleteResult delResult = chatCollection.deleteOne(and(eq("_id", new ObjectId(id)), eq("from", username)));
        return delResult.getDeletedCount() == 1;
    }
    public static String getFileNameFromMessage(String username, String id) {
        Bson filter = and(eq("_id", new ObjectId(id)), eq("from", username), eq("type", "file"));
        Document user = chatCollection.find(filter).first();
        if (user == null) return null;
        return user.getString("content").split("\\|")[0];
    }

    public static boolean removeFile(String username, String id) {
        Bson filter = and(eq("_id", new ObjectId(id)), eq("from", username), eq("type", "file"));
        DeleteResult delResult = chatCollection.deleteOne(filter);
        return delResult.getDeletedCount() == 1;
    }

    public static String createGroup(String name, ArrayList<String> userList) {
        for (String user : userList) {
            if (!Database.userExists(user)) return null;
        }
        Document newUser = new Document().append("name", name).append("users", userList);
        InsertOneResult result = groupCollection.insertOne(newUser);
        BsonValue id = result.getInsertedId();
        BsonObjectId bson_id = (BsonObjectId) id;
        if (bson_id != null) return bson_id.getValue().toString();
        return null;
    }

    public static ArrayList<ArrayList<String>> getAllGroup(String username) {
        ArrayList<ArrayList<String>> groups = new ArrayList<>();
        Bson filter = in("users", username);
        try(MongoCursor<Document> cursor = groupCollection.find(filter)
                .iterator())
        {
            while(cursor.hasNext()) {
                ArrayList<String> group = new ArrayList<>();
                BsonDocument doc = cursor.next().toBsonDocument();
                List<BsonValue> users = doc.getArray("users").getValues();
                String name = doc.getString("name").getValue();
                BsonObjectId bson_id = (BsonObjectId) doc.get("_id");
                String id = bson_id.getValue().toString();
                group.add(id);
                group.add(name);
                for (BsonValue user : users) {
                    group.add(user.asString().getValue());
                }
                groups.add(group);
            }
        }
        return groups;
    }
}
