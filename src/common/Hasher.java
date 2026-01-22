package common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {

    public static String calculateSHA(String entrada) {

        try {
            //OBLIGA HA CAPTAR EXCEPTION POR SI EL ALGORITMO QUE RECLAMO NO EXISTE.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            //DIVIDIMOS EN UN ARRAY DE BYTES LA ENTRADA QUE NOS HAN PASADO
            byte[] hashBytes = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));

            //CREAMOS VACIO EL BUILDEADOR DE STRINGS
            StringBuilder hexString = new StringBuilder();

            //RECORREMOS EL ARRAY DE BYTES YA REVUELTO PARA DARLE UN NUMERO HEXADECIMAL A CADA UNO
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);

                //SI LA CIFRA DEL BYTE SALE 1, LE AÑADIMOS UN 0.
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                //AQUÍ VAMOS SUMANDO LOS BYTES YA CONVERTIDOS EN DOS DÍGITOS.
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
