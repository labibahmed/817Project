/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg817project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

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
        
    }
    
    public static void main(String[] args) throws Exception {
        Supervisor s = new Supervisor();
    }
}
