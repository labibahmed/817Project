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
public class PurchaseDept {
    PublicKey PUClient, PUSupervisor, PUPurDept;
    PrivateKey PRPurDept;
    ServerSocket s;
    Socket supervisor,client;
    PrintWriter Cw, Sw;
    InputStreamReader Cr,Sr;
    BufferedReader Cb,Sb;
    
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
    }
    
    public static void main(String[] args) throws Exception {
        PurchaseDept s = new PurchaseDept();
    }
}
