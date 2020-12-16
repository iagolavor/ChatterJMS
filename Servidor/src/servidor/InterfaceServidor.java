/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.jms.Session;

/**
 *
 * @author iago-
 */
public interface InterfaceServidor extends Remote{
    
    public boolean registerUser(String name) throws RemoteException;
    
    public void sendDirectMessage(String msg, String user) throws RemoteException;
    
    public void connectToQueue(String name, String username) throws RemoteException;
    
    public String[] getUsers() throws RemoteException;
    
    public String[] listQueus() throws RemoteException;
    
    public String[] listTopics() throws RemoteException;
}
