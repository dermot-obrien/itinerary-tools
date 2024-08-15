package org.dobrien;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CreateLogseqMarkup {

    private static Logger logger = LoggerFactory.getLogger(DayIO.class);

    public void create(String folder) {
        int rowNo  = 1;
        int colNo  = 0;
        try {
            String filename = folder + "Himalayas 2024.xlsx";
            String contentFolder = folder + "pages\\";
            String itineraryFolder = contentFolder + "itinerary\\";
            new File(itineraryFolder).mkdirs();
            FileInputStream file = new FileInputStream(new File(filename));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet itinerarySheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            String itineraryFilename = contentFolder+"Itinerary.md";
            PrintWriter itineraryPageWriter = new PrintWriter(new FileWriter(itineraryFilename));

            int dayColNo = 0;
            int stopColNo = 1;
            int dateColNo = 2;
            int itineraryColNo = 3;
            int destinationColNo = 3;

            int lastDay = -1;
            for ( ; rowNo <= itinerarySheet.getLastRowNum(); rowNo++) {
                colNo = dateColNo;
                Row row = itinerarySheet.getRow(rowNo);
                Cell cell = row.getCell(colNo);
                evaluator.evaluate(cell);
                Date date = cell.getDateCellValue();
                if (date == null) continue;

                colNo = dayColNo;
                cell = row.getCell(colNo);
                evaluator.evaluate(cell);
                int day = (int)cell.getNumericCellValue();

                colNo = itineraryColNo;
                String itinerary = row.getCell(colNo).getStringCellValue();
                String daySummary = itinerary;
                itineraryPageWriter.println(String.format("[Day %02d](itinerary/Day %02d.md) - %s",day,day,daySummary));

                DateFormat format = new SimpleDateFormat("yyyy_MM_dd");
                String journalFilename = itineraryFolder+String.format("Day %02d.md",day);
                System.out.println(journalFilename);

                PrintWriter writer = new PrintWriter(new FileWriter(journalFilename));
                DateFormat titleFormat = new SimpleDateFormat("MMM. d, yyyy");
                writer.println("# "+titleFormat.format(date));
                writer.println("**"+itinerary+"**");
                writer.flush();
                writer.close();
            }
            itineraryPageWriter.flush();
            itineraryPageWriter.close();
        }
        catch(Exception e) {
            System.out.println(e);
        }

    }

    public static void main(String[] args) {
        String folder = "C:\\Users\\dermot.obrien\\OneDrive\\Interests & Having Fun\\Travel & Adventure\\2024-09 Himalayas\\";
        String excelFilename = folder + "Himalayas 2024.xlsx";
        String contentFolder = folder + "pages\\";
        String markdownFolder = contentFolder + "itinerary\\";
        new CreateLogseqMarkup().process(excelFilename,markdownFolder);
    }

    public void process(String excelFilename, String markdownFolder) {
        List<Day> days = new DayIO().readDaysFromExcel(excelFilename);
        DayIO.get().readDaysFromMarkdown(days,markdownFolder);
        DayIO.get().writeDaysToMarkdown(days,markdownFolder);
        logger.info(String.format("%d days",days.size()));
        for (Day day : days) {
            logger.info(String.format("Day %02d: %s",day.getDayNo(),day.fromTo()));
            if (day.getStopovers().size() > 1) {
                for (Stopover stopover : day.getStopovers()) {
                    logger.info(String.format("   %s", stopover.fromTo()));
                }
            }
            logger.info(String.format("   Accommodation: %s",day.accommodation()));
            logger.info(String.format("   Ad Hoc: %s",day.getAdHoc()));
        }
    }
}