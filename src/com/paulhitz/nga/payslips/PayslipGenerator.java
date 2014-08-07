package com.paulhitz.nga.payslips;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

/**
 * Convert a specified text file into a PDF. It will correctly format the text
 * and add a payslip image to the background.
 *
 * @author Paul Hitz
 * @version 101129
 */
public class PayslipGenerator {
    /**
     * The payslip image to use as the background on every page.
     */
    private static final URL BACKGROUND_IMAGE = PayslipGenerator.class.getResource("/resources/templates/payslip_background.png");

    /**
     * The font for all of the payslip text.
     */
    private static final Font PAYSLIP_FONT = new Font(Font.FontFamily.COURIER, 8, Font.NORMAL);

    /**
     * This is used by the source file to separate each payslip.
     */
    private static final String PAYSLIP_SEPARATOR = "\\n\\n\\n1";


    /**
     * Instantiate a new <code>PayslipGenerator</code> object.
     */
    public PayslipGenerator() {
        //Do nothing.
    }

    /**
     * Convert the supplied text file(s) to a PDF of payslips.
     *
     * @param args An array of <code>String</code>s containing the files to convert.
     */
    public static void main (String[] args) {
        //Ensure the app was called correctly.
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            displayHelp();
            return;
        }
        if (args[0].equalsIgnoreCase("version")) {
            displayVersionInfo();
            return;
        }

        //Iterate through each file supplied and try to generate a PDF for each.s
        for (String file : args) {
            //Read in the specified file and extract the contents.
            System.out.println("Attempting to read file... " + file);
            String content;
            try {
                content = readFile(file);
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                continue;
            }

            //Perform some String manipulation to remove PCL script.
            content = removePclScript(content);

            //Split the file contents up into individual payslips.
            String[] payslips = content.split(PAYSLIP_SEPARATOR);

            //Remove the last entry since it isn't valid.
            payslips = Arrays.copyOfRange(payslips, 0, payslips.length - 1);
            System.out.print("Extracting individual payslips... ");
            System.out.println(payslips.length + " payslips found.");

            //Convert the array of payslips into a large PDF of payslips.
            try {
                System.out.print("Generating PDF... ");
                PayslipGenerator pdfGenerator = new PayslipGenerator();
                pdfGenerator.generatePdf(payslips, file + ".pdf");
                System.out.println(file + ".pdf");
                System.out.println("OPERATION SUCCESSFULLY COMPLETED");
                System.out.println("");
            } catch (Exception e) {
                System.out.println("ERROR... Error generating PDF.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a new PDF document and add the supplied payslips to it.
     *
     * @param payslips The payslips to add to the new PDF.
     * @param pdfName The name to use for the new PDF file.
     * @throws IOException
     * @throws DocumentException
     */
    private void generatePdf(String[] payslips, String pdfName) throws IOException, DocumentException {
        //Configure the background image.
        Image payslipBackground = Image.getInstance(BACKGROUND_IMAGE);
        payslipBackground.setAlignment(Image.UNDERLYING);
        payslipBackground.setAbsolutePosition(0, 0);
        payslipBackground.scaleAbsolute(595, 842); //A4 page size at 72 DPI

        //Create and configure a new PDF document.
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfName));
        document.addTitle("PayslipGenerator");
        document.addKeywords("payslips");
        document.addCreator("NGA Dublin PCL to PDF converter");
        document.addAuthor("NorthgateArinso");
        document.open();

        //Add each payslip to the document.
        for (String payslip : payslips) {
            document.add(payslipBackground);

            //Add the entire payslip to an iText Paragraph and add that to the document.
            Paragraph paragraph = new Paragraph(10.2f, "\n\n\n\n" + payslip, PAYSLIP_FONT);
            paragraph.setIndentationLeft(55);
            document.add(paragraph);
            document.newPage();
        }
        document.close();
    }

    /**
     * Read in the specified file and return the contents.
     *
     * @param file The file to read
     * @return A <code>String</code> containing the contents of the file.
     * @throws IOException if there is an error reading the file.
     */
    private static String readFile(String file) throws IOException {
        BufferedReader inputStream = null;
        StringBuffer fileContents = new StringBuffer();

        try {
            inputStream = new BufferedReader(new FileReader(file));

            String line;
            while ((line = inputStream.readLine()) != null) {
                fileContents.append(line);
                fileContents.append("\n");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return fileContents.toString();
    }

    /**
     * Removes certain PCL script from the supplied text.
     *
     * @param content The text containing PCL script.
     * @return the sanitised text.
     */
    private static String removePclScript(String content) {
        content = content.replaceAll("H.RATE=........", "               ");
        content = content.substring(content.indexOf("\n1") + 3, content.length()); //Removes junk at the start of the file.
        content = "\n" + content;
        return content;
    }

    /**
     * Display some version information about the application.
     */
    private static void displayVersionInfo() {
        System.out.println("NGA Dublin PCL to PDF converter, v1.0");
        System.out.println("By Paul Hitz");
    }

    /**
     * Display usage information.
     */
    private static void displayHelp() {
        System.out.println("Enables the conversion of PCL formatted documents to PDF documents.");
        System.out.println("Usage: java -jar PayslipGenerator.jar <PCL_FILE>");
        System.out.println("example: java -jar PayslipGenerator.jar c:\\PSEAL075.001");
    }
}
