package utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;


public class Apputils {

    // Utility used to File Copy.
    public static String fileCopy (String srcFile, String srcFileName, String dstFolder) throws IOException {

        String dstPath = "";

        // 1. Check if dst string has a slash in it. if not append to the string.
        if (dstFolder.endsWith("\\") == false) {
            dstFolder = dstFolder + "\\";
        }

        IOException ex = new IOException();
        // 1. Check if DstFolder exists. If not create it.
        Path dstFolderPath = Paths.get(dstFolder);
        if ((Files.exists(dstFolderPath)) == false) {
            System.out.println("Destination Folder does not exist. Attempting to create.");

            try {
                Files.createDirectories(dstFolderPath);
            } catch (IOException e){
                System.out.println("Error creating directory");
                e.printStackTrace();
            }
        }

        // 2. Try to move the file to the target destination
        try {
            Path fileMove = Files.move
                    (Paths.get(srcFile),
                            Paths.get(dstFolder + "\\" + srcFileName));
            dstPath = dstFolder + "\\" + srcFileName;
        } catch (IOException e) {
            System.out.println("IOException occurred trying to move folder: ");
            e.printStackTrace();
            dstPath = "";

        }

        return dstPath;
    }

    // Used to convert images to JPEG. Will take in PNG files
    public static Hashtable<String, String> imagePNGToJPG (String srcImage, String srcBase64, String dstImage) throws IOException {
        Hashtable<String,String> returnTypes = new Hashtable<String,String>();

        // set the status to false by default within the hashtable.
        returnTypes.put("status", "false");

        // get the source file and convert to Path.


        // Get the destination path, create an empty file and convert String to Path type.
        File outputFile = null;
        Path srcPath = null;
        try {
            srcPath = Paths.get(srcImage);
            outputFile = new File(dstImage);
            returnTypes.put("dstImagePath", outputFile.getPath());

        } catch (java.nio.file.InvalidPathException e) {
            System.out.println("Cannot Get the path when trying to convert image to JPEG. Path: " + srcImage);
            e.printStackTrace();
            returnTypes.put("status", "false");

        } catch (NullPointerException e) {
            System.out.println("Failed to create the outFile file.");
            e.printStackTrace();
            returnTypes.put("status", "false");
        }

        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(srcPath.toFile());

        } catch (Exception e) {
            System.out.print("Error creating a buffered Image before conversion. Either the InputStream for ImageIO.read() is in an incorrect format, or the input is null.");
            e.printStackTrace();
            returnTypes.put("status", "false");
            return returnTypes;
        }

        // Create a blank, RGB, same width and height as the original image.

        // jpg needs BufferedImage.Type_Int_RGB
        // png needs BufferedImage.Type_INT_ARGB
        BufferedImage newBufferedImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        // draw a white background and put the originalImage on it.
        newBufferedImage.createGraphics()
                .drawImage(originalImage,
                        0,
                        0,
                        Color.WHITE,
                        null);

        // save the image to disk
        try {
            ImageIO.write(newBufferedImage, "jpg", outputFile);
            System.out.println("Writing jpg file to: " + outputFile);
            returnTypes.put("status", "true");

        } catch (Exception e) {
            System.out.println("Could not write file to disk: " + outputFile.getPath());
            e.printStackTrace();
            returnTypes.put("status", "false");
            return returnTypes;
        }
        return returnTypes;
    }

    // Used to convert Images to PDF. Will take either a Base64 string of the content to be written, or a filepath to the file to be written to.
    public static boolean imageToPDF (String srcImage, String srcBase64, String dstImage) throws IOException {
        boolean status = false;

        //1. Check if dst string has a slash in it. if not append to the string.

        //if (dstFolder.endsWith("\\") == false) {
        //    dstFolder = dstFolder + "\\";
        // }

        if (srcImage.length() > 1) {

            try {
                // Create initial document and configure pdf page parameters.
                float left = 20;
                float right = 20;
                float top = 20;
                float bottom = 20;
                Document document = new Document(PageSize.A3, left, right, top, bottom);
                // Write the base64 string to the file.
                FileOutputStream fos = new FileOutputStream(dstImage);
                PdfWriter writer = PdfWriter.getInstance(document, fos);
                writer.open();
                document.open();
                Image image = Image.getInstance(srcImage);

                if (image.getHeight() > 3000) {
                    image.scalePercent(300);
                }

                //image.scaleAbsoluteWidth(PageSize.A4.getWidth());
                //image.scaleAbsoluteHeight(PageSize.A4.getWidth());
                PdfPTable table = new PdfPTable(1);
                table.setWidthPercentage(90);
                PdfPCell cellone = new PdfPCell(image);
                cellone.setBorder(Rectangle.NO_BORDER);
                cellone.setCalculatedHeight(image.getHeight());
                table.addCell(cellone);


                //image.scaleAbsolute(PageSize.A4);
                //document.add(image);

                document.add(table);
                document.close();
                writer.close();
                status = true;

            } catch (Exception e) {
                System.out.println("Error occurred writing to PDF file!");
                e.printStackTrace();
                status = false;
            }
        }
        return status;
    }
}
