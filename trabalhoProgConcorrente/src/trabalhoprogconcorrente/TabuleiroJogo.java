package trabalhoprogconcorrente;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Beatriz Nogueira e Keslley Lima.
 */
public class TabuleiroJogo extends javax.swing.JFrame {

    private char[][] jogoVelha = new char[3][3];  // jogoVelha do jogo
    private boolean isJogandoEmUmaPartida;    // indica se jogo está em andamento
    private boolean isConectado;  // indica se jogador local está conectado
    private boolean minhaVez;       // indica se é a vez do jogador local
    private boolean inicieiUltimoJogo;  // indica se último jogo foi iniciado pelo jogador local
    private boolean fuiConvidado;   // indica se jogador local foi convidado
    private ServerSocket servidorTCP;      // socket servidor TCP criado para jogador remoto se conectar
    private CnxTCP conexaoTCP;      // conexão TCP com o jogador remoto
    private String meuNome;    // apelido do jogador local
    private DefaultListModel<OnLine> jogadores;  // lista de jogadores que estão online
    private final static Random aleatorio = new Random();   // gerador de números aleatórios

    private final int portaUDP = 20181;

    // cores dos jogadores no jogoVelha
    private final Color COR_LOCAL = new Color(51, 153, 0);
    private final Color COR_REMOTO = new Color(255, 0, 0);
    private final Color COR_EMPATE = new Color(255, 255, 0);

    // identificação dos jogadores
    public final static int jogadorLocal = 1;
    public final static int jogadorRemoto = 2;
    public final char simboloVazio = ' ';
    public final char simboloLocal = 'X';
    public final char simboloRemoto = 'O';

    // motivos para jogo encerrar
    public final static int CONEXAO_TIMEOUT = 0;
    public final static int CONEXAO_CAIU = 1;
    public final static int JOGADOR_DESISTIU = 2;
    public final static int FIM_JOGO = 3;

    // resultadosPartidas dos jogos
    private final int resultadoVazio = -1;
    private final int empate = 0;
    private final int jogadorLocalVenceu = 1;
    private final int jogadorRemotoVenceu = 2;

    // posições no jogoVelha onde foi conseguido a vitória
    private final int SEM_GANHADOR = 0;
    private final int linha1 = 1;
    private final int linha2 = 2;
    private final int linha3 = 3;
    private final int coluna1 = 4;
    private final int coluna2 = 5;
    private final int coluna3 = 6;
    private final int diagonalPrincipal = 7;
    private final int diagonalSecundaria = 8;

    // tipos de mensagens mostradas na tela
    public static final String mensagemIN = "Recebida";
    public static final String mensagemOUT = "Enviada";
    public static final String mensagemERRO = "Ocorreu um erro";
    public static final String mensagemINF = "Informações";
    public static final String mensagemTCP = "Protocolo TCP"; ////aparentemente não uso................
    public static final String mensagemUDP = "Protocolo UDP"; ////aparentemente não uso................
    public static final String mensagemSemProtocolo = ""; ////aparentemente não uso................

    private int[] resultadosPartidas = new int[5];  // resultadosPartidas de cada jogo
    private int meuAtualJogo;          // número do jogo atual
    // dados relacionados a threads e sockets
    private UDP udpEscutaThread;         // thread para leitura da porta UDP
    private TCP tcpEscutaThread;         // thread de escuta da porta TCP
    private InetAddress addrLocal;             // endereço do jogador local
    private InetAddress addrBroadcast;         // endereço para broadcasting
    private InetAddress addrJogadorRemoto;     // endereço do jogador remoto
    private String nomeRemoto;              // apelido do jogador remoto
    private Timer JogadorOnlineEmIntervalo;         // temporizador para saber quem está online
    private Timer timeoutJogadorOnline;  // temporizador de timeout
    private Timer timeoutAguardandoOutroJogador;    // temporizador de timeout

    // status do programa
    private boolean aguardandoConexao;
    private boolean aguardandoInicioJogo;
    private boolean aguardandoConfirmacao;
    private boolean aguardandoJogadorRemoto;
    private boolean aguardandoRespostaConvite;

