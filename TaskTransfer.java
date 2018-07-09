
package com.jmprojects.bugle.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

public class TaskTransfer extends Task<Boolean> {

    private final List<File> origemLista;
    private final File destino;
    private long quantidadeDeArquivos;
    private long contaArquivos;
    private int erros;
    private final StringBuilder logGeral = new StringBuilder();
    private final StringBuilder logErros = new StringBuilder();
    private final boolean ignorar_arquivos_existentes;
    private final boolean deletar_origem;
    private boolean apartir_da_raiz;

    public TaskTransfer(List<File> origemLista, File destino, boolean ignorar_arquivos_existentes, 
            boolean deletar_origem, boolean apartir_da_raiz) {
        this.origemLista = origemLista;
        this.destino = destino;
        this.ignorar_arquivos_existentes = ignorar_arquivos_existentes;
        this.deletar_origem = deletar_origem;
        this.apartir_da_raiz = apartir_da_raiz;
    }

    @Override
    protected Boolean call() {
        quantidade_de_arquivos();
        validacao_apartir_da_raiz();
        log_inicio();
        copia_recursiva();
        log_final();
        return true;
    }

    private void do_copia_recursiva_completo(File origem, File destino) {
        try{
            if (origem.isDirectory()) {
                if (!destino.exists()) {
                    destino.mkdir();
                }
                File[] files = origem.listFiles();
                for (File file : files){
                    File destFile = new File(destino, file.getName());
                    do_copia_recursiva_completo(file, destFile);
                }
            } else {
                String hashAntes = crc32(origem);
                FileUtils.copyFile(origem, destino);
                String hashDepois = crc32(destino);
                if (hashDepois.equals(hashAntes) && destino.length() == origem.length()) {
                    update_progress();
                    if(deletar_origem){
                        origem.delete();
                    }
                } else{
                    update_progress();
                    destino.delete();
                    logGeral.append("Erro ao transferir:");
                    logGeral.append("\n");
                    logGeral.append(origem.toString());
                    erros++;
                }
            }
        } catch (IOException ex) {
            logErros.append(ex);
            logErros.append("\n");
        }
    }

    private void do_copia_recursiva_ignora_arquivos_existentes(File origem, File destino) {
        try{
            if (origem.isDirectory()) {
                if (!destino.exists()) {
                    destino.mkdir();
                }
                File[] files = origem.listFiles();
                for (File file : files) {
                    File dest_file = new File(destino, file.getName());
                    do_copia_recursiva_ignora_arquivos_existentes(file, dest_file);
                }
            } else {
                if (destino.exists()) {
                    update_progress();
                } else {
                    String hashAntes = crc32(origem);
                    FileUtils.copyFile(origem, destino);
                    String hashDepois = crc32(destino);
                    if (hashDepois.equals(hashAntes) && destino.length() == origem.length()) {
                        update_progress();
                        if(deletar_origem) {
                            origem.delete();
                        }
                    } else {
                        update_progress();
                        destino.delete();
                        logGeral.append("Erro ao transferir:");
                        logGeral.append("\n");
                        logGeral.append(origem.toString());
                        erros++;
                    }
                }
            }
        } catch (IOException ex) {
            logErros.append(ex);
            logErros.append("\n");
        }
    }
    
    private void quantidade_de_arquivos() {
        for (File f : origemLista) {
            do_quantidade_de_arquivos(f);
        }
    }

    private void do_quantidade_de_arquivos(File arquivo) {
        if (arquivo.isDirectory()) {
            File files[] = arquivo.listFiles();
            for (File file : files) {
                do_quantidade_de_arquivos(file);
            }
        } else {
            quantidadeDeArquivos++;
        }
    }

    private void update_progress() {
        contaArquivos++;
        long currentpercent = contaArquivos * 100 / quantidadeDeArquivos;
        updateMessage(String.valueOf(currentpercent) + "%");
    }

