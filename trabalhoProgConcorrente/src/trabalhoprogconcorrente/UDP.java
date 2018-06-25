/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhoprogconcorrente;

import java.net.DatagramSocket;
import javax.swing.SwingWorker;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

public class UDP extends SwingWorker<Void, String> {
    private TabuleiroJogo mainJogo;
    private String meuNnome;
    private DatagramSocket socket;
    private int porta;
    private InetAddress enderecoIpLocal;

    public UDP(TabuleiroJogo mainJogo, String meuNnome, int porta, 
            InetAddress enderecoIpLocal) throws SocketException {
        this.mainJogo = mainJogo;
        this.meuNnome = meuNnome;
        socket = new DatagramSocket(porta, enderecoIpLocal);
        socket.setReuseAddress(true);
        this.porta = porta;
        this.enderecoIpLocal = enderecoIpLocal;
    }

    @Override
    protected Void doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); 
        //To change body of generated methods, choose Tools | Templates.
    }
    
    public void encerraConexao() {
        if (socket.isConnected()) {
            socket.disconnect();
        }
        
        socket.close();
    }
}
