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
            System.out.println("Erro: " + e.getMessage()); //implementar método pra exibir mensagens
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
                    int tamanhoMensagem = Integer.parseInt(mensagem.substring(2,
                            5));

                    if (mensagem.length() != tamanhoMensagem) {
                        System.out.println("Tamanho da mensagem não é válido"); //implementar método pra exibir mensagens
                    }
                    int tamanhoMinimo = 5;

                    if (mensagem.length() < tamanhoMinimo) {
                        System.out.println("Tamanho da mensagem não é válido"); //implementar método pra exibir mensagens
                    }

                    String complemento;

                    if (tamanhoMensagem > tamanhoMinimo) {
                        complemento = mensagem.substring(5);
                    }

                    int tamanhoTotalMsg = Integer.parseInt(mensagem.substring(0,
                            2));
                    complemento = "";                   
                    int posicao = Integer.parseInt(complemento);

                    switch (tamanhoTotalMsg) {
                        case 7:
                            if (posicao == 1 || posicao == 2) 
                               // mainTabuleiro.nome do metodo que começa jogo passando a posição como parametro;
                            break;
                    
                        case 8:
                            if(posicao> 0 && posicao <10)
                                //mainTabuleiro.nome do metodo que marca posição como parametro jogador remoto e posição;
                            break;
                            
                        case 9:
                            //mainTabuleiro. nome metodo que inicial jogo
                            break;
                            
                        case 10:
                            //MainTabuleiro.nome metodo que encerra conexão TCP, jogador desistiu;
                            break;
                        
                        default:
                            System.out.println("Mensagem inválida"); //implementar método pra exibir mensagens
                    }

                    System.out.println(mensagem); //implementar método pra exibir mensagem;    
                } else{
                    //MainTabuleiro.nome metodo que encerra conexão TCP, conexao caiu;
                    
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
                System.out.println("Erro: " + ex.getMessage()); //implementar método pra exibir mensagens
                return false;
            }
        }
    }

    public boolean enviarMensagemViaTCP(int num, String complemento) throws IOException {
        String mensagem = "";
        try{
            if ("".equals(complemento) || complemento.isEmpty() || complemento
                    == null)
                mensagem = String.format("%02d005",num);
            else
                mensagem = String.format("%02d%03d%s", num, 5 +
                        complemento.length(), complemento);
            
            outw.write(mensagem);
            outw.flush();
            
            System.out.println("Sua mensagem:"+ mensagem); //implementar método pra exibir mensagens
            
            return true;
        }catch(IOException ex){
            System.out.println("Erro envio da mensagem:"+ mensagem); //implementar método pra exibir mensagens
            return false;
        }    
    }
}