    private void log_inicio() {
        logGeral.append("\n");
        logGeral.append("Origem:");
        logGeral.append("\n");
        for (File fileOrigemLista : origemLista) {
            logGeral.append(fileOrigemLista.toString());
            logGeral.append("\n");
        }
        logGeral.append("\n");
        logGeral.append("Destino:");
        logGeral.append("\n");
        logGeral.append(destino.toString());
        logGeral.append("\n\n");
        updateTitle(logGeral.toString());
    }

    private void copia_recursiva_completo() {
        if(apartir_da_raiz){
            copia_recursiva_completo_apartir_da_raiz();
        }else{
            copia_recursiva_completo_com_a_raiz();
        }
    }

    private void copia_recursiva_ignora_arquivos_existentes() {
        if(apartir_da_raiz){
            copia_recursiva_ignora_arquivos_existentes_apartir_da_raiz();
        }else{
            copia_recursiva_ignora_arquivos_existentes_com_a_raiz();
        }
    }

    private void log_final() {
        if (erros == 0) {
            logGeral.append("\n");
            logGeral.append("\n");
            logGeral.append("Concluido com sucesso !");
            logGeral.append("\n");
            logGeral.append("Arquivos corrompidos: ");
            logGeral.append(erros);
        } else {
            logGeral.append("\n");
            logGeral.append("\n");
            logGeral.append("Concluido com erros !");
            logGeral.append("\n");
            logGeral.append("Arquivos corrompidos: ");
            logGeral.append(erros);
        }
        logGeral.append("\n\n");
        logGeral.append("Outros erros:");
        logGeral.append("\n");
        logGeral.append(logErros);
        updateTitle(logGeral.toString());
    }

    private void copia_recursiva_ignora_arquivos_existentes_apartir_da_raiz() {
        for (File f : origemLista) {
            if (f.isDirectory()) {
                do_copia_recursiva_ignora_arquivos_existentes(f, destino);
            } else {
                File arquivoDestino = new File(destino, f.getName());
                do_copia_recursiva_ignora_arquivos_existentes(f, arquivoDestino);
            }
        }
    }

    private void copia_recursiva_ignora_arquivos_existentes_com_a_raiz() {
        for (File f : origemLista) {
            if (f.isDirectory()) {
                File diretorioRaiz = new File(destino, f.getName());
                if (!diretorioRaiz.exists()) {
                    diretorioRaiz.mkdir();
                }
                do_copia_recursiva_ignora_arquivos_existentes(f, diretorioRaiz);
            } else {
                File arquivoDestino = new File(destino, f.getName());
                do_copia_recursiva_ignora_arquivos_existentes(f, arquivoDestino);
            }
        }
    }
    
    private void copia_recursiva_completo_apartir_da_raiz(){
        for (File f : origemLista){
            if (f.isDirectory()) {
                do_copia_recursiva_completo(f, destino);
            } else {
                File arquivoDestino = new File(destino, f.getName());
                do_copia_recursiva_completo(f, arquivoDestino);
            }
        }
    }
    
    private void copia_recursiva_completo_com_a_raiz(){
        for (File f : origemLista){
            if (f.isDirectory()) {
                File diretorioRaiz = new File(destino, f.getName());
                if (!diretorioRaiz.exists()) {
                    diretorioRaiz.mkdir();
                }
                do_copia_recursiva_completo(f, diretorioRaiz);
            } else {
                File arquivoDestino = new File(destino, f.getName());
                do_copia_recursiva_completo(f, arquivoDestino);
            }
        }
    }

    private void copia_recursiva() {
        if(ignorar_arquivos_existentes){
            copia_recursiva_ignora_arquivos_existentes();
        }else{
            copia_recursiva_completo();
        }
    }
    
    private String crc32(File file){
        try {
            long hash = FileUtils.checksumCRC32(file);
            return Long.toHexString(hash);
        } catch (IOException ex) {
            logErros.append("\n");
            logErros.append(ex);
        }
        return null;
    }

    private void validacao_apartir_da_raiz() {
        int contador = 0;
        for(File file : origemLista){
            if(file.isDirectory()){
                contador++;
            }
        }
        if(contador > 1){
            apartir_da_raiz = false;
        }
    }

}
