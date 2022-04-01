/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg817project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;

/**
 *
 * @author Labib
 */
public class Supervisor {
    PublicKey PUClient, PUSupervisor, PUPurDept;
    PrivateKey PRSupervisor;
    ServerSocket s;
    Socket client, purdept;
    PrintWriter Cw, Pw;
    InputStreamReader Cr,Pr;
    BufferedReader Cb,Pb;
    String ID = "Supervisor";
    Cipher RSA;
    
    public Supervisor() throws Exception {
        s = new ServerSocket(4999); // server on port 4999
        client = s.accept(); // accept the client on 4999
        purdept = new Socket("localhost",8000); // port 4999 on local host
        
        // Creating reading and writing objects for Client Socket
        Cw = new PrintWriter(client.getOutputStream()); // might delete since we don't need to write to client
        Cr = new InputStreamReader(client.getInputStream());
        Cb = new BufferedReader(Cr);
        
        // Creating reading and writing objects for PurDept Socket
        Pw = new PrintWriter(purdept.getOutputStream());
        Pr = new InputStreamReader(purdept.getInputStream());
        Pb = new BufferedReader(Pr);
        
        //Set up encryption variables
        RSA = Cipher.getInstance("RSA");
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        KeyPair rsaKey = kg.generateKeyPair();
        PUSupervisor = rsaKey.getPublic();
        PRSupervisor = rsaKey.getPrivate();
        
    }
    
    public void readClient() throws IOException{
        String msg = Cb.readLine();
        System.out.println(msg);
    }
    
    public void keyExchange() throws IOException, ClassNotFoundException{
        // creating object streams to exchange keys with Client
        ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
        PUClient = (PublicKey) objectInputStream.readObject();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectOutputStream.writeObject(PUSupervisor);
        
        
        // creating object streams to exchange keys with purdept
        objectOutputStream = new ObjectOutputStream(purdept.getOutputStream());
        objectOutputStream.writeObject(PUSupervisor);
        objectInputStream = new ObjectInputStream(purdept.getInputStream());
        PUSupervisor = (PublicKey) objectInputStream.readObject();
    }
    
    public static void main(String[] args) throws Exception {
        Supervisor s = new Supervisor();
        s.keyExchange();
        s.readClient();
    }
}
