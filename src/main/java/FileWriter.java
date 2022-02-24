import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileWriter {
    private final String ROOT_PATH;
    private static FileWriter fileDriver =null;
    private Lock lock=new ReentrantLock();
    public FileWriter(String rootPath) {
        this.ROOT_PATH = rootPath;
    }
    public static FileWriter getInstance(String rootPath){
        if(fileDriver==null) {
            synchronized (FileWriter.class) {
                fileDriver = new FileWriter(rootPath);
            }
        }
        return fileDriver;
    }
    public List<JSONObject> readFile(String directoryName, String fileName) {
        List <JSONObject> jsonObjects=new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(
                new FileReader(ROOT_PATH+"/"+directoryName+"/"+fileName)
        )){
            JSONParser jsonParser=new JSONParser();
            String obj;
            while(( obj = bufferedReader.readLine()) != null){
                JSONObject jsonObject=(JSONObject) jsonParser.parse(obj);
                jsonObjects.add(jsonObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return jsonObjects;
    }
    public void createDirectory(String directoryName){
        lock.lock();
        File newDirectory = new File(ROOT_PATH+"/"+directoryName);
        if (!newDirectory.exists()){
            newDirectory.mkdirs();
        }
        lock.unlock();
    }
    public void createSubDirectory(String parentDirectoryName,String directoryName){
        lock.lock();
        File newDirectory = new File(ROOT_PATH+"/"+parentDirectoryName+"/"+directoryName);
        if (!newDirectory.exists()){
            newDirectory.mkdirs();
        }
        lock.unlock();
    }
    public void createFile(String path,String fileName) throws IOException {
        lock.lock();
        File newFile=new File(ROOT_PATH+"/"+path+"/"+fileName+".txt");
        newFile.createNewFile();
        lock.unlock();
    }
    public void writeToFile(String path,String fileName,JSONObject jsonObject) throws IOException {
        lock.lock();
        BufferedWriter bufferedWriter=new BufferedWriter(new java.io.FileWriter(ROOT_PATH+"/"+path+"/"+fileName));
        bufferedWriter.write(jsonObject.toJSONString());
        bufferedWriter.flush();
        bufferedWriter.close();
        lock.unlock();
    }
    public boolean isDirectoryExist(String directoryName){
        Path path = Paths.get(ROOT_PATH+"/"+directoryName);
        return( Files.exists(path));
    }
    public Boolean deleteFile(String path,String fileName) throws IOException {
        lock.lock();
         if(Files.deleteIfExists(Paths.get(ROOT_PATH+"/"+fileName)))
           return true;
        lock.unlock();
        return false;
    }
}
