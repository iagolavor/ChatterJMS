/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author iago-
 */
public interface UserInterface extends Remote{    
    public void chatMessage(String msg, String url) throws RemoteException;
    
    public void addQueue(String queu) throws RemoteException;
    
    public void addTopic(String topic) throws RemoteException;
}
