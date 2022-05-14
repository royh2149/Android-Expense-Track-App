package com.example.expensetracker;

import android.util.Log;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.function.Consumer;

public class DatabaseTools {

    public static final String SERVER_IP = "10.0.2.2"; // 10.0.2.2 = host computer for emulator
    public static final int PORT = 27017;
    public static final String MAIN_COLLECTION = "users";
    public static final String ACTIONS_COLLECTION = "expenses";
    public static final String DB_NAME = "project";

    public static final String HASH_FUNCTION = "MD5";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "passwd";
    public static final String COLUMN_BALANCE = "balance";
    public static final String COLUMN_JOINED = "joined";
    public static final String COLUMN_ACTIONS = "actions";

    public static final String COLUMN_ACTION_ID = "_id";
    public static final String COLUMN_SUM = "sum";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DESC = "desc";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_DATE = "actionDate";
    public static final String COLUMN_TYPE = "isIncome";

    /* private static Consumer<Document> printCostumer = new Consumer<Document>() {
        @Override
        public void accept(Document document) {
            System.out.println(document.toJson());
        }
    };


    public static void createDB(){ // only to check connection works properly
        getDB();
        System.out.println("Connected Successfully!");
    } */

    /**
     * Adds a user to the database. Registers him
     * @param username user's username
     * @param passwd user's password
     * @param balance user's balance
     * @return true if user was added successfully, false otherwise
     */
    public static boolean addUser(String username, String passwd, double balance){ // returns true if user was added successfully
        if (usernameAlreadyExists(username)){ // make sure usernames won't collide
            System.out.println("UserName Already exits!");
            return false;
        }

        MongoDatabase database = getDB();
        if (database == null){
            return false;
        }
        MongoCollection<Document> collection = database.getCollection(MAIN_COLLECTION); // get the users collection

        // create the document with the necessary information
        Document doc = new Document(COLUMN_USERNAME, username).append(COLUMN_PASSWORD, passwd).append(COLUMN_BALANCE, balance).
                append(COLUMN_JOINED, LocalDateTime.now());
        collection.insertOne(doc);

        return true;
    }

    /**
     * Gets a user from the database, by the user's username
     * @param username user's username
     * @return the requested user, null if doesn't exist
     */
    public static User getUser(String username){ // returns true if user was added successfully
        MongoDatabase database = getDB();
        if (database == null){
            return null;
        }
        MongoCollection<Document> collection = database.getCollection(MAIN_COLLECTION); // get the users collection

        // search for the user
        FindIterable<Document> results = collection.find(Filters.eq(COLUMN_USERNAME,username));
        Iterator iterator = results.iterator(); // create iterator to check if the user was found

        if (!iterator.hasNext()){ // handle the case in which username doesn't exist
            return null;
        }

        // the iterator holds a BSon document
        Document doc = (Document) iterator.next(); // get the user
        User user = new User(doc.getString(COLUMN_USERNAME), "", doc.getDouble(COLUMN_BALANCE));

        return user;
    }

    /**
     * Gets an action from the database, by the action's ObjectId
     * @param actionId action's ObjectId
     * @return the requested action, null if doesn't exist
     */
    public static Action getAction(ObjectId actionId){ // returns true if user was added successfully
        MongoDatabase database = getDB();
        if (database == null){
            return null;
        }
        MongoCollection<Document> collection = database.getCollection(ACTIONS_COLLECTION); // get the users collection

        // search for the user
        FindIterable<Document> results = collection.find(Filters.eq(COLUMN_ACTION_ID, actionId));
        Iterator iterator = results.iterator(); // create iterator to check if the user was found

        if (!iterator.hasNext()){ // handle the case in which username doesn't exist
            return null;
        }

        // the iterator holds a BSon document
        Document doc = (Document) iterator.next(); // get the user
        Action action = generateActionFromDoc(doc);

        return action;
    }

