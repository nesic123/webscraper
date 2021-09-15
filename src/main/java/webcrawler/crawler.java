package webcrawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import utils.AppConfig;
import utils.Apputils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class crawler {
    public static void main(String[] args) throws Exception {

        // Check if the output path has no \ at the end. If Not then append one.
        Pattern CaptureOutputPathPattern = Pattern.compile(".*(\\\\$).*", Pattern.CASE_INSENSITIVE);
        if (AppConfig.CaptureOutputPath.matches(String.valueOf(CaptureOutputPathPattern)) == false) {
            System.out.println("Capture Output Path doesnt have a proceeding \\. Adding one before proceeding");
            AppConfig.CaptureOutputPath = AppConfig.CaptureOutputPath + "\\\\";
        }

        // Create Download Folder
        File downloadFolder = new File(AppConfig.downloadFolder);
        //downloadFolder.mkdir();

        // Map Chrome Options
        Map<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadFolder.getAbsolutePath());


        // Set Parameters and establish new instance of Chrome Driver.
        System.setProperty("webdriver.chrome.driver", AppConfig.ChromeDriverPath);
        WebDriver driver = new ChromeDriver();

        // Get a web page and then sleep, settings some parameters etc.
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

        // Click on any buttons that may need to be clicked to ignore welcome/first load time messages.

        try {
            driver.findElement(By.cssSelector("body > div.dialog-desktop-container.desktop-dialog-open > div.dialog-desktop-container__content.dlg-content > div > div.desktop-dialog__footer > button")).click();
        } catch (Exception e) {
            System.out.println("Did not find initial welcome message. Skipping.");
        }

        try {
            driver.findElement(By.cssSelector("#playerDiv_80181591 > div.mgp_preRollSkipButton.mgp_skipable > div")).click();
        } catch (Exception e) {
            System.out.println("Did not find initial player click button. Skipping.");
        }

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
        Path pathScreenshotFull = null;
        try {
            pathScreenshotFull = Paths.get(screenshotFirstPage);
        } catch (Exception e) {

        }

        // Take Full Screen Shot (pathScreenshotfull) of the whole page including rolling screen.
        Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        String screenshotFull = (AppConfig.CaptureOutputPath + title + "." + captureDT + ".FullScreen.png");
        // Convert Initial Screenshot to a Path type for later use.
        Path pathScreenshot = Paths.get(screenshotFull);
        System.out.println("Full Screen shot path is: " + pathScreenshot);

        try {
            ImageIO.write(screenshot.getImage(),"PNG",new File(screenshotFull));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get image links from the web page and store within a hash table.
        System.out.println("Prepping to Identify images!!!");
        List<WebElement> imagesList = (List<WebElement>) driver.findElements(By.xpath("//*/img"));

        // Iterate through the list of images. Count to 250 before stopping capturing any more images.
        int countImg = 0;
        for (WebElement i : imagesList) {

            System.out.println("Count is at: " + countImg);
            if (countImg <= 250) {
                // If an image extension is not found in the URL, go to the else statement.
                String iSrc = i.getAttribute("src");

                // Deal with empty iSrc trings. If found make them not null.
                if (iSrc == null) {
                    iSrc = " ";
                }

                if (iSrc.matches(String.valueOf(AppConfig.imgStringPattern)) == true) {

                    countImg = countImg++;
                    System.out.println("<---------->");
                    System.out.println("src attribute: " + i.getAttribute("src"));
                    System.out.println("alt attribute = " + i.getAttribute("alt"));
                    System.out.println("<---------->");

                    // Get the src URL of the file.
                    //String iSrc = i.getAttribute("src");
                    String iSrcFullName = i.getAttribute("alt");

                    //Get the file name
                    String[] nameSplit = iSrc.split("/");
                    String iSrcName = (nameSplit[nameSplit.length -1]);
                    // get the file extension of a 3 char file extension. If it detects a 4 char string, fix the sub string.

                    String iSrcExt = "";
                    try {
                        iSrcExt = iSrcName.substring(iSrcName.length() - 4);
                        String iSrcSub = iSrcExt.substring(0, 1);
                        if (iSrcSub == ".") {
                            iSrcExt = iSrcName.substring(iSrcName.length() - 3);
                            System.out.println("iSrcExt Var is: " + iSrcExt);
                        } else {
                            System.out.println(iSrcExt);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("Getting Image located at: " + iSrc);

                    // Initiate a non chromium connection and download the file to disk.
                    URL iURL = new URL(iSrc);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) iURL.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    // Copy Cookies from chromium to the new URL connection just in case it is protected by a user login portal etc.
                    Set<Cookie> cookies = driver.manage().getCookies();
                    String cookingString = "";
                    for (Cookie cookie : cookies) {
                        cookingString += cookie.getName() + "=" + cookie.getValue() + ";";
                    }

                    // Attempt to download file.
                    String imgName = (AppConfig.CaptureOutputPath + "siteImages\\" + iSrcName);
                    Path imageOutFolder = Path.of((AppConfig.CaptureOutputPath + "siteImages\\"));
                    System.out.println("Writing the following to disk: " + imgName);

                    // Check if folder to download images to exists. If not create the folder.
                    if (!(Files.exists(imageOutFolder))) {
                        System.out.println("Destination Folder does not exist. Attempting to create.");

                        try {
                            Files.createDirectories(imageOutFolder);
                        } catch (IOException e){
                            System.out.println("Error creating directory: " + imageOutFolder);
                            e.printStackTrace();
                        }
                    }

                    try (InputStream in = httpURLConnection.getInputStream()) {
                        Files.copy(in, new File(imgName).toPath());
                    } catch (Exception e) {
                        System.out.println("An error has occurred writing images to disk.");
                    }
                }
            }
        }

        // Get video links from the web page and store within a  hash table.
        System.out.println("Prepping to Identify videos!!!");
        // Define image pattern matching string. This will be used to ensure URLs being accessed must have an image file type, and if not, run other logic to deal with the exception.

        // Build list of video files found on the website.
        List<WebElement> videosList = (List<WebElement>) driver.findElements(By.xpath("//div[@class='video-title']"));

        // Print out a list of src links for videos that it found.
        int countVid = 0;
        for (WebElement i: videosList) {
            countVid = countVid ++;
            System.out.println("Count is at: " + countVid);
            if (countVid <= 250) {

                // Locate the Src link to the video file.
                String iSrc = i.getAttribute("src");

                // Deal with empty iSrc strings. If found make th
                // em not null.
                if (iSrc == null) {
                    iSrc = " ";
                }

                if (iSrc.matches(String.valueOf(AppConfig.vidStringPattern)) == true) {

                    System.out.println("<---------->");
                    System.out.println("src attribute: " + i.getAttribute("src"));
                    System.out.println("alt attribute = " + i.getAttribute("alt"));
                    System.out.println("<---------->");

                    // Get the src URL of the file.
                    //String iSrc = i.getAttribute("src");
                    String iSrcFullName = i.getAttribute("alt");

                    //Get the file name
                    String[] nameSplit = iSrc.split("/");
                    String iSrcName = (nameSplit[nameSplit.length -1]);

                    // Get the file extension of a 3 char file extension. If it detects a 4 char string, fix the sub string.
                   /* String{
                        iSrcExt = iSrcName.substring(iSrcName.length() - 4);
                        String iSrcSub = iSrcExt.substring(0, 1);
                        if (iSrcSub == ".") {
                            iSrcExt = iSrcName.substring(iSrcName.length() - 3);
                            System.out.println("iSrcExt Var is: " + iSrcExt);
                        } else {
                            System.out.println(iSrcExt);
                        }
                    }g iSrcExt = "";
                    try  catch (Exception e) {
                        e.printStackTrace();
                    }

                    */

                    System.out.println("Getting Video located at: " + iSrc);

                    // Initiate a non chromium connection and download the file to disk.
                    URL iURL = new URL(iSrc);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) iURL.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    // Copy Cookies from chromium to the new URL connection just in case it is protected by a user login portal etc.
                    Set<Cookie> cookies = driver.manage().getCookies();
                    String cookingString = "";
                    for (Cookie cookie : cookies) {
                        cookingString += cookie.getName() + "=" + cookie.getValue() + ";";
                    }

                    // Attempt to download file.
                    String vidName = (AppConfig.CaptureOutputPath + "siteVideos\\" + iSrcName);
                    Path vidOutFolder = Path.of((AppConfig.CaptureOutputPath + "siteVideos\\"));
                    System.out.println("Writing the following to disk: " + vidName);

                    // Check if folder to download images to exists. If not create the folder.
                    if (!(Files.exists(vidOutFolder))) {
                        System.out.println("Destination Folder does not exist. Attempting to create.");

                        try {
                            Files.createDirectories(vidOutFolder);
                        } catch (IOException e){
                            System.out.println("Error creating directory: " + vidOutFolder);
                            e.printStackTrace();
                        }
                    }

                    try (InputStream in = httpURLConnection.getInputStream()) {
                        Files.copy(in, new File(vidName).toPath());
                    } catch (Exception e) {
                        System.out.println("An error has occurred writing images to disk.");
                    }
                }
            }
        }

        // Close the Google Chrome instance and do some cleanup
        driver.manage().deleteAllCookies();
        driver.quit();

        // Convert Image from PNG to JPG  before sending to the PDF Converter method. PNG's are lossless and cannot be converted straight to PDF hence the JPG step.
        // Check if the path variable to the input file exits then execute.
        if (AppConfig.CaptureJPGImages) {
            String fsspJPG = "";
            if (Files.exists((pathScreenshot))) {
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