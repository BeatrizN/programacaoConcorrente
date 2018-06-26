package trabalhoprogconcorrente;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
        try {
            while(true) {
                Socket conexao = socket.accept();
                if (conexao.getInetAddress().equals(enderecoIpRemoto) == true) {
                    CnxTCP criaConexao = new CnxTCP(mainJogo, conexao); 
                    criaConexao.execute();
                    // Apresentar mensagem de conexao realizada com sucesso
                    return true; 
                } else {
                   conexao.close();                    
                    // Mensagem de erro na tentativa de conexao
                }
            }
        }catch (IOException ex) {
            return false;
        }
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