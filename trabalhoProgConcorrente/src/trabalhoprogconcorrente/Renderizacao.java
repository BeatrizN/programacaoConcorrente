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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
/**
 *
 * @author Beatriz Nogueira e Keslley Lima.
 */
public class Renderizacao extends DefaultListCellRenderer  {
    public Component getListCellRendererComponent(JList<?> list, Object jogador,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, jogador, index, isSelected, 
                cellHasFocus);
        if (jogador instanceof OnLine) {
            setText(((OnLine)jogador).getNome());
        }
        
        return this;
    }
}
