/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;


import user.UserInterface;
/**
 *
 * @author iago-
 */
public class Servidor extends UnicastRemoteObject implements InterfaceServidor{
    
    //Mock do endereço do servidor
    //private String host = "//localhost:";
    private static int standardPort = 1099;
    private static String name = "server";
    private Vector<Client> users;
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private ActiveMQConnection connection;
    private Session session;
    
    public Servidor() throws RemoteException{
        super();
        users = new Vector<Client>();
    }
    
    private void registerRmi(){
        try {
            LocateRegistry.createRegistry(standardPort);
        } catch (RemoteException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Register remote object in RMI Naming server.
     * @return 1 if successfull, 0 if exception
     */
    public int startServer(){
        registerRmi();
        try {
            Naming.bind(name, this);
            return 1;
        } catch (AlreadyBoundException | MalformedURLException | RemoteException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    
    /**
     * Connect server to Activemq broker using default Activemq broker URL.
     */
    public void connectActivemq(){
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            this.connection = (ActiveMQConnection)connectionFactory.createConnection();
            this.connection.start();
            
            this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            System.out.println("connectActivemq error");
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    /**
     * Create a topic on the broker.
     * @param name String
     */    
    public void createTopic(String name){
        try {
            this.session.createTopic(name);
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Create a queu on the broker.
     * @param name String
     */
    public void createQueu(String name){
        try {
            this.session.createQueue(name);
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Deletes a queue on the broker.
     * @param name
     */
    public void deleteQueue(String name){
        try {
            this.connection.destroyDestination(new ActiveMQQueue(name));
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Deletes a topic on the broker
     * @param name
     */
    public void deleteTopic(String name){
        try {
            this.connection.destroyDestination(new ActiveMQTopic(name));
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks if user has duplicated name.
     * @param name
     * @return 
     */
    public boolean checkDuplicate(String name){
        for(Client c : users){
            if(c.getName().equals(name)){
                return false;
            }
        }
        return true;
    }
    
        
    //------- Remote methods below -----------
    
    /**
     * Get the remote user object and created a Queu to subscribe it to.
     * @param name
     * @return true or false
     */
    @Override
    public boolean registerUser(String name) {
        try {
            if(checkDuplicate(name)){
                //Finding the remote user object.
                UserInterface user = ( UserInterface )Naming.lookup("user"+name);
                Client cli = new Client(name, user, this);
                //Creating a queu for each registered user.
                Destination dest = this.session.createQueue(name);
                MessageConsumer consumer = this.session.createConsumer(dest);
                //Giving that user a MessageListener for that queu
                consumer.setMessageListener(cli);
                //Adding that queu to the user's list.
                user.addQueue(name);
                //Adding the user to the servers' user list.
                users.add(cli);
                return true;
            }
        } catch (NotBoundException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return false;
    }
    /**
     * Returns a String[] containing the queu names.
     * @return String[] 
     * @throws java.rmi.RemoteException
     */
    @Override
    public String[] listQueus() throws RemoteException {
        ArrayList<String> list = new ArrayList<>();
        try {
            DestinationSource ds = this.connection.getDestinationSource();
            Set<ActiveMQQueue> queues = ds.getQueues();
            for(ActiveMQQueue queue : queues){
                list.add(queue.getQueueName());
            }
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list.toArray(new String[0]);
    }
    
    /**
     * Returns a String[] containing the topic names.
     * @return String[]
     * @throws java.rmi.RemoteException
     */
    @Override
    public String[] listTopics() throws RemoteException {
        ArrayList<String> list = new ArrayList<>();
        try {
            DestinationSource ds = this.connection.getDestinationSource();
            Set<ActiveMQTopic> topics = ds.getTopics();
            for(ActiveMQTopic topic : topics){
                list.add(topic.getTopicName());
            }
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list.toArray(new String[0]);
    }

    @Override
    public void sendDirectMessage(String msg, String user) throws RemoteException {
        try {
            Destination dest = this.session.createQueue(user);
            MessageProducer producer = this.session.createProducer(dest);
            TextMessage message = this.session.createTextMessage(msg);
            producer.send(message);
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String[] getUsers() throws RemoteException {
        ArrayList<String> s = new ArrayList<>();
        for(Client c : this.users){
            s.add(c.getName());
        }
        return s.toArray(new String[0]);
    }

    @Override
    public void connectToQueue(String name, String username) throws RemoteException {
        try {
            for(Client c : this.users){
                if(c.getName().equals(username)){
                    Destination dest = this.session.createQueue(name);
                    MessageConsumer consumer = this.session.createConsumer(dest);
                    consumer.setMessageListener(c);
                    c.getUser().addQueue(name);
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
