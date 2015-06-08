package fh_dortmund_hagmans.einkauf.models;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/** Stellt einen Nutzer des Systems dar.
 * @author Hendrik Hagmans
 */
public class User {

    private String name;
    private String password;
    private String regKey = null;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegKey() {
        return regKey;
    }

    public void setRegKey(String regKey) {
        this.regKey = regKey;
    }

    public static String byte2HexStr(byte binary) {
        StringBuffer sb = new StringBuffer();
        int hex;

        hex = (int) binary & 0x000000ff;
        if (0 != (hex & 0xfffffff0)) {
            sb.append(Integer.toHexString(hex));
        } else {
            sb.append("0" + Integer.toHexString(hex));
        }
        return sb.toString();
    }

    /**
     * Verschlüsselt das angegebene Passwort
     *
     * @param password
     * @return
     */
    public static String encryptPassword(String password) {
        DESKeySpec dk;
        SecretKey secretKey = null;
        try {
            dk = new DESKeySpec(new Long(7490854493772951678L).toString()
                    .getBytes());
            SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
            secretKey = kf.generateSecret(dk);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cipher c;

        try {
            c = Cipher.getInstance("DES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted;
            encrypted = c.doFinal(password.getBytes());

            // convert into hexadecimal number, and return as character string.
            String result = "";
            for (int i = 0; i < encrypted.length; i++) {
                result += byte2HexStr(encrypted[i]);
            }

            return result;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Entschlüsselt das von encryptPassword verschlüsselte Passwort.
     *
     * @param password
     * @return
     */
    public static String decryptPassword(String password) {
        DESKeySpec dk;
        SecretKey secretKey = null;
        if (password.length() == 16) {
            try {
                dk = new DESKeySpec(new Long(7490854493772951678L).toString()
                        .getBytes());
                SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
                secretKey = kf.generateSecret(dk);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Cipher c;

            try {
                byte[] tmp = new byte[password.length() / 2];
                int index = 0;
                while (index < password.length()) {
                    // convert hexadecimal number into decimal number.
                    int num = Integer.parseInt(
                            password.substring(index, index + 2), 16);

                    // convert into signed byte.
                    if (num < 128) {
                        tmp[index / 2] = new Byte(Integer.toString(num))
                                .byteValue();
                    } else {
                        tmp[index / 2] = new Byte(
                                Integer.toString(((num ^ 255) + 1) * -1))
                                .byteValue();
                    }
                    index += 2;
                }

                c = Cipher.getInstance("DES/ECB/PKCS5Padding");
                c.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(c.doFinal(tmp));
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }

            return "";
        } else {
            return "";
        }
    }
}
