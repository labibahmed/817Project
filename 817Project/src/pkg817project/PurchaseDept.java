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
public class PurchaseDept {
    PublicKey PUClient, PUSupervisor, PUPurDept;
    PrivateKey PRPurDept;
    ServerSocket s;
    Socket supervisor,client;
    PrintWriter Cw, Sw;
    InputStreamReader Cr,Sr;
    BufferedReader Cb,Sb;
    String ID = "PurchaseDept";
    Cipher RSA;
    
    public PurchaseDept() throws Exception {
        s = new ServerSocket(8000); // server on port 4999
        client = s.accept(); // accept the client on 4999
        supervisor = s.accept();
        // Creating reading and writing objects for Client Socket
        Cw = new PrintWriter(client.getOutputStream()); // might delete since we don't need to write to client
        Cr = new InputStreamReader(client.getInputStream());
        Cb = new BufferedReader(Cr);
        
        // Creating reading and writing objects for Supervisor Socket
        Sw = new PrintWriter(supervisor.getOutputStream()); // might delete since we don't need to write to Supervisor
        Sr = new InputStreamReader(supervisor.getInputStream());
        Sb = new BufferedReader(Sr);
        
        //Set up encryption variables
        RSA = Cipher.getInstance("RSA");
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        KeyPair rsaKey = kg.generateKeyPair();
        PUPurDept = rsaKey.getPublic();
        PRPurDept = rsaKey.getPrivate();
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
        objectOutputStream.writeObject(PUPurDept);
        
        
        // creating object streams to exchange keys with supervisor
        objectInputStream = new ObjectInputStream(supervisor.getInputStream());
        PUSupervisor = (PublicKey) objectInputStream.readObject();
        objectOutputStream = new ObjectOutputStream(supervisor.getOutputStream());
        objectOutputStream.writeObject(PUPurDept);
        
    }
    
    public static void main(String[] args) throws Exception {
        PurchaseDept s = new PurchaseDept();
        s.keyExchange();
        s.readClient();
    }
}
