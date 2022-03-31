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
public class Client {
    PublicKey PUClient, PUSupervisor, PUPurDept;
    PrivateKey PRClient;
    Socket supervisor,purdept;
    PrintWriter Sw, Pw;
    InputStreamReader Sr,Pr;
    BufferedReader Sb,Pb;
    
    
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
    }
    
    public static void main(String[] args) throws Exception {
        Client s = new Client();
        
    }
}
