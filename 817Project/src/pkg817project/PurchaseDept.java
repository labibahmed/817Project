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
    String ID = "PurchaseDept@gmail.com";
    Cipher RSA;
    String msgFromClient;
    String msgFromSupervisor;
    String deMsgClient;
    String deMsgSuper;
    Timestamp time;
    
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
    
    public boolean verifyTime(long i){
        if (time == null || time.getTime() < i) {
            time = new Timestamp(i);
            return true;
        }
        else return false;
    }
    
    public void readClient() throws IOException, Exception{
        msgFromClient = Cb.readLine();
        msgFromSupervisor = Sb.readLine();
        
        String[] enmsg_array;
        enmsg_array = msgFromClient.split("\\|\\|"); //split msg and client signature [0] [1]
        String clientMsg = enmsg_array[0]; //client message
        String clientsig = enmsg_array[1]; //client sig
        
        String[] supermsg_arr;
        supermsg_arr = msgFromSupervisor.split("\\|\\|"); //superID
        String superMsg = supermsg_arr[0]; //super msg 
        String superSig = supermsg_arr[1]; //super sig
        String superclient = supermsg_arr[2];
        
        byte[] enmsg = Base64.getDecoder().decode(clientMsg);             
        byte[] demsg = RSAdecrypt(enmsg,PRPurDept);
        deMsgClient = new String (demsg);
        System.out.println(deMsgClient);   
        
        byte[] enmsg2 = Base64.getDecoder().decode(superMsg);             
        byte[] demsg2 = RSAdecrypt(enmsg2,PRPurDept);
        byte[] placeholder = Base64.getDecoder().decode(superclient);  
        byte[] ph2 =RSAdecrypt(placeholder,PRPurDept);
        deMsgSuper = new String (demsg2);
        String[] deMsgArrSuper =deMsgSuper.split("\\|\\|");
        System.out.println(deMsgSuper);  
        System.out.println(new String(ph2));
        
        
        
       if(verifySigClient(deMsgClient,clientsig) && verifySigSuper(deMsgSuper,superSig) && verifyTime(Long.parseLong(deMsgArrSuper[1])) ){
           if (deMsgClient.equals(new String(ph2)))
            System.out.println("Order from Client is approved by Purchasing Department");
           else System.out.println("Order does not match Supervisor's");
       } else {
           System.out.println("Order rejected");
       }    
    }
    
    public byte[] RSAdecrypt(byte[] msg, PrivateKey p) throws Exception{
        RSA.init(Cipher.DECRYPT_MODE, p);
        return RSA.doFinal(msg);
    }
    
    public byte[] RSAencrypt(byte[] msg, PublicKey p) throws Exception{
        RSA.init(Cipher.ENCRYPT_MODE, p);
        return RSA.doFinal(msg);
    }

    
    public boolean verifySigClient(String msg, String signature) throws Exception{
        byte[] msgByte = msg.getBytes();
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initVerify(PUClient);
        sig.update(msgByte);
        boolean result = sig.verify(Base64.getDecoder().decode(signature));
        return result;
    }
    
    public boolean verifySigSuper(String msg, String signature) throws Exception{
        byte[] msgByte = msg.getBytes();
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initVerify(PUSupervisor);
        sig.update(msgByte);
        boolean result = sig.verify(Base64.getDecoder().decode(signature));
        return result;
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

//verify timestamp from client and supervisor
//verify client message from client and supervisor