    /**
     * Creates new form TabuleiroJogo
     */
    public TabuleiroJogo() {
        initComponents();
        
        this.setTitle("Jogo da Velha");
        this.setLocationRelativeTo(null);
        
        isJogandoEmUmaPartida = isConectado = false;
        servidorTCP = null;
        conexaoTCP = null;
        udpEscutaThread = null;
        tcpEscutaThread = null;
        addrLocal = null;
        aguardandoConexao = aguardandoInicioJogo = false;
        aguardandoConfirmacao = aguardandoJogadorRemoto = false;
        aguardandoRespostaConvite = false;

        try {
            addrBroadcast = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível criar endereço para broadcasting.",
                    "Finalizando o programa",
                    JOptionPane.ERROR_MESSAGE);
            finalizaJogo();
            return;
        }
        
        jogadores = new DefaultListModel<>();
        jList1.setModel(jogadores); ////////////////////////////////////////////************************
        jList1.setCellRenderer(new Renderizacao()); ////////////////////////////////////////****************************

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                if (netint.isVirtual() || netint.isLoopback()){
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                if(inetAddresses.hasMoreElements()) {
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        if ((inetAddress instanceof Inet4Address) &&
                            inetAddress.isSiteLocalAddress()) {
                            jComboBox1.addItem(inetAddress.getHostAddress() +
                                    "  " + netint.getDisplayName());
                        }
                    }
                }
            }
        }catch(SocketException ex) {
        }
        
        ActionListener quemEstaOnlinePerformer = (ActionEvent evt) -> {
            for(int i = 0; i < jogadores.getSize(); ++i) {
                jogadores.get(i).setIsOnLine(false);
            }

            enviarUDP(addrBroadcast, 1, meuNome);
            timeoutJogadorOnline.start();
        };
        JogadorOnlineEmIntervalo = new Timer(200000, quemEstaOnlinePerformer);
        JogadorOnlineEmIntervalo.setRepeats(true);  

        ActionListener timeoutQuemEstaOnlinePerformer = (ActionEvent evt) -> {
            atualizaListaOnLines();
        };
        timeoutJogadorOnline = new Timer(17000, timeoutQuemEstaOnlinePerformer);
        timeoutJogadorOnline.setRepeats(false);   

        ActionListener timeoutAguardandoJogadorRemotoPerformer = (ActionEvent evt) -> {
            if(aguardandoRespostaConvite) {
                cancelaConviteDeJogo(true);
            } else {
                try {
                    finalizarConexaoViaTCP(CONEXAO_TIMEOUT);
                } catch (IOException ex) {
                    Logger.getLogger(TabuleiroJogo.class.getName()).log(Level.SEVERE, null, ex);
                }
		}
        };
        
        timeoutAguardandoOutroJogador = new Timer(30000, timeoutAguardandoJogadorRemotoPerformer);
        timeoutAguardandoOutroJogador.setRepeats(false);

    }

    public void iniciarSessaoJogo() throws IOException {

        if (timeoutAguardandoOutroJogador.isRunning()) {
            timeoutAguardandoOutroJogador.stop();
        }

        jogadorRemotoJLabel.setText(nomeRemoto + "/" + simboloRemoto);
        jogadorRemotoJLabel.setEnabled(true);

        if (fuiConvidado) {
            int aux = aleatorio.nextInt(2) + 1;
            if (aux == jogadorLocal) {
                inicieiUltimoJogo = true;
                minhaVez = inicieiUltimoJogo;
                statusJLabel.setText("Agora é sua vez");
            } else {
                inicieiUltimoJogo = false;
                minhaVez = inicieiUltimoJogo;
                statusJLabel.setText("Aguardando o jogador jogar");
            }

            String complemento = String.valueOf(aux);
            conexaoTCP.enviarMensagemViaTCP(7, complemento);
        }

        isJogandoEmUmaPartida = true;
        meuAtualJogo = 1;
        zerarPlacar();

        limparTabuleiro();

        placarRemotoJLabel.setEnabled(true);
        placarLocalJLabel.setEnabled(true);
        
    }

    public void zerarPlacar() {
        Color corTabuleiro;
        String nomeRemoto;

        if (isJogandoEmUmaPartida == true) {
            nomeRemoto = this.nomeRemoto + "/" + simboloRemoto;
            corTabuleiro = Color.BLACK;
        } else {
            nomeRemoto = "Remoto" + "/" + simboloRemoto;
            corTabuleiro = Color.BLUE;
        }

        jogadorRemotoJLabel.setText(nomeRemoto);
        posicoesJPanel.setBackground(corTabuleiro);

        for (int i = 0; i < 5; i++) {
            resultadosPartidas[i] = resultadoVazio;
        }

        exibirPlacar();

        int posicao = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                jogoVelha[i][j] = simboloVazio;

                switch (posicao) {
                    case 0:
                        pos1JLabel.setText("");
                        break;
                    case 1:
                        pos2JLabel.setText("");
                        break;
                    case 2:
                        pos3JLabel.setText("");
                        break;
                    case 3:
                        pos4JLabel.setText("");
                        break;
                    case 4:
                        pos5JLabel.setText("");
                        break;
                    case 5:
                        pos6JLabel.setText("");
                        break;
                    case 6:
                        pos7JLabel.setText("");
                        break;
                    case 7:
                        pos8JLabel.setText("");
                        break;
                    case 8:
                        pos9JLabel.setText("");
                        break;
                }
                posicao = posicao + 1;
            }
        }

        jogadorRemotoJLabel.setEnabled(isJogandoEmUmaPartida);
        placarRemotoJLabel.setEnabled(isJogandoEmUmaPartida);
        placarLocalJLabel.setEnabled(isJogandoEmUmaPartida);
    }

    public void exibirPlacar() {
        Color cor;
        int local = 0;
        int remoto = 0;
        javax.swing.JLabel label = null;

        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    label = jogo1JLabel;
                    break;
                case 1:
                    label = jogo2JLabel;
                    break;
                case 2:
                    label = jogo3JLabel;
                    break;
                case 3:
                    label = jogo4JLabel;
                    break;
                case 4:
                    label = jogo5JLabel;
                    break;
            }

            cor = Color.DARK_GRAY;
            if (isJogandoEmUmaPartida) {
                if (resultadosPartidas[i] == resultadoVazio && meuAtualJogo
                        == (i + 1)) {
                    cor = Color.BLACK;
                } else {
                    switch (resultadosPartidas[i]) {

                        case jogadorLocalVenceu:
                            local = local + 1;
                            cor = COR_LOCAL;
                            break;
                        case jogadorRemotoVenceu:
                            remoto = remoto + 1;
                            cor = COR_REMOTO;
                            break;
                        default:
                            cor = COR_EMPATE;
                    }
                }

                label.setEnabled((i + 1) <= meuAtualJogo);
            } else {
                label.setEnabled(false);
            }

            label.setForeground(cor);
        }

        placarLocalJLabel.setText(String.valueOf(local));
        placarRemotoJLabel.setText(String.valueOf(remoto));
    }

    public void limparTabuleiro() {
        int posicao = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                jogoVelha[i][j] = simboloVazio;

                switch (posicao) {
                    case 0:
                        pos1JLabel.setText("");
                        break;
                    case 1:
                        pos2JLabel.setText("");
                        break;
                    case 2:
                        pos3JLabel.setText("");
                        break;
                    case 3:
                        pos4JLabel.setText("");
                        break;
                    case 4:
                        pos5JLabel.setText("");
                        break;
                    case 5:
                        pos6JLabel.setText("");
                        break;
                    case 6:
                        pos7JLabel.setText("");
                        break;
                    case 7:
                        pos8JLabel.setText("");
                        break;
                    case 8:
                        pos9JLabel.setText("");
                        break;
                }
                posicao = posicao + 1;
            }
        }
    }

    public void JogadorQueComecaJogando(int jogador) throws IOException {
        aguardandoInicioJogo = false;

        iniciarSessaoJogo();
        inicieiUltimoJogo = true;

        if (jogador == 1) {
            minhaVez = inicieiUltimoJogo;
            statusJLabel.setText("Aguardando jogador começar");
        } else {
            statusJLabel.setText("Sua vez de jogar");
        }

    }

    public void marcarPosicao(int jogadorEscolhido, int posicao) throws IOException {

        int linha = (posicao - 1) / 3;
        int coluna = (posicao - 1) % 3;
        Color cor;
        char marca;

        if (jogadorEscolhido == jogadorLocal) {
            cor = COR_LOCAL;
            marca = simboloLocal;
        } else {
            cor = COR_REMOTO;
            marca = simboloRemoto;
        }

        jogoVelha[linha][coluna] = marca;
        javax.swing.JLabel label = null;

        switch (posicao) {
            case 1:
                label = pos1JLabel;
                break;
            case 2:
                label = pos2JLabel;
                break;
            case 3:
                label = pos3JLabel;
                break;
            case 4:
                label = pos4JLabel;
                break;
            case 5:
                label = pos5JLabel;
                break;
            case 6:
                label = pos6JLabel;
                break;
            case 7:
                label = pos7JLabel;
                break;
            case 8:
                label = pos8JLabel;
                break;
            case 9:
                label = pos9JLabel;
                break;
        }

        label.setForeground(cor);
        label.setText(Character.toString(marca));

        if (jogadorEscolhido == jogadorLocal) {
            conexaoTCP.enviarMensagemViaTCP(8, String.valueOf(posicao));
        }

        int ganhador = jogadorVencedor();

        if (ganhador != SEM_GANHADOR) {
            resultadosPartidas[meuAtualJogo - 1] = ganhador % 10;
            exibirResultadoPartida(ganhador);
            novaPartida(ganhador);
        }

        if (jogadorEscolhido == jogadorLocal) {
            minhaVez = false;
            statusJLabel.setText("Aguardando o jogador iniciar");
        } else {
            minhaVez = true;
            statusJLabel.setText("Agora sua vez");
        }

    }

    private int jogadorVencedor() {
        for (int linha = 0; linha < 3; linha++) {
            if ((jogoVelha[linha][0] == jogoVelha[linha][1])
                    && (jogoVelha[linha][1] == jogoVelha[linha][2])
                    && jogoVelha[linha][0] != simboloVazio) {
                int resultado = 0;
                switch (linha) {
                    case 0:
                        resultado = linha1;
                        break;
                    case 1:
                        resultado = linha2;
                        break;
                    case 2:
                        resultado = linha3;
                        break;
                }
                return 10 * resultado + (jogoVelha[linha][0] == simboloLocal
                        ? jogadorLocal : jogadorRemoto);
            }
        }

        for (int coluna = 0; coluna < 3; coluna++) {
            if ((jogoVelha[0][coluna] == jogoVelha[1][coluna])
                    && (jogoVelha[1][coluna] == jogoVelha[2][coluna])
                    && jogoVelha[0][coluna] != simboloVazio) {
                int resultado = 0;
                switch (coluna) {
                    case 0:
                        resultado = coluna1;
                        break;
                    case 1:
                        resultado = coluna2;
                        break;
                    case 2:
                        resultado = coluna3;
                        break;
                }

                return 10 * resultado
                        + (jogoVelha[0][coluna] == simboloLocal ? jogadorLocal
                                : jogadorRemoto);

            }
        }

        if ((jogoVelha[0][0] == jogoVelha[1][1])
                && (jogoVelha[0][0] != simboloVazio) && (jogoVelha[1][1]
                == jogoVelha[2][2])) {
            return 10 * diagonalPrincipal
                    + (jogoVelha[0][0] == simboloLocal ? jogadorLocal
                            : jogadorRemoto);
        }

        if ((jogoVelha[0][2] != simboloVazio)
                && (jogoVelha[0][2] == jogoVelha[1][1])
                && (jogoVelha[1][1] == jogoVelha[2][0])) {
            return 10 * diagonalSecundaria
                    + (jogoVelha[0][2] == simboloLocal ? jogadorLocal : jogadorRemoto);
        }

        return SEM_GANHADOR;
    }

    private void exibirResultadoPartida(int ganhador) {
        destacarPlacarTabuleiro(ganhador / 10);
        exibirPlacar();
        String mensagem = "";

        switch (ganhador % 10) {
            case jogadorLocalVenceu:
                mensagem = "Você é o ganhador!!";
                break;
            case jogadorRemotoVenceu:
                mensagem = "Você não ganhou ! Não foi dessa fez";
                break;
            case empate:
                mensagem = "A partida ficou empatado! ninguém ganhou";
                break;
        }

        if (meuAtualJogo == 5) {
            int remoto = Integer.parseInt(placarRemotoJLabel.getText());
            int local = Integer.parseInt(placarLocalJLabel.getText());

            mensagem += "\n\n Placar final:"
                    + "\n " + meuNome + ":" + local
                    + "\n" + nomeRemoto + ":" + remoto
                    + "\n\n";
            if (local == remoto) {
                mensagem += "Você ganhou essa sensão, SHOW!!";
            } else {
                mensagem += nomeRemoto + "Ganhou a sessão!";
            }

            mensagem += "\n\nPara jogar novamente é necessário convidar jogador"
                    + "novamente";
        }

        JOptionPane.showMessageDialog(this, mensagem, "A partida" + meuAtualJogo
                + "de 5.", JOptionPane.INFORMATION_MESSAGE);
    }

    private void destacarPlacarTabuleiro(int posicaoVencedor) {

        boolean[][] destaca = {{false, false, false},
        {false, false, false},
        {false, false, false}};

        switch (posicaoVencedor) {
            case linha1:
                destaca[0][0] = destaca[0][1] = destaca[0][2] = true;
                break;
            case linha2:
                destaca[1][0] = destaca[1][1] = destaca[1][2] = true;
                break;
            case linha3:
                destaca[2][0] = destaca[2][1] = destaca[2][2] = true;
                break;
            case coluna1:
                destaca[0][0] = destaca[1][0] = destaca[2][0] = true;
                break;
            case coluna2:
                destaca[0][1] = destaca[1][1] = destaca[2][1] = true;
                break;
            case coluna3:
                destaca[0][2] = destaca[1][2] = destaca[2][2] = true;
                break;
            case diagonalPrincipal:
                destaca[0][0] = destaca[1][1] = destaca[2][2] = true;
                break;
            case diagonalSecundaria:
                destaca[0][2] = destaca[1][1] = destaca[2][0] = true;
                break;
        }

        int linha, coluna;
        javax.swing.JLabel label = null;
        for (int pos = 0; pos < 9; ++pos) {
            linha = pos / 3;
            coluna = pos % 3;
            switch (pos) {
                case 0:
                    label = pos1JLabel;
                    break;
                case 1:
                    label = pos2JLabel;
                    break;
                case 2:
                    label = pos3JLabel;
                    break;
                case 3:
                    label = pos4JLabel;
                    break;
                case 4:
                    label = pos5JLabel;
                    break;
                case 5:
                    label = pos6JLabel;
                    break;
                case 6:
                    label = pos7JLabel;
                    break;
                case 7:
                    label = pos8JLabel;
                    break;
                case 8:
                    label = pos9JLabel;
                    break;
            }

            if (destaca[linha][coluna] == false) {
                label.setForeground(Color.BLUE);
            }
        }
    }

    private void novaPartida(int ultimoGanhador) throws IOException {
        limparTabuleiro();

        meuAtualJogo = meuAtualJogo + 1;
        exibirPlacar();

        if (ultimoGanhador != jogadorLocal) {
            boolean enviaMensagem = true;
            if (ultimoGanhador == empate) {
                enviaMensagem = !inicieiUltimoJogo;
            }

            if (enviaMensagem) {
                conexaoTCP.enviarMensagemViaTCP(9, null);

                minhaVez = inicieiUltimoJogo = true;
                statusJLabel.setText("Sua vez de jogar"); /////////////////////////////////////////////////////////////
            }
        } else {
            minhaVez = inicieiUltimoJogo = false;
            statusJLabel.setText("Aguardando início da partida"); ///////////////////////////////////////////////
            aguardandoInicioJogo = true;
        }
    }

    public void jogadorRemotocomecaJogando() {
        aguardandoInicioJogo = false;
        statusJLabel.setText("Aguardando o jogador");
    }

    public void finalizarConexaoViaTCP(int desistencia) throws IOException {
        if (isJogandoEmUmaPartida == true) {
            isJogandoEmUmaPartida = false;
            zerarPlacar();
            limparTabuleiro();
        }

        int portaJogadorRemoto = 0;
        String addrRemoto = "";

        if (conexaoTCP.getSocket() != null && conexaoTCP != null) {
            portaJogadorRemoto = conexaoTCP.getSocket().getPort();

            if (conexaoTCP.getSocket().getRemoteSocketAddress() != null) {
                addrRemoto = conexaoTCP.getSocket().getRemoteSocketAddress().
                        toString();
            }
        }

        try {
            if (servidorTCP != null) {
                servidorTCP.close();

                if (tcpEscutaThread != null) {
                    tcpEscutaThread.cancel(true);
                }
            }
        } catch (IOException ex) {

        }

        tcpEscutaThread = null;
        servidorTCP = null;

        if (conexaoTCP != null) {
            conexaoTCP.cancel(true);
        }

        conexaoTCP = null;

        if (desistencia == CONEXAO_TIMEOUT) {
            JOptionPane.showMessageDialog(null,
                    "TIMEOUT: aguardando conexão remota.",
                    "Encerrar jogo",
                    JOptionPane.WARNING_MESSAGE);
        }

        if (desistencia == CONEXAO_CAIU) {
            JOptionPane.showMessageDialog(null,
                    "Conexão com o jogador remoto caiu.",
                    "Encerrar jogo",
                    JOptionPane.WARNING_MESSAGE);
        }

        if (desistencia == JOGADOR_DESISTIU) {
            JOptionPane.showMessageDialog(null,
                    "O jogador remoto desistiu do jogo.",
                    "Encerrar jogo",
                    JOptionPane.WARNING_MESSAGE);
        }

        aguardandoInicioJogo = false;
        aguardandoConexao = aguardandoInicioJogo;

        aguardandoJogadorRemoto = false;
        aguardandoConfirmacao = aguardandoJogadorRemoto;

        posicoesJPanel.setBackground(Color.BLUE);

        statusJLabel.setText("");

        exibirMensagens(mensagemINF, addrRemoto, "Conexão foi encerrada");
        exibirMensagens(mensagemINF, "", "O jogo acabou"
        );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        painelMensagens = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaMensagens = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(140, 240, 140));

        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton9)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton8)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2)
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton4)
                            .addComponent(jButton5)
                            .addComponent(jButton6))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jButton7))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton9)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9});

        jPanel2.setBackground(new java.awt.Color(240, 140, 240));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Jogadores OnLine:");

        jButton11.setBackground(new java.awt.Color(140, 240, 140));
        jButton11.setText("Convidar");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setBackground(new java.awt.Color(140, 140, 240));
        jButton12.setText("Sair");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Nome", "Ip"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(406, 406, 406))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton11, jButton12});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton12))
                .addContainerGap())
        );

        painelMensagens.setBackground(new java.awt.Color(140, 140, 240));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Mensagem:");

        tabelaMensagens.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Tipo", "Endereço", "Conteúdo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tabelaMensagens);

        javax.swing.GroupLayout painelMensagensLayout = new javax.swing.GroupLayout(painelMensagens);
        painelMensagens.setLayout(painelMensagensLayout);
        painelMensagensLayout.setHorizontalGroup(
            painelMensagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMensagensLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(painelMensagensLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelMensagensLayout.setVerticalGroup(
            painelMensagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMensagensLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 28)); // NOI18N
        jLabel3.setText("Jogo da Velha");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setText("Nome:");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Porta de Conexão:");

        jButton10.setBackground(new java.awt.Color(240, 140, 240));
        jButton10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton10.setText("Começar");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jList1);

        jTextField1.setText("jTextField1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(painelMensagens, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField1)
                                .addGap(18, 18, 18)
                                .addComponent(jButton10)))
                        .addGap(18, 18, 18))))
            .addGroup(layout.createSequentialGroup()
                .addGap(287, 287, 287)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(32, 32, 32))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton10)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelMensagens, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(110, 110, 110))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TabuleiroJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TabuleiroJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TabuleiroJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TabuleiroJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TabuleiroJogo().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel painelMensagens;
    private javax.swing.JTable tabelaMensagens;
    // End of variables declaration//GEN-END:variables
  
    

    
    
    /*
    
    private javax.swing.JLabel statusJLabel;
    private javax.swing.JLabel jogadorRemotoJLabel;
    private javax.swing.JLabel placarLocalJLabel;
    private javax.swing.JLabel placarRemotoJLabel;
    private javax.swing.JPanel posicoesJPanel;
    private javax.swing.JLabel pos1JLabel;
    private javax.swing.JLabel pos2JLabel;
    private javax.swing.JLabel pos3JLabel;
    private javax.swing.JLabel pos4JLabel;
    private javax.swing.JLabel pos5JLabel;
    private javax.swing.JLabel pos6JLabel;
    private javax.swing.JLabel pos7JLabel;
    private javax.swing.JLabel pos8JLabel;
    private javax.swing.JLabel pos9JLabel;
    private javax.swing.JTable mensagensJTable;
    private javax.swing.JLabel jogo1JLabel;
    private javax.swing.JLabel jogo2JLabel;
    private javax.swing.JLabel jogo3JLabel;
    private javax.swing.JLabel jogo4JLabel;
    private javax.swing.JLabel jogo5JLabel;
*/
    public void exibirMensagens(String tipo, String endereco, String conteudo) {

        DefaultTableModel msg;
        msg = (DefaultTableModel) tabelaMensagens.getModel();

        msg.addRow(new String[]{tipo, endereco, conteudo});

        //Bia não esqueça desse comentario para apagar se não for usado!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        tabelaMensagens.changeSelection(tabelaMensagens.getRowCount() -1, 0, false, false);
    }
    
    public void conectou(CnxTCP cnx) throws IOException {
        aguardandoConexao = false;
        this.conexaoTCP = cnx;
        servidorTCP = null;
        iniciarSessaoJogo();
    }

    public boolean isNomeIgual(String nome, String nomeComparacao) {
        boolean igual = (nomeComparacao.compareToIgnoreCase(nome) == 0);
        return igual;
    }

    public void adicionaOnLinesLista(int tipoMensagem, String nome, InetAddress enderecoIp) {
        OnLine novo;
        OnLine jogador;
        for (int i = 0; i < jogadores.size(); ++i) {
            jogador = jogadores.get(i);

            if (isNomeIgual(nome, jogador.getNome())) {
                jogador.setIsOnLine(true);
                if (tipoMensagem == 1) {
                    enviarUDP(enderecoIp, 2, meuNome);
                }

                return;
            }

            if (jogador.getNome().compareToIgnoreCase(nome) > 0) {
                novo = new OnLine(nome, enderecoIp);
                jogadores.add(i, novo);
                if (tipoMensagem == 1) {
                    enviarUDP(enderecoIp, 2, meuNome);
                }

                return;
            }
        }

        novo = new OnLine(nome, enderecoIp);
        jogadores.addElement(novo);

        if (tipoMensagem == 1) {
            enviarUDP(enderecoIp, 2, meuNome);
        }
    }

    public void enviarUDP(InetAddress enderecoIp, int numero, String texto, Boolean... auxiliar) {
        String mensagem;
        boolean exibir = true;

        if ((texto == null) || texto.isEmpty()) {
            mensagem = String.format("%02d005", numero);
        } else {
            mensagem = String.format("%02d%03d%s", numero, 5 + texto.length(), texto);
        }

        if ((auxiliar.length > 0) && (auxiliar[0] instanceof Boolean)) {
            exibir = !auxiliar[0];
        }
        DatagramSocket socket = null;
        DatagramPacket pacote = new DatagramPacket(mensagem.getBytes(),
                mensagem.getBytes().length, enderecoIp, portaUDP);

        try {
            socket = new DatagramSocket(0, addrLocal);
            socket.setBroadcast(enderecoIp.equals(addrBroadcast));
            socket.send(pacote);

            if (exibir) {
                exibirMensagens(mensagemOUT, enderecoIp.getHostAddress(), mensagem);
            }
        } catch (IOException ex) {
            if (exibir) {
                exibirMensagens(mensagemOUT, enderecoIp.getHostAddress(),
                        "Mensagem inválida (Erro)");
            }
        }
    }
    
    public void removeJogadorOnline(String nome) throws IOException {
        if(isJogandoEmUmaPartida && (isNomeIgual(nome, nomeRemoto))) { //(nome.compareToIgnoreCase(apelidoRemoto) == 0))
            finalizarConexaoViaTCP(JOGADOR_DESISTIU);
        }
        
        for(int i = 0; i < jogadores.size(); ++i) {
            if(isNomeIgual(nome, jogadores.get(i).getNome())) {
                jogadores.remove(i);
                return;
            }
        }
    }

    public void fuiConvidado(String nome, InetAddress enderecoIP) {
        String mensagem;
        if(isJogandoEmUmaPartida) {
            exibirMensagens(mensagemINF, enderecoIP.getHostAddress(),
                    "Convite recusado automaticamente");
            mensagem = nome + "|0";
            enviarUDP(enderecoIP, 5, mensagem);
            
            return;
        }

        fuiConvidado = true;
        statusJLabel.setText(""); ///////////////////////////////////////////////////////////////////////////////////////////////////
        addrJogadorRemoto = null;
        
        mensagem = "O jogador " + nome + " está te convidando para um jogo\nAceita?";
        int resp = JOptionPane.showConfirmDialog(this, mensagem, "Convite para jogar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (resp == JOptionPane.NO_OPTION) {
            mensagem = meuNome + "|0";
            enviarUDP(enderecoIP, 5, mensagem);
            exibirMensagens(mensagemINF, "", "Convite não foi aceito");
            return;
        }
        
        servidorTCP = socketTCPiniciando();
        if (servidorTCP == null) {
            JOptionPane.showMessageDialog(null,
                    "Erro na criação da conexão TCP.",
                    "Conexão do jogador remoto",
                    JOptionPane.ERROR_MESSAGE);
            mensagem = meuNome + "|0";
            enviarUDP(enderecoIP, 5, mensagem);
            statusJLabel.setText(""); ////////////////////////////////////////////////////////////////////////////////////////

            return;
        }
        
        addrJogadorRemoto = enderecoIP;
        nomeRemoto = nome;
        tcpEscutaThread = new TCP(this, servidorTCP, enderecoIP);
        tcpEscutaThread.execute();
            
        mensagem = meuNome + "|" + servidorTCP.getLocalPort();
        enviarUDP(enderecoIP, 5, mensagem);
        
        aguardandoConexao = true;
        aguardandoConfirmacao = true;
        aguardandoInicioJogo = true;
        statusJLabel.setText("Aguardando Conexão"); //////////////////////////////////////////////////////////////////////////////////////
        timeoutAguardandoOutroJogador.start();
    }
    
    private ServerSocket socketTCPiniciando() {
        InetAddress enderecoInterface = encontraInterface();
        if(enderecoInterface == null) {
            return null;
        }
        ServerSocket socket;
        try {
            socket = new ServerSocket(0, 1, enderecoInterface);
            socket.setReuseAddress(true);
        } catch (IOException e) {
            return null;
        }
     
        return socket;
    }
    
    private InetAddress encontraInterface() {
        int x = jComboBox1.getSelectedIndex();
        if(x < 0) {
            return null;
        }
        InetAddress endereco;
        String auxiliar = jComboBox1.getItemAt(x);
        String[] espacamento = auxiliar.split("  ");
        
        try {
            endereco = InetAddress.getByName(espacamento[0]);
        } catch (UnknownHostException ex) {
            return null;
        }
        
        return endereco;
    }
    
        public void responderJogador(String mensagem, InetAddress enderecoIP) {
        // formato da resposta: Apelido|porta
        String[] strPartes= mensagem.split("\\|");
        if(strPartes.length != 2)
            return;
        
        // estou esperando uma resposta?
        if(aguardandoRespostaConvite == false) {
            return;
        }
        
        // verifica se quem respondeu foi realmente o jogador remoto
        if ((enderecoIP.equals(addrJogadorRemoto) == false) ||
            nomeRemoto.compareToIgnoreCase(strPartes[0]) != 0) {
                return;
        }
        // cancela espera da resposta ao convite
        aguardandoRespostaConvite = false;
        if(timeoutAguardandoOutroJogador.isRunning()) {
            timeoutAguardandoOutroJogador.stop();
        }
        
        int porta = Integer.parseInt(strPartes[1]);
        if(porta == 0) {
            cancelaConviteDeJogo(false);
            return;
        }

        enviarUDP(enderecoIP, 6, "Ok");
        try {
            Socket socket = new Socket(enderecoIP, porta);
            conexaoTCP = new CnxTCP(this, socket);
            conexaoTCP.execute();
            aguardandoInicioJogo = true;
            statusJLabel.setText("Aguardando Início"); ///////////////////////////////////////////////////////////////////////////////////////
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao criar conexão " + ex.getMessage(),
                    "Conectar com outro jogador", JOptionPane.INFORMATION_MESSAGE);
        }
    }
        
    private void cancelaConviteDeJogo(boolean timeout) {
        aguardandoRespostaConvite = false;
        statusJLabel.setText("");//////////////////////////////////////////////////////////////////////////////////////****************************************
        
        String mensagem;
        if(timeout) {
            mensagem = "Timeout: " + nomeRemoto + " não respondeu.";
        } else {
            mensagem = nomeRemoto + " recusou o convite.";
        JOptionPane.showMessageDialog(this, mensagem, "Convite para jogar",
                                      JOptionPane.INFORMATION_MESSAGE);//////////////////////////////////////////////////////////////////////*******************
        }
    }
    
    public void jogadorConfirmouParticipacao(InetAddress addr) throws IOException {
        if (addr.equals(addrJogadorRemoto) == false) {
            return;
	}
        aguardandoConfirmacao = false;
        iniciarSessaoJogo();
    }
    
    private void finalizaJogo() {
        enviarUDP(addrBroadcast, 3, meuNome, true);
        Container frame = jButton12.getParent();
        do {
            frame = frame.getParent(); 
        }while (!(frame instanceof JFrame));  
        ((JFrame)frame).dispose();
    }  
    
    public void atualizaListaOnLines() {
        for(int i = 0; i < jogadores.size(); ++i) {
            if(jogadores.get(i).isIsOnLine() == false) {
                jogadores.remove(i);
            }
        }
    }
}
