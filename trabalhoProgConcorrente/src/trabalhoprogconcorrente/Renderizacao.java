/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhoprogconcorrente;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
/**
 *
 * @author Alunoinf_2
 */
public class Renderizacao extends DefaultListCellRenderer  {
    public Component getListCellRendererComponent(JList<?> list, Object jogador,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, jogador, index, isSelected, 
                cellHasFocus);
        if (jogador instanceof OnLine)
            setText(((OnLine)jogador).getNome());

        return this;
    }
}
