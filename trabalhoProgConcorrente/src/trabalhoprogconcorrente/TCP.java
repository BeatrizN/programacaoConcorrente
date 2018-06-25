/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhoprogconcorrente;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.swing.SwingWorker;

/**
 *
 * @author Alunoinf_2
 */
public class TCP extends SwingWorker<Boolean, String> {
    private final TabuleiroJogo mainJogo;
    private final ServerSocket socket;
    private final InetAddress enderecoIpRemoto;

    public TCP(TabuleiroJogo mainJogo, ServerSocket socket, 
            InetAddress enderecoIpRemoto) {
        this.mainJogo = mainJogo;
        this.socket = socket;
        this.enderecoIpRemoto = enderecoIpRemoto;
    }
    
    @Override
    protected Boolean doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void encerraConexao() {
        try {
            if (socket.isClosed() == false) {
                socket.close();
            }
        } catch (IOException ex) {
        }
    }
}
