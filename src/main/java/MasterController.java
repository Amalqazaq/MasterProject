import org.json.simple.JSONObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// docker run -it --network network2 --volume //c/Users/user/Desktop/DB:/db -p 8000:8000 master
public class MasterController {
    private final FileWriter fileDriver= FileWriter.getInstance("/db");
    private final HashMap<String, User> users = new HashMap<>();
    private final LoadBalancer loadBalancer=LoadBalancer.getInstance();

    public static void main(String[] args) throws IOException {
        new MasterController();
    }
    private void initializeUsers() {
        List<JSONObject> jsonObjects = fileDriver.readFile("users", "users.json");
        User user;
        for (JSONObject jsonObject : jsonObjects) {
            user = new User();
            user.setId((String) jsonObject.get("id"));
            user.setUserName((String) jsonObject.get("username"));
            user.setPassword((String) jsonObject.get("password"));
            user.setRole((String) jsonObject.get("role"));
            users.put(user.getUserName(), user);
        }
    }
    public MasterController() throws IOException {
        initializeUsers();
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server started");

        ExecutorService executorService = Executors.newCachedThreadPool();
        while (true) {
            Socket socket = serverSocket.accept();
            executorService.execute(new ClientTask(socket));
        }
    }
    private class ClientTask implements Runnable {
        private ObjectOutputStream outputToClient;
        private ObjectInputStream inputFromClient;
        private Socket socket;
        private WriteService userService;

        public ClientTask(Socket socket) {
            this.socket = socket;
        }
        @Override public void run() {
            try {
                outputToClient= new ObjectOutputStream(socket.getOutputStream());
                inputFromClient = new ObjectInputStream(socket.getInputStream());
                String check=inputFromClient.readObject().toString();
                if(check.equalsIgnoreCase("client"))
                    login();
                else
                 registerNode();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                    outputToClient.close();
                    inputFromClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void registerNode() throws IOException, ClassNotFoundException {
            String ip=inputFromClient.readObject().toString();
            int portNumber= (int) inputFromClient.readObject();
            Node node=new Node(ip,portNumber,0);
            loadBalancer.registerObserver(node);
            inputFromClient.close();
            outputToClient.close();
            socket.close();
        }
        private void login() throws IOException, ClassNotFoundException {
            userService = new WriteService(outputToClient,inputFromClient);
            String username = userService.readStringFromUser("username");
            String password = userService.readStringFromUser("password");
            if (checkUser(username, password)) {
                determineUserOperation(username);
            }
            else {
                outputToClient.writeObject("Invalid username or password");
            }
        }
        private boolean checkUser (String enteredName, String enteredPassword){
            if (users.containsKey(enteredName)) {
                User tempUser = users.get(enteredName);
                return tempUser.getUserName().equals(enteredName) && tempUser.getPassword().equals(enteredPassword);
            }
            return false;
        }
        private void determineUserOperation(String username) throws IOException, ClassNotFoundException {
            String readOrWrite = userService.readStringFromUser("read of write?");
            if (readOrWrite.equalsIgnoreCase("write") && checkRole(username).equalsIgnoreCase("admin")){
                while (true){
                    outputToClient.writeObject("1.Create schema\n2.Add new collection\n3.Add object \n4.delete object\n5.Exit");
                    String operation = userService.readStringFromUser("operation");
                    if (operation.equals("5")){
                        inputFromClient.close();
                        outputToClient.close();
                        socket.close();
                        break;
                    }
                    userService.makeOperation(operation);
                    String schemaName = userService.getSchemaName();
                    loadBalancer.notifyObservers(schemaName);
                }
            }
            else {
                Node nodeFromCluster=loadBalancer.getMinLoadNode();
                nodeFromCluster.setLoad(nodeFromCluster.getLoad()+1);
                outputToClient.writeObject(String.valueOf(nodeFromCluster.getPortNumber()));
            }
        }
        private String checkRole(String userName){
            User tempUser = users.get(userName);
            return tempUser.getRole();
        }
    }
}