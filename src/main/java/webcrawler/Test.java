package webcrawler;

import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) throws Exception {


        Pattern imgStringPattern = Pattern.compile(".*(\\\\$).*", Pattern.CASE_INSENSITIVE);
        String testS = "E:\\capture\\";

        boolean t = testS.matches(String.valueOf(imgStringPattern));
        System.out.println("The boolean of t is: " + t);
    }

}
