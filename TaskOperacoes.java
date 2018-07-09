
package Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javafx.concurrent.Task;


public class TaskOperacoes extends Task<Boolean> {

    private int bufferSize;
    private long tamanhoTotalEmBytes;
    private long bytesTransferido;
    private final File origem;
    private final File destino;
    private final String extensao;
    private final Integer modo;
    private Integer erroTransferencia;

    public TaskOperacoes(File origem, File destino, String extensao, Integer modo) {
        this.origem = origem;
        this.destino = destino;
        this.extensao = extensao;
        this.modo = modo;
    }
    
    @Override
    protected Boolean call() throws Exception {
        calculaTamanhoTotal();
        if(modo != null) {
            switch (modo) {
                case 1:
                    copiar(origem, destino);
                    break;
                case 2:
                    excluir(destino);
                    break;
                case 3:
                    mover(origem, destino);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private String copiarArquivo(File origem, File destino) {
        try (InputStream in = new FileInputStream(origem);
                OutputStream out = new FileOutputStream(destino)) {
            int lido;
            byte[] buffer = new byte[bufferSize];
            Checksum crc32 = new CRC32();
            while ((lido = in.read(buffer)) != -1) {
                out.write(buffer, 0, lido);
                crc32.update(buffer, 0, lido);
                updateProgress(lido);
            }
            return Long.toHexString(crc32.getValue());
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return null;
    }

    private void bufferDinamico(File file) {
        if (file.length() <= 51200) {
            bufferSize = 1024;
        } else if (file.length() > 51200 && file.length() <= 52428800) {
            bufferSize = 1048576;
        } else if (file.length() > 52428800 && file.length() <= 314572800) {
            bufferSize = 4194304;
        } else if (file.length() > 314572800) {
            bufferSize = 8388608;
        }
    }

    private void calculaTamanhoTotal() {
        for(File f : origem.listFiles()) {
            if (f.getName().toLowerCase().endsWith(extensao)) {
                tamanhoTotalEmBytes += f.length();
            }
        }         
    }
    
    private void mover(File origem, File destino) throws IOException {
        erroTransferencia = null;
        for(File f : origem.listFiles()){
            if(f.getName().toLowerCase().endsWith(extensao)){
                File destinoFinal = new File(destino, f.getName());
                if(!destinoFinal.exists()){
                    bufferDinamico(f);
                    String hashAntes = copiarArquivo(f, destinoFinal);
                    String hashDepois = crc32File(destinoFinal);
                    if(!hashDepois.equals(hashAntes)){
                        System.err.println("Erro ao transferir:");
                        System.err.println(f.toString());
                        destinoFinal.delete();
                        erroTransferencia++;
                    } else{
                        f.delete();
                    }
                }
            }
        }
        if(erroTransferencia != null){
            copiar(origem, destino);
        }
    }
    
    
    private String crc32File(File file) {
        try (InputStream in = new FileInputStream(file)) {
            int lido;
            byte[] buffer = new byte[bufferSize];
            Checksum crc32 = new CRC32();
            while ((lido = in.read(buffer)) != -1) {
                crc32.update(buffer, 0, lido);
            }
            return Long.toHexString(crc32.getValue());
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return null;
    }
    
    
    private void updateProgress(long lido) {
        bytesTransferido += lido;
        updateProgress(bytesTransferido, tamanhoTotalEmBytes);
        long currentpercent = bytesTransferido * 100 / tamanhoTotalEmBytes;
        updateMessage(String.valueOf(currentpercent) + "%");
    }
    
    
    private void excluir(File caminho) {
        for(File f : origem.listFiles()){
            if(f.getName().toLowerCase().endsWith(extensao)){
                f.delete();
            }
        }
        updateProgress(tamanhoTotalEmBytes);
    }

    private void copiar(File origem, File destino) throws IOException {
        erroTransferencia = null;
        for(File f : origem.listFiles()){
            if(f.getName().toLowerCase().endsWith(extensao)){
                File destinoFinal = new File(destino, f.getName());
                if(!destinoFinal.exists()){
                    bufferDinamico(f);
                    String hashAntes = copiarArquivo(f, destinoFinal);
                    String hashDepois = crc32File(destinoFinal);
                    if(!hashDepois.equals(hashAntes)){
                        System.err.println("Erro ao transferir:");
                        System.err.println(f.toString());
                        destinoFinal.delete();
                        erroTransferencia++;
                    }
                }
            }
        }
        if(erroTransferencia != null){
            copiar(origem, destino);
        }
    }

}
