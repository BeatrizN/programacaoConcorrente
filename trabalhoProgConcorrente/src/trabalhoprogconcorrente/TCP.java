/*
* Copyright (c) 2018 Willians Santos
* Copyright (c) 2018 Keslley Lima, Beatriz Nogueira
* MIT license.
* Esse trabalho foi desenvolvido no contexto da disciplina de Desenvolvimento de
* Software concorrente pelos alunos Beatriz Nogueira e Keslley Lima, no qual foi
* utilizado como principal referência o repositório público "JogoVelhaSocket" do
* usuário "tiowillians" presente no GitHub em https://github.com/tiowillians/JogoVelhaSocket
* Vale ressaltar que  o código fonte desse trabalho também está presente em um
* repositório público no seguinte caminho https://github.com/BeatrizN/programacaoConcorrente
*/
package trabalhoprogconcorrente;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingWorker;

/**
 *
 * @author Beatriz Nogueira e Keslley Lima.
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
                    // Apresentar mensagem de conexao realizada com sucesso (caixa no mainJogo).
                    mainJogo.conectou(criaConexao);                    
                    return true; 
                } else {
                   conexao.close();                    
                    mainJogo.exibirMensagens(mainJogo.mensagemIN, conexao.getRemoteSocketAddress().
                            toString(), "Tentativa de conexão inválida.");
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
