import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Locale;

public class WriteService {
    private String schemaName;
    private final ObjectOutputStream outputToClient;
    private final ObjectInputStream inputFromClient;
    private final FileWriter fileDriver= FileWriter.getInstance("/db");
    public WriteService(ObjectOutputStream outputToClient, ObjectInputStream inputFromClient) {
        this.outputToClient = outputToClient;
        this.inputFromClient = inputFromClient;
    }
    public String getSchemaName() {
        return schemaName;
    }
    public void makeOperation(String operation) throws IOException, ClassNotFoundException {

        if (operation.equals("1")) {
            createSchema();
            outputToClient.writeObject("schema created");
        } else if (operation.equals("2")) {
            addCollectionToSchema();
        } else if (operation.equals("3")) {
            addJsonObject();
        }
        else if (operation.equals("4")) {
            deleteObjectFromCollection();
        }
        else {
            outputToClient.writeObject("Invalid");
        }
    }
    public String readStringFromUser(String stringName) throws IOException, ClassNotFoundException {
        outputToClient.writeObject("Choose "+stringName+":");
        String stringValue=(String) inputFromClient.readObject();
        return stringValue.toLowerCase(Locale.ROOT);
    }
    private void createSchema() throws IOException, ClassNotFoundException {
        schemaName = readStringFromUser("schema name");
        fileDriver.createDirectory(schemaName);
        int numberOfCollection = Integer.parseInt(readStringFromUser("number of collections"));
        for (int i = 0; i < numberOfCollection; i++) {
            createNewCollection(schemaName);
        }
    }
    private void addCollectionToSchema() throws IOException, ClassNotFoundException {
        schemaName = readValidSchemaName();
        createNewCollection(schemaName);
    }
    private void createNewCollection(String schemaName) throws IOException, ClassNotFoundException {
        String collectionName = readStringFromUser("collection name");
        fileDriver.createSubDirectory(schemaName, collectionName);
        JSONObject jsonObject = readKeys();
        String path= schemaName+"/"+ collectionName;
        fileDriver.createFile(path, collectionName);
        fileDriver.writeToFile(path, collectionName +".txt",jsonObject);
    }
    private JSONObject readKeys() throws IOException, ClassNotFoundException {
        int numberOfKeys = Integer.parseInt(readStringFromUser("number of keys"));
        JSONObject jsonObject =new JSONObject();
        jsonObject.put("id","");
        for (int j = 1; j <= numberOfKeys; j++) {
            String key = readStringFromUser("Enter key number " + j);
            jsonObject.put(key,"");
        }
        return jsonObject;
    }
    private void addJsonObject() throws IOException, ClassNotFoundException {
        schemaName = readValidSchemaName();
        String collectionName = readValidCollectionName(schemaName);
        List<JSONObject> objectList= fileDriver.readFile(schemaName+"/"+ collectionName, collectionName +".txt");
        JSONObject emptyObject =objectList.get(0);
        for (Object key: emptyObject.keySet()) {
            String value = readStringFromUser("value of " + key.toString());
            emptyObject.put(key, value);
            fileDriver.createFile(schemaName + "/" + collectionName, (String) emptyObject.get("id"));
            fileDriver.writeToFile(schemaName + "/" + collectionName, (String) emptyObject.get("id") + ".txt", emptyObject);
        }
        }
    private String readValidSchemaName() throws IOException, ClassNotFoundException {
        schemaName = readStringFromUser("schema name");
        while (!fileDriver.isDirectoryExist(schemaName)){
            outputToClient.writeObject("wrong schema name");
            schemaName = readStringFromUser("schema name");
        }
        return schemaName;
    }
    private String readValidCollectionName(String schemaName) throws IOException, ClassNotFoundException {
        String collectionName = readStringFromUser("type name");
        while (!fileDriver.isDirectoryExist(schemaName+"/"+ collectionName)){
            outputToClient.writeObject("wrong type name");
            collectionName = readStringFromUser("type name");
        }
        return collectionName;
    }
    private void deleteObjectFromCollection() throws IOException, ClassNotFoundException {
        String collectionName = readStringFromUser("type name");
        String indexForObject=readStringFromUser("id for object");
        String path= schemaName+"/"+ collectionName;
        fileDriver.deleteFile(path,indexForObject);
    }
}





