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
 * @author Beatriz Nogueira e Keslley Lima.
 */
public class CnxTCP extends SwingWorker<Boolean, String> {
    
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
            this.entrada = this.socket.getInputStream();
            this.inr = new InputStreamReader(this.entrada, "ISO-8859-1");
            this.bfr = new BufferedReader(this.inr);
            
            this.saida = this.socket.getOutputStream();
            this.outw = new OutputStreamWriter(this.saida, "ISO-8859-1");
            this.bfw = new BufferedWriter(this.outw);
        } catch (IOException e) {
            mainTabuleiro.exibirMensagens(TabuleiroJogo.mensagemERRO,
                    socket.getRemoteSocketAddress().toString(), 
                    "Erro ao criar uma nova conexão");
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
                mensagem = (String) bfr.readLine();
                if (!"".equals(mensagem.trim())) {
                    int tamanhoMensagem = Integer.parseInt(mensagem.substring(2,5));
                    
                    if (mensagem.length() != tamanhoMensagem) {
                        mainTabuleiro.exibirMensagens(TabuleiroJogo.mensagemIN,
                                socket.getRemoteSocketAddress().toString(),
                                "Erro no tamanho da mensagem" + mensagem);
                    }
                    
                    int tamanhoMinimo = 5;
                    
                    if (mensagem.length() < tamanhoMinimo) {
                        mainTabuleiro.exibirMensagens(TabuleiroJogo.mensagemIN,
                                socket.getRemoteSocketAddress().toString(),
                                "Mensagem não é valida, devido ao tamanho, que é: " + mensagem);;
                    }
                    
                    String complemento;
                    
                    if (tamanhoMensagem > tamanhoMinimo) {
                        complemento = mensagem.substring(5);
                    }
                    
                    int tamanhoTotalMsg = Integer.parseInt(mensagem.substring(0,2));
                    complemento = "";
                    int posicao = Integer.parseInt(complemento);
                    
                    switch (tamanhoTotalMsg) {
                        case 7:
                            if (posicao == 1 || posicao == 2) {
                                mainTabuleiro.JogadorQueComecaJogando(posicao);
                            }
                            break;
                        
                        case 8:
                            if (posicao > 0 && posicao < 10) {
                                mainTabuleiro.marcarPosicao(mainTabuleiro.jogadorRemoto, posicao);
                            }
                            break;
                        
                        case 9:
                            mainTabuleiro.jogadorRemotocomecaJogando();
                            break;
                        
                        case 10:
                            mainTabuleiro.finalizarConexaoViaTCP(TabuleiroJogo.JOGADOR_DESISTIU);
                            break;
                        
                        default:
                            mainTabuleiro.exibirMensagens(TabuleiroJogo.mensagemIN,
                                    socket.getRemoteSocketAddress().toString(),
                                    "Mnsagem não é válida, o tamanho dela é: " + mensagem);
                    }
                    
                } else {
                    mainTabuleiro.finalizarConexaoViaTCP(TabuleiroJogo.CONEXAO_CAIU);
                    
                    Thread.currentThread().stop();
                    
                    entrada.close();
                    inr.close();
                    bfr.close();
                    socket.close();
                    saida.close();
                    outw.close();
                    bfw.close();
                }
            } catch (IOException ex) {
                mainTabuleiro.exibirMensagens(TabuleiroJogo.mensagemIN, 
                        socket.getRemoteSocketAddress().toString(), ex.getMessage());
                return false;
            }
        }
    }
    
    public boolean enviarMensagemViaTCP(int num, String complemento) throws IOException {
        String mensagem = "";
        try {
            if ("".equals(complemento) || complemento.isEmpty() || complemento
                    == null) {
                mensagem = String.format("%02d005", num);
            } else {
                mensagem = String.format("%02d%03d%s", num, 5
                        + complemento.length(), complemento);
            }
            
            outw.write(mensagem);
            outw.flush();
            mainTabuleiro.exibirMensagens(TabuleiroJogo.mensagemOUT, 
                   socket.getRemoteSocketAddress().toString(), mensagem);
            
            return true;
        } catch (IOException ex) {
            mainTabuleiro.exibirMensagens(TabuleiroJogo.mensagemOUT, 
                    socket.getRemoteSocketAddress().toString(), 
                    "Não foi possível enviar a mensagem:" + mensagem);
            return false;
        }
    }
}
