
package Model;

import View.TelaLog;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;


public class ThreadVerificaArquivos extends Thread {

    private File origem;
    private File destino;
    private List<ModeloArquivo> diferenca;
    private JButton btStart;
    private JButton btSim;
    private JButton btNao;
    private ModeloTabela modeloTabela;
    private JTable tabela;
    private JLabel lbDesejaCopiar;
    private JLabel lbQuantidadeArquivos;
    private JButton btIniciar;

    public ThreadVerificaArquivos(File origem, File destino, List<ModeloArquivo> diferenca,
            JButton btStart, JButton btSim, JButton btNao,
            ModeloTabela modeloTabela, JTable tabela,
            JLabel lbDesejaCopiar, JLabel lbQuantidadeArquivos, JButton btIniciar) {
        this.origem = origem;
        this.destino = destino;
        this.diferenca = diferenca;
        this.btStart = btStart;
        this.btSim = btSim;
        this.btNao = btNao;
        this.modeloTabela = modeloTabela;
        this.tabela = tabela;
        this.lbDesejaCopiar = lbDesejaCopiar;
        this.lbQuantidadeArquivos = lbQuantidadeArquivos;
        this.btIniciar = btIniciar;
    }

    @Override
    public void run() {

        btStart.setEnabled(false);

        int quantidadeDiretorios = 0;
        int quantidadeArquivos = 0;

        for (File f : origem.listFiles()) {

            ModeloArquivo modeloArquivo = new ModeloArquivo();
            File arquivo = new File(destino, f.getName());

            if (arquivo.exists()) {

            } else {

                if (f.isDirectory()) {
                    quantidadeDiretorios++;
                    modeloArquivo.setOrigem(f);
                    modeloArquivo.setArquivo(arquivo);
                    modeloArquivo.setNome(arquivo.getName());
                    modeloArquivo.setDiretorioOuArquivo("Diretorio");
                    diferenca.add(modeloArquivo);
                } else {
                    quantidadeArquivos++;
                    modeloArquivo.setOrigem(f);
                    modeloArquivo.setArquivo(arquivo);
                    modeloArquivo.setNome(arquivo.getName());
                    modeloArquivo.setDiretorioOuArquivo("Arquivo");
                    diferenca.add(modeloArquivo);
                }

            }
        }

        if (diferenca.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todos os arquivos que tem na origem, também tem no destino.");
            btIniciar.setEnabled(true);
            btSim.setVisible(false);
            btNao.setVisible(false);
            lbDesejaCopiar.setVisible(false);
        } else {

            int diretorios = 0;
            int arquivos = 0;
            for (ModeloArquivo ma : diferenca) {
                if (ma.getOrigem().isDirectory()) {
                    diretorios++;
                } else {
                    arquivos++;
                }
            }

            if (arquivos == 0) {
                modeloTabela = new ModeloTabela(diferenca);
                tabela.setModel(modeloTabela);
                tabela.setAutoResizeMode(tabela.AUTO_RESIZE_LAST_COLUMN);
                tabela.getColumnModel().getColumn(0).setPreferredWidth(276);
                lbQuantidadeArquivos.setText("Arquivos: " + quantidadeArquivos
                        + "    Diretorios: " + quantidadeDiretorios + "    Total: " + diferenca.size());
                btSim.setVisible(false);
                btNao.setVisible(false);
                btIniciar.setEnabled(true);
                lbDesejaCopiar.setVisible(false);

            } else {
                modeloTabela = new ModeloTabela(diferenca);
                tabela.setModel(modeloTabela);
                tabela.setAutoResizeMode(tabela.AUTO_RESIZE_LAST_COLUMN);
                tabela.getColumnModel().getColumn(0).setPreferredWidth(276);
                lbQuantidadeArquivos.setVisible(true);
                lbQuantidadeArquivos.setText("Arquivos: " + quantidadeArquivos
                        + "    Diretorios: " + quantidadeDiretorios + "    Total: " + diferenca.size());
                btSim.setVisible(true);
                btNao.setVisible(true);
                lbDesejaCopiar.setVisible(true);
            }
        }

    }

}
