/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhoprogconcorrente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javax.swing.SwingWorker;

/**
 *
 * @author Alunoinf_2
 */
public class CnxTCP extends SwingWorker<Boolean, String>{
    private final TabuleiroJogo mainTabuleiro;
    private final Socket socket;
    
    private InputStream entrada;  
    private InputStreamReader inr;  
    private BufferedReader bfr;
    
    private OutputStream saida;  
    private OutputStreamWriter outw;  
    private BufferedWriter bfw;      

    public CnxTCP(TabuleiroJogo mainTabuleiro, Socket socket) {
        this.mainTabuleiro = mainTabuleiro;
        this.socket = socket;
                
        try {
            this.entrada  = this.socket.getInputStream();
            this.inr = new InputStreamReader(this.entrada, "ISO-8859-1");
            this.bfr = new BufferedReader(this.inr);
            
            this.saida =  this.socket.getOutputStream();
            this.outw = new OutputStreamWriter(this.saida, "ISO-8859-1");
            this.bfw = new BufferedWriter(this.outw); 
        } catch (IOException e) {
            // Mensagem de erro ao criação da nova conexão;
        }
    }
    
    public Socket getSocket() {
        return this.socket;
    }    

    @Override
    protected Boolean doInBackground() throws Exception {
        String mensagem;
        while (true) {
            try {
                
            } catch (IOException ex) {
                //
            }
        //throw new UnsupportedOperationException("Not supported yet."); 
        //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    public boolean enviarMensagem (){
        return true;
    }
}
