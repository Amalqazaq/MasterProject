import java.io.*;
import java.net.Socket;


public class Node implements Observer {
    private  String ip;
    private  int portNumber;
    private int load;
    public Node(String ip, int portNumber,int load) {
        this.ip = ip;
        this.portNumber = portNumber;
        this.load=load;
    }
    @Override
    public void update(String schemaName) {
        try {
            Socket socket = new Socket(ip,7000);
            ObjectOutputStream outputToCache = new ObjectOutputStream(socket.getOutputStream());
            outputToCache.writeObject("update schema");
            outputToCache.writeObject(schemaName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getPortNumber() {
        return portNumber;
    }
    public void setLoad(int load) {
        this.load = load;
    }
    public int getLoad() {
        return load;
    }

}

