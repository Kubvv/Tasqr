package com.example.tasqr;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/* Class for static methods that repeat themselves in project */
public class Utilities {

    private static final String TAG = "Utilities";
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private final static ArrayList<String> skillSet = new ArrayList<>(Arrays.asList (
            ".NET",
            "Ada", "Android", "Angular.js","Assembly",
            "Bash", "BASIC",
            "C", "C#", "C++", "COBOL",
            "Django",
            "Elixir",
            "F#", "Flask", "Fortran",
            "Git", "Go", "Groovy",
            "Html/css", "Haskell",
            "Java", "JavaScript", "jQuery",
            "Kotlin",
            "Lisp", "Low Level", "Lua",
            "Matlab",
            "Node.js",
            "Obj-C", "Ocaml",
            "Pascal", "Perl", "PHP", "Python",
            "R", "Rails", "React", "Ruby","Rust",
            "Scala", "Spring", "Sql", "Swift",
            "Unity", "Unix", "Unreal",
            "Windows", "Wordpress",
            "Vue",
            "Xamarin"
    ));


    /* Messages user with long toast message in activity a */
    public static void toastMessage(String message, Activity a) {
        Toast.makeText(a, message, Toast.LENGTH_LONG).show();
    }

    public static String generateHash(String pass, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(salt);
        byte[] hash = digest.digest(pass.getBytes());
        return bytesToHex(hash);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0f];
        }

        return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i+= 2) {
            bytes[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i+1), 16));
        }
        return bytes;
    }

    public static ArrayList<String> getSkillSet() {
        return skillSet;
    }

    public static int getSkillSize() { return skillSet.size(); }
}
