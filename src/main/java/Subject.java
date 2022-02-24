import java.io.IOException;

public interface Subject {
    void registerObserver(Node node);
    void unregisterObserver(Node node);
    void notifyObservers(String schemaName) ;
}
