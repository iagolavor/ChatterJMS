/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import java.awt.Component;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
//JMS
import javax.jms.MessageListener;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import servidor.InterfaceServidor;

/**
 *
 * @author iago-
 */
public class User extends UnicastRemoteObject implements UserInterface{
    
    private static int standardPort = 1099;
    private UserFrame userFrame;
    public InterfaceServidor server;
    private String name;
    private ArrayList<String> topics;
    private ArrayList<String> queus;
    
    public User(String name, UserFrame userframe) throws RemoteException, AlreadyBoundException, MalformedURLException{
        super();
        this.userFrame = userframe;
        this.name = name;
        this.topics = new ArrayList<>();
        this.queus = new ArrayList<>();
    }
    
    /**
     * Get class' nname property.
     * @return String
     */
    public String getName(){
        return this.name;
    }
    
    /**
     *  Returns a ArrayList<String> containing the user's subscribed topics
     * @return ArrayList<String>
     */
    public ArrayList<String> getUserTopics(){
        return this.topics;
    }
    
    /**
     * Returns a ArrayList<String> containing the user's subscribed queus
     * @return ArrayList<String>
     */
    public ArrayList<String> getUserQueues(){
        return this.queus;
    }
    
    /**
     * Adds a topic into the user's topic array.
     * @param topic 
     */
    @Override
    public void addTopic(String topic){
        this.topics.add(topic);
    }
    /**
     * Adds a queu into the user's queu array.
     * @param queu
     */
    @Override
    public void addQueue(String queu){
        this.queus.add(queu);
    }
    
    /**
     * Connects user with server interface and register into the remote object.
     * @throws java.rmi.RemoteException
     */
    public void startUser() throws RemoteException{
        try{
            Naming.bind("user"+this.name, this);
            this.server = (InterfaceServidor)Naming.lookup("server");
            server.registerUser(this.name);
        } catch (AlreadyBoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String[] getTopics() throws RemoteException{
        return this.server.listTopics();
    }

    @Override
    public void chatMessage(String msg, String url) throws RemoteException {
        System.out.println("My name:"+this.getName() + " msg:"+msg+" url:"+url);
        int idx = this.userFrame.jTabbedPane2.indexOfTab(url);
        JTextArea area = this.userFrame.textAreas.get(idx);
        area.append(msg+"\n");
    }
}
