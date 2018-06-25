package trabalhoprogconcorrente;

import java.net.DatagramSocket;
import javax.swing.SwingWorker;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDP extends SwingWorker<Void, String> {
    private TabuleiroJogo mainJogo;
    private String meuNnome;
    private DatagramSocket socket;
    private int porta;
    private InetAddress enderecoIpLocal;
    private byte[] buffer;

    public UDP(TabuleiroJogo mainJogo, String meuNnome, int porta, 
            InetAddress enderecoIpLocal) throws SocketException {
        this.mainJogo = mainJogo;
        this.meuNnome = meuNnome;
        socket = new DatagramSocket(porta, enderecoIpLocal);
        socket.setReuseAddress(true);
        this.porta = porta;
        this.enderecoIpLocal = enderecoIpLocal;
        this.buffer = new byte[256];
    }

    @Override
    protected Void doInBackground() throws Exception {
        String mensagem;
        while (true) {
            try {
                DatagramPacket pacote = new DatagramPacket(this.buffer, this.buffer.length);
                this.socket.receive(pacote);
                mensagem = new String(pacote.getData()).trim();
                
                if(pacote.getAddress().equals(enderecoIpLocal)) {
                    continue;
                }
                
                if (mensagem.length() < 5) {
                    //Mensagem inválida (curta);
                    continue;
                }
                
                //Inserir aqui função para exibir mensagem;
                
                int tamanhoMensagem = Integer.parseInt(mensagem.substring(2, 5));
                if (tamanhoMensagem != mensagem.length()) {
                    //Tamanho da mensagem invalido);
                    continue;
                }

                String conteudoMensagem = "";
                if (tamanhoMensagem > 5)
                    conteudoMensagem = mensagem.substring(5);

                int tipoMensagem = Integer.parseInt(mensagem.substring(0, 2));
                switch(tipoMensagem) {
                    case 1:
                            //
                            break;
                    case 2:
                            //
                            break;                   
                    default:
                            // Mensagem inválida.
                }

            } catch (IOException ex){
                throw new UnsupportedOperationException("Not supported yet.");
            } 
        }
    }
    
    public void encerraConexao() {
        if (socket.isConnected()) {
            socket.disconnect();
        }
        
        socket.close();
    }
}
