import java.util.*;

public class LoadBalancer implements Subject{
    List<Observer> registeredNodes= new ArrayList<>();
    private static LoadBalancer loadBalancer=null;

    public LoadBalancer() {

    }
    public static LoadBalancer getInstance(){
        if(loadBalancer==null) {
            synchronized (LoadBalancer.class) {
                loadBalancer = new LoadBalancer();
            }
        }
        return loadBalancer;
    }
    @Override
    public void registerObserver(Node node) {
        registeredNodes.add(node);
    }
    @Override
    public void unregisterObserver(Node node) {
        registeredNodes.remove(registeredNodes.indexOf(node));
    }
    @Override
    public void notifyObservers(String schemaName){
        for (Observer observer:registeredNodes) {
            observer.update(schemaName);
        }
    }

    synchronized public Node getMinLoadNode(){
        int minLoad=Integer.MAX_VALUE;
        Node nodeWithMinimumLoad = null;
        for (Observer observer:registeredNodes){
            Node node = (Node) observer;
            if(node.getLoad()<minLoad) {
                minLoad = node.getLoad();
                nodeWithMinimumLoad = node;
            }
        }
        return nodeWithMinimumLoad;
    }

}
