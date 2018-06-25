/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhoprogconcorrente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
    
    // leitura dos dados
    private InputStream entrada;  
    private InputStreamReader inr;  
    private BufferedReader bfr;
    
    // envio dos dados
    private OutputStream saida;  
    private OutputStreamWriter outw;  
    private BufferedWriter bfw;      

    public CnxTCP(TabuleiroJogo mainTabuleiro, Socket socket, InputStream entrada, InputStreamReader inr, BufferedReader bfr, OutputStream saida, OutputStreamWriter outw, BufferedWriter bfw) {
        this.mainTabuleiro = mainTabuleiro;
        this.socket = socket;
        this.entrada = entrada;
        this.inr = inr;
        this.bfr = bfr;
        this.saida = saida;
        this.outw = outw;
        this.bfw = bfw;
    }
    
    
    
    public Socket getSocket()
    {
        return socket;
    }    

    @Override
    protected Boolean doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean enviarMensagem (){
        return true;
    }
}
