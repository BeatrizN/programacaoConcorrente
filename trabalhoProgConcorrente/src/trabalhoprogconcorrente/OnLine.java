<<<<<<< HEAD
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
=======
>>>>>>> 77e29659d60beeb991baff85183856ebdfc99b11
package trabalhoprogconcorrente;

import java.net.InetAddress;

/**
 *
 * @author Beatriz Nogueira e Keslley Lima.
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
