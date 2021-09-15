package utils;

import java.util.regex.Pattern;

public class AppConfig {

    // Path to chrome driver.
    public static final String ChromeDriverPath = "C:\\Software\\chrome\\chromedriver.exe";
    public static String CaptureOutputPath = "E:\\capture\\";
    public static String downloadFolder = "E:\\capture\\downloads\\";
    public static final boolean CaptureJPGImages = true;
    //public static final String URLToCapture = "https://www.bing.com/images/search?q=shark&form=HDRSC3&first=1&tsc=ImageBasicHover";
    //public static final String URLToCapture = "https://www.toptal.com/designers/ui/long-scroll-websites";
    public static final String URLToCapture = "https://www.pornhub.com/view_video.php?viewkey=ph5769782cb4e0c";
    //public static final String URLToCapture = "https://xhamster.com";
    public static final int ChromeThreadWaitTime = 5000;
    public static final int ChromeScrollTimeOut = 15000;
    public static final int PageLoadTimeOut = 60;

    // Define image pattern matching string. This will be used to ensure URLs being accessed must have an image file type, and if not, run other logic to deal with the exception.
    public static final Pattern imgStringPattern = Pattern.compile(".*(.svg$|.jpg$|.jpeg$|.png$|.gif$|.webp$|.tiff$|.psd$|.raw$|.bmp$|.heif$|.indd$|.jpeg2000$|.eps$|.ai$|.pdf$).*", Pattern.CASE_INSENSITIVE);

    // Define the String matching pattern for video file types.
    public static final Pattern vidStringPattern = java.util.regex.Pattern.compile(".*(.mp4$|.mov$|.wmv$|.avi$|.avchd$|.webm$|.mkv$|.flv$|.f4v$|.vob$|.ogv$|.ogg$|.gif$|.gifv$|.mng$|.yuv$|.rm$|.rmvb$|.m4p$|.m4v$|.mpg$|.mpeg$|.mpg$|.mp2$|.mpe$|.mpv$|.m2v$|.3gp$|.3g2$|.nsv$).*", Pattern.CASE_INSENSITIVE);
}