    /**
     * Gets the actions performed by a given username.
     * @param username user's username
     * @return the requested user's actions. The list will be empty if the user doesn't exist
     */
    public static ArrayList<Action> getActionsOfUser(String username){ // returns a list of actions
        MongoDatabase database = getDB();
        if (database == null){
            return new ArrayList<>();
        }
        MongoCollection<Document> collection = database.getCollection(ACTIONS_COLLECTION); // get the actions collection

        // the ArrayList to be returned
        ArrayList<Action> actions = new ArrayList<>();

        // search for the actions of the received user, sort them from the latest to the oldest
        FindIterable<Document> results = collection.find(Filters.eq(COLUMN_USERNAME,username)).sort(new BasicDBObject(COLUMN_DATE, -1));
        Iterator iterator = results.iterator(); // create iterator to check if the user was found

        while (iterator.hasNext()){
            Document doc = (Document) iterator.next(); // get the current action
            Action curAction = generateActionFromDoc(doc);
            actions.add(curAction); // add the current action to the result list
        }

        return actions;
    }

    /**
     * Gets the actions performed by a given username, since and until specific dates.
     * @param username user's username
     * @param startDate the minimum date of actions to get
     * @param endDate the maximum date of actions to get
     * @return the requested user's actions in the received date range. The list will be empty if the user doesn't exist
     */
    public static ArrayList<Action> getActionsOfUserSinceUntil(String username, LocalDateTime startDate, LocalDateTime endDate){ // returns a list of actions
        MongoDatabase database = getDB();
        if (database == null){
            return new ArrayList<>();
        }
        MongoCollection<Document> collection = database.getCollection(ACTIONS_COLLECTION); // get the actions collection

        // the ArrayList to be returned
        ArrayList<Action> actions = new ArrayList<>();

        // search for the actions of the received user, sort them from the latest to the oldest
        FindIterable<Document> results = collection.find(Filters.and(Filters.eq(COLUMN_USERNAME,username),
                Filters.gte(COLUMN_DATE, startDate),
                Filters.lte(COLUMN_DATE, endDate))
                ).sort(new BasicDBObject(COLUMN_DATE, -1));
        Iterator iterator = results.iterator(); // create iterator to check if the user was found

        while (iterator.hasNext()){
            Document doc = (Document) iterator.next(); // get the current action
            Action curAction = generateActionFromDoc(doc);
            actions.add(curAction); // add the current action to the result list
        }

        return actions;
    }

    /**
     * Authenticate the user
     * @param username - username
     * @param passwd - password
     * @return the document representing the user
     */
    public static Document Authenticate(String username, String passwd){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return null;
        }
        MongoCollection<Document> collection = database.getCollection(MAIN_COLLECTION); // use the right collection

        // search whether username already exists
        FindIterable<Document> results = collection.find(Filters.eq(COLUMN_USERNAME,username));
        Iterator iterator;
        try {
            iterator = results.iterator(); // create iterator to check if password matches
        } catch (Exception e){
            return null;
        }

        if (!iterator.hasNext()){ // handle the case in which username doesn't exist
            return null;
        }

        // the iterator holds a BSon document
        Document doc = (Document) iterator.next();
        if(doc.get(COLUMN_PASSWORD).equals(passwd)){ // verify the password
            return doc;
        }

