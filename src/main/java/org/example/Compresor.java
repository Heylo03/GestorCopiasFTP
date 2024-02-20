package org.example;

import materialProfesor.GestorFTP;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compresor {

    /**
     * Metodo que comprime una carpeta y la guarda dentro del dispositivo con una extensión rar
     * @param carpetaOrigen ubicacion de la carpeta que queremos comprimir
     * @param archivoDestino ubicacion de donde vamor a guardar el archivo ya comprimido
     * @throws IOException
     */
    public static void comprimirCarpeta(String carpetaOrigen, String archivoDestino) throws IOException {
        FileOutputStream fos = new FileOutputStream(archivoDestino);
        ZipOutputStream zos = new ZipOutputStream(fos);
        File carpeta = new File(carpetaOrigen);

        comprimirRecursivo(carpeta, carpeta.getName(), zos);
        zos.close();
    }

    /**
     * Metodo que comprime un directorio de forma recursivo
     * @param archivo archivo que queremos comprimir
     * @param nombreBase nombre de como queremos guardar el archivo rar
     * @param zos Salida de datos para comprimir archivos
     * @throws IOException
     */
    private static void comprimirRecursivo(File archivo, String nombreBase, ZipOutputStream zos) throws IOException {
        //comprimir subdirectorios
        if (archivo.isDirectory()) {
            for (File archivoHijo : archivo.listFiles()) {
                comprimirRecursivo(archivoHijo, nombreBase + "/" + archivoHijo.getName(), zos);
            }
        } else {
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(archivo);
            zos.putNextEntry(new ZipEntry(nombreBase));
            int longitud;
            while ((longitud = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, longitud);
            }
            zos.closeEntry();
            fis.close();
        }
    }


        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Ingrese la ruta de la carpeta a comprimir:");
            String carpetaOrigen = scanner.nextLine();
            System.out.println("Ingrese el nombre para el archivo comprimido:");
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
            String archivoDestino = scanner.nextLine()+formatoFecha.format(new Date())+ ".zip";

            // Comprimir la carpeta
            try {
                Compresor.comprimirCarpeta(carpetaOrigen, archivoDestino);
                System.out.println("Carpeta comprimida correctamente.");
            } catch (IOException e) {
                System.err.println("Error al comprimir la carpeta: " + e.getMessage());
                return;
            }

            // Subir el archivo comprimido al servidor FTP
            GestorFTP gestorFTP = new GestorFTP();
            try {
                gestorFTP.conectar();

                boolean subido = gestorFTP.subirFichero(archivoDestino);
                if (subido) {
                    System.out.println("Archivo subido correctamente al servidor FTP.");
                } else {
                    System.err.println("Error al subir el archivo al servidor FTP.");
                }
            } catch (Exception e) {
                System.err.println("Error al conectar con el servidor FTP: " + e.getMessage());
            } finally {
                try {
                    gestorFTP.desconectar();
                } catch (IOException e) {
                    System.err.println("Error al desconectar del servidor FTP: " + e.getMessage());
                }
            }
        }
    }



