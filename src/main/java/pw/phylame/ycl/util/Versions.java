package pw.phylame.ycl.util;

public final class Versions {
    public static final int jvmVersion;

    static {
        jvmVersion = Runtime.class.getPackage().getSpecificationVersion().charAt(2) - '0';
    }

}
