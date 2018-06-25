/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhoprogconcorrente;

import java.net.InetAddress;

/**
 *
 * @author Alunoinf_2
 */
public class OnLine {
    private final String nome;
    private final InetAddress enderecoIp;
    private final int porta;
    private boolean isOnLine;

    public OnLine(String nome, InetAddress enderecoIp) {
        this.nome = nome;
        this.enderecoIp = enderecoIp;
        this.porta = 0;
        this.isOnLine = true;
    }

    public String getNome() {
        return this.nome;
    }

    public InetAddress getEnderecoIp() {
        return this.enderecoIp;
    }

    public int getPorta() {
        return this.porta;
    }

    public boolean isIsOnLine() {
        return this.isOnLine;
    }

    public void setIsOnLine(boolean isOnLine) {
        this.isOnLine = isOnLine;
    }
    
}
