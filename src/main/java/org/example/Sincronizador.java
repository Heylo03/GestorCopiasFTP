package org.example;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sincronizador {

    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "root";
    private static final String PASSWORD = "root";
    private static final String CARPETA_LOCAL = "D:/SubidasServidor/local";
    private static final String CARPETA_SERVIDOR_FTP = "D:/SubidasServidor/remota";
    private static final int TIEMPO_ESPERA = 20;

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(Sincronizador::sincronizacion, 0, TIEMPO_ESPERA, TimeUnit.SECONDS);
    }

    /**
     * Metodo principal de la clase,sincroniza la carpeta de la nube a los cambios realizados en la local
     */
    public static void sincronizacion() {
        try {
            //login cliente
            FTPClient clienteFtp = new FTPClient();
            clienteFtp.connect(SERVIDOR, PUERTO);
            clienteFtp.login(USUARIO, PASSWORD);
            //Salida del contenido de ambas carpetas (en caso de desync tarda una sincronizacion extra en verse el cambio)
            System.out.println("SINCRONIZANDO....");
            List<String> listaArchivosLocal = listadoArchivosLocales(CARPETA_LOCAL);
            System.out.println("LOCAL : " + listaArchivosLocal);
            List<String> listaArchivosRemotos = listadoArchivosRemotos(clienteFtp, CARPETA_SERVIDOR_FTP);
            System.out.println("NUBE : " + listaArchivosRemotos);
            //Busqueda de archivos en la nube que no existan en la carpeta local,en caso de ser así se borran
            for (String archivoRemoto : listaArchivosRemotos) {
                if (!listaArchivosLocal.contains(archivoRemoto)) {
                    borraArchivo(clienteFtp, archivoRemoto);
                }
            }
            //Busqueda de archivos que no se encuentren en la nube o que no estén actualizados,en ese caso los sube al servidor
            for (String archivoLocal : listaArchivosLocal) {
                if (!listaArchivosRemotos.contains(archivoLocal) || archivoActualizado(clienteFtp, CARPETA_SERVIDOR_FTP, archivoLocal)) {
                    subeArchivo(clienteFtp, archivoLocal);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo que devuelve una lista de los archivos subidos a la nube,posteriormente los borraremos o modificaremos segun la carpeta local
     * @param clienteFTP Cliente registrado en el servidor
     * @param carpeta Carpeta donde se encuentran los archivos de la nube
     * @return  Listado de archivos de la nube
     * @throws IOException
     */
    public static List<String> listadoArchivosRemotos(FTPClient clienteFTP, String carpeta) throws IOException {
        List<String> listaRemota = new ArrayList<>();
        clienteFTP.changeWorkingDirectory(carpeta);
        Collections.addAll(listaRemota, clienteFTP.listNames());
        return listaRemota;
    }

    /**
     * Metodo que devuelve el listado de los archivos locales del equipo
     * @param carpetaLocal carpeta de los archivos del equipo para subir a la nube
     * @return listado de archivos de la carpetaLocal
     */
    public static List<String> listadoArchivosLocales(String carpetaLocal) {
        List<String> listaLocal = new ArrayList<>();
        File directorio = new File(carpetaLocal);
        File[] ListaArchivos = directorio.listFiles();
        if (ListaArchivos != null) {
            for (File archivo : ListaArchivos) {
                listaLocal.add(archivo.getName());
            }
        }
        return listaLocal;
    }

    /**
     * Metodo booleano que usaremos para obtener la ultima versión de un archivo en funcion de su fecha de modificación
     * @param clienteFTP Cliente del servidor
     * @param carpetaRemota carpeta de la nube dentro del servidor
     * @param nombreArchivo nombre del archivo que queremos comprobar si tenemos su ultima version tanto en local como en la nube
     * @return true para el caso de que sea la ultima versión , false en caso contario
     * @throws IOException
     */
    public static boolean archivoActualizado(FTPClient clienteFTP, String carpetaRemota, String nombreArchivo) throws IOException {
        clienteFTP.changeWorkingDirectory(carpetaRemota);
        FTPFile[] archivosRemotos = clienteFTP.listFiles();
        for (FTPFile archivoRemoto : archivosRemotos) {
            long ultimaModificacionRemota = archivoRemoto.getTimestamp().getTimeInMillis();
            File archivoLocal = new File(CARPETA_LOCAL + "/" + nombreArchivo);
            long ultimaModificacionLocal = archivoLocal.lastModified();
            return ultimaModificacionLocal > ultimaModificacionRemota;
        }
        return false;
    }

    /**
     * Metodo que borra el archivo de la nube
     * @param clienteFTP cliente del servidor
     * @param archivo archivo que queremos borrar
     * @throws IOException
     */
    public static void borraArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        clienteFTP.deleteFile(archivo);
    }

    /**
     * Metodo que sube el archivo de la carpeta local a la nube
     * @param clienteFTP cliente del servidor
     * @param archivo archivo a subir
     * @throws IOException
     */
    public static void subeArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        File ficheroLocal = new File(CARPETA_LOCAL + File.separator + archivo);
        FileInputStream fis = new FileInputStream(ficheroLocal);
        clienteFTP.storeFile(archivo, fis);
        fis.close();
    }
}
