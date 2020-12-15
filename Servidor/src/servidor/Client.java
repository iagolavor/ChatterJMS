/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import user.UserInterface;

/**
 *
 * @author iago-
 */
public class Client implements MessageListener{
    
    private String name;
    private UserInterface user;
    private Servidor server;
    
    public Client(String name, UserInterface user, Servidor server){
        this.name = name;
        this.user = user;
        this.server = server;
    }
    
    public String getName(){
        return this.name;
    }
    
    public UserInterface getUser(){
        return this.user;
    }

    @Override
    public void onMessage(Message msg) {
        if(msg instanceof TextMessage){
            TextMessage textMessage = (TextMessage) msg;
            try {
                this.getUser().chatMessage(textMessage.getText(), this.getName());
                //this.server.sendToOne(textMessage.getText(), name);
            } catch (JMSException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
