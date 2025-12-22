package common;

public class Hasher {

    public Hasher() {}

    public String calculateMD5 (String entrada) {

        return calculateMD5(entrada.toLowerCase());
    }
}
