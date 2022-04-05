
package pkg817project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

/**
 *
 * @author Labib
 */
public class Client {
    PublicKey PUClient, PUSupervisor, PUPurDept;
    PrivateKey PRClient;
    Socket supervisor,purdept;
    PrintWriter Sw, Pw;
    InputStreamReader Sr,Pr;
    BufferedReader Sb,Pb;
    String ID = "Client@gmail.com";
    Cipher RSA;
    
    
    public Client() throws Exception {
        supervisor = new Socket("localhost",4999); // port 4999 on local host
        purdept = new Socket("localhost",8000);
        
        // Creating reading and writing objects for Supervisor Socket
        Sw = new PrintWriter(supervisor.getOutputStream());
        Sr = new InputStreamReader(supervisor.getInputStream());
        Sb = new BufferedReader(Sr);
        
        // Creating reading and writing objects for PurDept Socket
        Pw = new PrintWriter(purdept.getOutputStream());
        Pr = new InputStreamReader(purdept.getInputStream());
        Pb = new BufferedReader(Pr);
        
        //Set up encryption variables
        RSA = Cipher.getInstance("RSA");
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        KeyPair rsaKey = kg.generateKeyPair();
        PUClient = rsaKey.getPublic();
        PRClient = rsaKey.getPrivate();
        
    }
    
    public String order(){
        System.out.println("Please enter what you would like to order: ");
        Scanner scan = new Scanner(System.in);
        String item = scan.nextLine();
        System.out.println("Please enter how many you would like: ");
        String quantity = scan.nextLine();
        System.out.println("Please enter at what price you would like to buy it at: ");
        String price = scan.nextLine();
        return item + quantity + price;
    }
    public byte[] signature(String msg) throws Exception{
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(PRClient);
        privateSignature.update(msg.getBytes());
        return privateSignature.sign();
    }
    
    public Timestamp getTime(){
        return new Timestamp(System.currentTimeMillis());
    }
    
    
    public byte[] RSAencrypt(byte[] msg, PublicKey p) throws Exception{
        RSA.init(Cipher.ENCRYPT_MODE, p);
        return RSA.doFinal(msg);
    }
    
    
    
    public void keyExchange() throws IOException, ClassNotFoundException{
        // creating object streams to exchange keys with purdept
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(purdept.getOutputStream());
        objectOutputStream.writeObject(PUClient);
        ObjectInputStream objectInputStream = new ObjectInputStream(purdept.getInputStream());
        PUPurDept = (PublicKey) objectInputStream.readObject();
        
        // creating object streams to exchange keys with supervisor
        objectOutputStream = new ObjectOutputStream(supervisor.getOutputStream());
        objectOutputStream.writeObject(PUClient);
        objectInputStream = new ObjectInputStream(supervisor.getInputStream());
        PUSupervisor = (PublicKey) objectInputStream.readObject();
        
    }
    
    public void formOutput() throws Exception{
        Timestamp t = getTime();
        String msg = ID + "||" + t.getTime() + "||" +order();
        String sig = Base64.getEncoder().encodeToString(signature(msg));
        byte[] SuperMsg = RSAencrypt(msg.getBytes(), PUSupervisor);
        byte[] PurMsg = RSAencrypt(msg.getBytes(),PUPurDept); 
        
        System.out.println("Sending to Supervisor: " + Base64.getEncoder().encodeToString(SuperMsg) + "||" + sig);
        Sw.println(Base64.getEncoder().encodeToString(SuperMsg) + "||" + sig);
        Sw.flush();
        System.out.println("Sending to Purchasing Department: " + Base64.getEncoder().encodeToString(PurMsg)+ "||" + sig);
        Pw.println(Base64.getEncoder().encodeToString(PurMsg)+ "||" + sig);
        Pw.flush();
        
        Scanner scan = new Scanner(System.in);
        String item = scan.nextLine();
    }
    
    
   
    
    public static void main(String[] args) throws Exception, BadPaddingException {
        Client s = new Client();
        s.keyExchange();
        s.formOutput();
        
    }
}