        return null; // indicate that wrong credentials were entered
    }

    /* public static boolean addUser(String user, String pass){
        try {
            // register the user
            addUser(user, pass, 0);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    } */

    /**
     * add a new action to the database
     * @param action the action to insert
     * @return true if successful, false otherwise
     */
    public static boolean insertAction(Action action){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return false;
        }
        MongoCollection<Document> collection = database.getCollection(ACTIONS_COLLECTION); // use the right collection

        // determine the type of the action
        boolean isIncome = false;
        if (action instanceof Income){
            isIncome = true;
        }

        // create the document with the necessary information
        Document doc = new Document(COLUMN_USERNAME, action.getUsername()).append(COLUMN_TYPE, isIncome).
                append(COLUMN_SUM, action.getSum()).append(COLUMN_CATEGORY, action.getCategory()).
                append(COLUMN_DESC, action.getDesc()).append(COLUMN_IMAGE, action.getImage())
                .append(COLUMN_DATE, action.getDate());
        InsertOneResult result = collection.insertOne(doc); // add the document to the database
        action.setActionId(result.getInsertedId().asObjectId().getValue()); // set the Database ID for the action


        User user = getUser(action.getUsername()); // get the relevant user
        if (user == null){ // ensure the user exists
            return false;
        }

        // calculate the new balance
        double newBalance = user.getBalance();
        if (isIncome){
            newBalance += action.getSum();
        } else {
            newBalance -= action.getSum();
        }

        MongoCollection<Document> usersCollection = database.getCollection(MAIN_COLLECTION); // use the right collection
        // update the new balance in the database
        usersCollection.updateOne(Filters.eq(COLUMN_USERNAME, action.getUsername()), // update the appropriate user
                Updates.set(COLUMN_BALANCE, newBalance)); // update the balance

        return true;
    }

    /**
     * Updates the properties of the received action
     * @param action the action to update
     * @return true if successful, false otherwise
     */
    public static boolean updateAction(Action action){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return false;
        }
        MongoCollection<Document> collection = database.getCollection(ACTIONS_COLLECTION); // use the right collection

        Action oldAction = getAction(action.getActionId()); // get the previous action

        // determine the type of the action
        boolean isIncome = false;
        if (action instanceof Income){
            isIncome = true;
        }

        // setup the update object with all the necessary attributes
        Bson updateObject = Updates.combine(
                Updates.set(COLUMN_USERNAME, action.getUsername()),
                Updates.set(COLUMN_TYPE, isIncome),
                Updates.set(COLUMN_SUM, action.getSum()),
                Updates.set(COLUMN_CATEGORY, action.getCategory()),
                Updates.set(COLUMN_DESC, action.getDesc()),
                Updates.set(COLUMN_IMAGE, action.getImage()),
                Updates.set(COLUMN_DATE, action.getDate())
        );

        // update the relevant action
        collection.updateOne(Filters.eq(COLUMN_ACTION_ID, action.getActionId()), updateObject);

        // update the current balance of the user
        User user = getUser(action.getUsername());
        if (user == null){
            return false;
        }
        double newBalance = user.getBalance();

        // neutralize the influence of the previous action
        if (oldAction instanceof Income){
            newBalance -= oldAction.getSum();
        } else {
            newBalance += oldAction.getSum();
        }

        // apply the influence of the current action
        if (isIncome){
            newBalance += action.getSum();
        } else {
            newBalance -= action.getSum();
        }

        return updateUserBalance(action.getUsername(), newBalance);
    }

    /**
     * update the balance of the user
     * @param username the user to modify
     * @param newBalance the new balance
     * @return true if successful, false otherwise
     */
    public static boolean updateUserBalance(String username, double newBalance){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return false;
        }
        MongoCollection<Document> collection = database.getCollection(MAIN_COLLECTION); // use the right collection

        // create the BSON update object with the new balance
        Bson updateObject = Updates.combine(Updates.set(COLUMN_BALANCE, newBalance));

        // update the balance of the user in the Database
        collection.updateOne(Filters.eq(COLUMN_USERNAME, username), updateObject);

        return true;
    }

    /**
     * Delete the received action from the DB
     * @param action the action to be deleted
     * @return true if successful, false otherwise
     */
    public static boolean deleteAction(Action action){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return false;
        }
        MongoCollection<Document> collection = database.getCollection(ACTIONS_COLLECTION); // use the right collection

        // delete the relevant action
        collection.deleteOne(Filters.eq(COLUMN_ACTION_ID, action.getActionId()));

        // update the current balance of the user
        User user = getUser(action.getUsername());
        if (user == null){
            return false;
        }
        double newBalance = user.getBalance();

        // neutralize the influence of the deleted action
        if (action instanceof Income){
            newBalance -= action.getSum();
        } else {
            newBalance += action.getSum();
        }

        return updateUserBalance(action.getUsername(), newBalance);
    }

    /* public static boolean resetAccount(String username){
        MongoDatabase db = getDB(); // get the database object
        if (db == null){
            return false;
        }
        MongoCollection<Document> collection = db.getCollection(MAIN_COLLECTION); // use the right collection

        collection.updateOne(Filters.eq(COLUMN_USERNAME, username), // update the appropriate user
                Updates.set(COLUMN_ACTIONS, new ArrayList<Document>())); // reset the actions

        collection.updateOne(Filters.eq(COLUMN_USERNAME, username), // update the appropriate user
                Updates.set(COLUMN_BALANCE, 0.0)); // reset the balance

        return true;
    } */

    /**
     * change the username of a user
     * @param old the original username
     * @param newOne the new username
     * @return true if successful, false otherwise
     */
    public static boolean changeUsername(String old, String newOne){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return false;
        }
        MongoCollection<Document> collection = database.getCollection(MAIN_COLLECTION); // use the right collection
        MongoCollection<Document> actionsCol = database.getCollection(ACTIONS_COLLECTION); // to update the associated actions

        if (usernameAlreadyExists(newOne)){
            return false;
        }else {
            collection.updateOne(Filters.eq(COLUMN_USERNAME, old), Updates.set(COLUMN_USERNAME, newOne));
            actionsCol.updateMany(Filters.eq(COLUMN_USERNAME, old), Updates.set(COLUMN_USERNAME, newOne));
            return true;
        }
    }

    /**
     * change the password of a user
     * @param username the username to modify
     * @param newPasswd the new password to save
     * @return
     */
    public static boolean changePassword(String username, String newPasswd){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return false;
        }
        MongoCollection<Document> collection = database.getCollection(MAIN_COLLECTION); // use the right collection

        try {
            collection.updateOne(Filters.eq(COLUMN_USERNAME, username), Updates.set(COLUMN_PASSWORD, newPasswd));
            return true; // return true if operation succeeded
        }catch (Exception e){
            return false; // return false in case of error
        }
    }

    /**
     * returns true if username received already exists in the database
     * @param username the username against which the method checks
     * @return true if username already exists in the database or in case of connection error
     */
    private static boolean usernameAlreadyExists(String username){
        MongoDatabase database = getDB(); // get the database object
        if (database == null){
            return true;
        }
        MongoCollection<Document> collection = database.getCollection(MAIN_COLLECTION); // use the right collection

        // search whether username already exists
        FindIterable<Document> results = collection.find(Filters.eq(COLUMN_USERNAME,username));
        Iterator iterator = results.iterator(); // create iterator to check if empty
        return iterator.hasNext(); // if the iterator is empty (has nothing next) it means username is available
    }

    // return the Database object
    private static MongoDatabase getDB(){ // returns the database object
        try {
            if (!InetAddress.getByName(SERVER_IP).isReachable(5000)){ // check if the server is available
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        MongoClient mongoClient = MongoClients.create("mongodb://" + SERVER_IP + ":" + PORT); // connect to the mongoDB server
        MongoDatabase database = mongoClient.getDatabase(DB_NAME); // create database if doesn't exist

        Log.d("MongoDB", "Connected to MongoDB");

        return database;
    }


    /**
     * generates an Action object from the Mongo Document object that is returned by queries
     * @param doc the document to convert into an action
     * @return the action described by doc
     */
    private static Action generateActionFromDoc(Document doc){
        Action action;
        if (doc.getBoolean(COLUMN_TYPE)){ // create an income
            Income income = new Income(doc.getDouble(COLUMN_SUM), doc.getString(COLUMN_CATEGORY),
                    doc.getString(COLUMN_DESC), doc.getString(COLUMN_IMAGE),
                    doc.getString(COLUMN_USERNAME), HelperMethods.convertToLocalDateTimeViaInstant((Date) doc.get(COLUMN_DATE)));
            income.setActionId(doc.getObjectId(COLUMN_ACTION_ID));
            action = income;
        } else { // create an outcome
            Outcome outcome = new Outcome(doc.getDouble(COLUMN_SUM), doc.getString(COLUMN_CATEGORY),
                    doc.getString(COLUMN_DESC), doc.getString(COLUMN_IMAGE),
                    doc.getString(COLUMN_USERNAME), HelperMethods.convertToLocalDateTimeViaInstant((Date) doc.get(COLUMN_DATE)));
            outcome.setActionId(doc.getObjectId(COLUMN_ACTION_ID));
            action = outcome;
        }

        return action; // return the created action
    }
}