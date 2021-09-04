package webcrawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import utils.AppConfig;
import utils.Apputils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class crawler {
    public static void main(String[] args) throws Exception {

        // 1. Set paramaters and establish new instance of Chrome Driver.
        System.setProperty("webdriver.chrome.driver", AppConfig.ChromeDriverPath);
        WebDriver driver = new ChromeDriver();

        // 2. Get a web page and then sleep, settings some parameters etc.
        driver.manage().window().maximize();
        driver.get(AppConfig.URLToCapture);
        Thread.sleep(AppConfig.ChromeThreadWaitTime);
        driver.manage().timeouts().pageLoadTimeout(AppConfig.PageLoadTimeOut, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(AppConfig.ChromeThreadWaitTime, TimeUnit.MILLISECONDS);


        // Set Chrome Options
        ChromeOptions chromeOpt = new ChromeOptions();
        chromeOpt.addArguments("disable-infobars");
        chromeOpt.addArguments("--disable-extensions");


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime captureDTTemp = LocalDateTime.now();
        String captureDT = dtf.format(captureDTTemp).replace(":","").replace("/","").replace(" ", "");

        // Record some metadata into variables captured instance of the website.
        String title = driver.getTitle();
        try {
            title = title.substring(0, 30);
        } catch (Exception e) {}


        // Take Screenshot of the view point of the page (not the entire page if it scrolls).
        File file = ((TakesScreenshot) driver).getScreenshotAs((OutputType.FILE));
        String screenshotBase64 = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);

        // Give the Screenshot a name before trying to copy the file.
        String ssp = (title + "." + captureDT + ".ScreenShot.png");

        // Move the screenshot to the destination folder path.
        String screenshotFirstPage = null;
        try {
            screenshotFirstPage = Apputils.fileCopy(file.getPath(), ssp ,AppConfig.CaptureOutputPath);
        } catch (IOException e) {
            System.out.println("Failed to move file: ");
            e.printStackTrace();
        }
        // Convert initial Screenshot to a Path type for later use.
        Path pathScreenshotFull = Paths.get(screenshotFirstPage);

        // Take Full Screen Shot (pathScreenshotfull) of the whole page including rolling screen.
        Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        String screenshotFull = (AppConfig.CaptureOutputPath + title + "." + captureDT + ".FullScreen.png");
        // Convert Initial Screenshot to a Path type for later use.
        Path pathScreenshot = Paths.get(screenshotFull);
        System.out.println("Full Screen shot path is: " + pathScreenshot.toString());

        try {
            ImageIO.write(screenshot.getImage(),"PNG",new File(screenshotFull));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get image links from the web page and store within a  hash table.
        System.out.println("Prepping to Identify images!!!");
        List<WebElement> imagesList = (List<WebElement>) driver.findElements(By.tagName("img"));

        for (WebElement i : imagesList) {
            System.out.println("<---------->");
            System.out.println(i.getAttribute("src"));
            System.out.println(i.getAttribute("alt"));
            System.out.println("<---------->");
        }

        int count = 0;
        for (WebElement i : imagesList) {
            // Get the src URL of the file.
            String iSrc = i.getAttribute("src");
            String iSrcFullName = i.getAttribute("alt");

            //Get the file name
            String[] nameSplit = iSrc.split("/");
            String iSrcName = (nameSplit[nameSplit.length -1]);
            // get the file extension of a 3 char file extension. If it detects a 4 char string, fix the sub string.

            String iSrcExt = "";
            try {
                iSrcExt = iSrcName.substring(iSrcName.length() - 4);

                if (iSrcExt.substring(iSrcExt.charAt(0)) == ".") {
                    iSrcExt = iSrcName.substring(iSrcName.length() - 3);
                    System.out.println("iSrcExt Var is: " + iSrcExt);
                } else {
                    System.out.println(iSrcExt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Getting Image located at: " + iSrc);
            URL iURL = new URL(iSrc);

            BufferedImage saveImage = null;
            try {
                saveImage = ImageIO.read(iURL);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String outPath = (AppConfig.CaptureOutputPath + "_" + count + "_" + iSrcName);
            System.out.println("Writing the following to disk!: " + outPath);
            ImageIO.write(saveImage, "png", new File(outPath));
            count ++;
        }

        // Close the Google Chrome instance and do some cleanup
        driver.manage().deleteAllCookies();
        driver.close();

        // Convert Image from PNG to JPG  before sending to the PDF Converter method. PNG's are lossless and cannot be converted straight to PDF hence the JPG step.
        // Check if the path variable to the input file exits then execute.
        
        if (AppConfig.CaptureJPGImages == true) {
            String fsspJPG = "";
            if (Files.exists((pathScreenshot)) == true) {
                String fssnJPEG = (AppConfig.CaptureOutputPath + title + "." + captureDT + "FullScreen.jpg");
                Hashtable<String, String> convert = Apputils.imagePNGToJPG(screenshotFull, "", fssnJPEG);
                fsspJPG = convert.get("dstImagePath");
            }

            String fssJPG = "";
            if ((Files.exists(pathScreenshotFull)) == true) {
                String fssnJPG = (AppConfig.CaptureOutputPath + title + "." + captureDT + ".jpg");
                Hashtable<String, String> convert = Apputils.imagePNGToJPG(screenshotFirstPage, "", fssnJPG);
                fssJPG = convert.get("dstImagePath");
            }            
        }

        // Convert Images to PDF

        if ((Files.exists(pathScreenshot)) == true) {
            String fssnPDF = (AppConfig.CaptureOutputPath + title + "." + captureDT + ".FullScreen.pdf");
            Apputils.imageToPDF(screenshotFull, "", fssnPDF);
            System.out.println("Saving to file: " + screenshotFull);
        }

        if ((Files.exists(pathScreenshotFull)) == true) {
            String fssPDF = screenshotFirstPage.replace(".png", ".pdf");
            Apputils.imageToPDF(screenshotFirstPage, "", fssPDF);
            System.out.println("Saving to file: " + screenshotFirstPage);
        }

    }
}