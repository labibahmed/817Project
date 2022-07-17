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
import java.security.Signature;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Scanner;
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
    String ID = "Supervisor@gmail.com";
    Cipher RSA;
    String msgFromClient;
    String deMsgClient;
    Timestamp time;
    
    
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
    
    
    public boolean verifyTime(long i){
        if (time == null || time.getTime() < i) {
            time = new Timestamp(i);
            return true;
        }
        else return false;
    }
    
    public byte[] RSAdecrypt(byte[] msg, PrivateKey p) throws Exception{
        RSA.init(Cipher.DECRYPT_MODE, p);
        return RSA.doFinal(msg);
    }
    
    public byte[] RSAencrypt(byte[] msg, PublicKey p) throws Exception{
        RSA.init(Cipher.ENCRYPT_MODE, p);
        return RSA.doFinal(msg);
    }

    
    public boolean verifySig(String msg, String signature) throws Exception{
        byte[] msgByte = msg.getBytes();
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initVerify(PUClient);
        sig.update(msgByte);
        boolean result = sig.verify(Base64.getDecoder().decode(signature));
        return result;
    }
    
    public void readClient() throws IOException, Exception{
        msgFromClient = Cb.readLine();
        System.out.println("Received from Client: " + msgFromClient);
        String[] enmsg_array;
        enmsg_array = msgFromClient.split("\\|\\|"); //split msg and client signature [0] [1]
        String clientMsg = enmsg_array[0];
        String clientsig = enmsg_array[1];
        byte[] enmsg = Base64.getDecoder().decode(clientMsg);             
        byte[] demsg = RSAdecrypt(enmsg,PRSupervisor);
        deMsgClient = new String (demsg);
        System.out.println("Client's order Decrypted: "+deMsgClient);   
        String[] deMsgArr = deMsgClient.split("\\|\\|");
        
       if(verifySig(deMsgClient,clientsig) && verifyTime(Long.parseLong(deMsgArr[1])) ){
           System.out.println("Verified the Client's Timestamp and Signature\nTherefore the Client's order request will be sent to the Purchasing Department");
           
       } else {
           System.out.println("Client Timestamp or Signature not verified");
       }
            
    }
    
    public Timestamp getTime(){
        return new Timestamp(System.currentTimeMillis());
    }
    
    public byte[] signature(String msg) throws Exception{
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(PRSupervisor);
        privateSignature.update(msg.getBytes());
        return privateSignature.sign();
    }
    
    
    public void formOutput() throws Exception{ //(old message) + new timestamp + super sig >> send to purdep
        Timestamp t = getTime();
        String msg = ID + "||" + t.getTime(); //supervisorID + timestamp 
        if(PUPurDept == null){
            System.out.println("PR Key is null.");
        }
        String sig = Base64.getEncoder().encodeToString(signature(msg));
        byte[] PurMsg = RSAencrypt(msg.getBytes(),PUPurDept); 
        byte[] clientMes = RSAencrypt(deMsgClient.getBytes(),PUPurDept);
        
        System.out.println("The message that will be sent to Purchasing Department : " + msg + "||" + deMsgClient);
        System.out.println("Sending to Purchasing Department: " + Base64.getEncoder().encodeToString(PurMsg)+ "||" + sig + "||" + Base64.getEncoder().encodeToString(clientMes));
        Pw.println(Base64.getEncoder().encodeToString(PurMsg)+ "||" + sig + "||"+ Base64.getEncoder().encodeToString(clientMes));
        Pw.flush();
        
       //Scanner scan = new Scanner(System.in);
       //String item = scan.nextLine();
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
        PUPurDept = (PublicKey) objectInputStream.readObject();
        
        
    }
    
    public static void main(String[] args) throws Exception {
        Supervisor s = new Supervisor();
        s.keyExchange();
        s.readClient();
        s.formOutput();
    }
}