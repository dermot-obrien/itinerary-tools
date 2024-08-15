package org.dobrien;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DayIO {

    private static Logger logger = LoggerFactory.getLogger(DayIO.class);

    private static final DayIO instance = new DayIO();

    public static DayIO get() { return instance; }

    public List<Day> readDaysFromGoogleSheets(String excelFilename) {
        try {
            Map<Integer,Day> dayNoToDay = new HashMap<>();
            List<Day> days = new ArrayList<>();
            return days;
        }
        catch(Exception e) {
            logger.error("Unable to read",e);
            return null;
        }
    }

    public void writeDaysToMarkdown(List<Day> days, String folder) {
        try {
            for (Day day : days) {
                String markdownFilename = folder + String.format("Day %02d.md", day.getDayNo());
                File markdownFile = new File(markdownFilename);
                PrintWriter writer = new PrintWriter(markdownFile);
                DateFormat titleFormat = new SimpleDateFormat("MMM. d, yyyy");
                writer.println("# "+titleFormat.format(day.getDate()));
                writer.println("**"+day.fromTo()+"**");
                writer.println("**Accommodation:** "+day.accommodation());
                for (Stopover stopover : day.getStopovers()) {
                    writer.println(stopover.getNotes());
                }
                String adHoc = day.getAdHoc();
                if (adHoc != null) adHoc = adHoc.trim();
                if (adHoc != null && adHoc.length() > 0) {
                    writer.println(day.getAdHoc());
                }


                writer.flush();
                writer.close();
            }
        }
        catch(Exception e) {
            logger.error("Unable to write",e);
        }
    }

    public void readDaysFromMarkdown(List<Day> days, String folder) {
        try {
            String delimiter = "---";
            for (Day day : days) {
                String markdownFilename = folder+String.format("Day %02d.md",day.getDayNo());
                File markdownFile = new File(markdownFilename);
                if (markdownFile.exists()) {
                    LineNumberReader reader = new LineNumberReader(new FileReader(markdownFile));
                    StringBuilder builder = new StringBuilder();
                    String line = reader.readLine();
                    boolean isAdHoc = false;
                    while(line != null) {
                        if (line.startsWith(delimiter)) isAdHoc = true;
                        if (isAdHoc) {
                            builder.append(line);
                            builder.append(System.lineSeparator());
                        }
                        line = reader.readLine();
                    }
                    day.setAdHoc(builder.toString());
                    reader.close();
                }
            }
        }
        catch(Exception e) {
            logger.error("Unable to read.",e);
        }
    }

    public List<Day> readDaysFromExcel(String excelFilename) {
        try {
            Map<Integer,Day> dayNoToDay = new HashMap<>();
            List<Day> days = new ArrayList<>();
            FileInputStream file = new FileInputStream(new File(excelFilename));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet itinerarySheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // Day	Stop	Date	Itinerary	Tour Day	China Visa	Type	Flight Booked	Destination	Accommodation Booked	Accommodation	Actions	Notes	Meals	Latitude	Longitude	Elevation (m)	Elevation Change (m)	Elevation (ft)
            int dayColNo = 0;
            int stopColNo = 1;
            int dateColNo = 2;
            int itineraryColNo = 3;
            int destinationColNo = 8;
            int notesColNo = 12;
            int accommodationColNo = 10;

            int lastDay = -1;
            String startAt = null;
            for (int rowNo=1 ; rowNo <= itinerarySheet.getLastRowNum(); rowNo++) {
                Row row = itinerarySheet.getRow(rowNo);
                Cell cell = row.getCell(dayColNo);
                evaluator.evaluate(cell);
                if (cell.getCellType() == CellType.BLANK) continue;
                int dayNo = (int)cell.getNumericCellValue();
                Day day = dayNoToDay.get(dayNo);
                if (day == null) {
                    day = new Day();
                    days.add(day);
                    day.setDayNo(dayNo);
                    dayNoToDay.put(dayNo,day);
                }

                Stopover stopover = new Stopover();
                stopover.setDay(day);
                day.getStopovers().add(stopover);
                cell = row.getCell(dateColNo);
                evaluator.evaluate(cell);
                day.setDate(cell.getDateCellValue());

                stopover.setStopNo((int)row.getCell(stopColNo).getNumericCellValue());
                stopover.setItinerary(row.getCell(itineraryColNo).getStringCellValue());
                stopover.setNotes(row.getCell(notesColNo).getStringCellValue());
                stopover.setAccommodation(row.getCell(accommodationColNo).getStringCellValue());
                stopover.setStartFrom(startAt);
                stopover.setEndAt(row.getCell(destinationColNo).getStringCellValue());
                startAt = stopover.getEndAt();
            }
            file.close();
            return days;
        }
        catch(Exception e) {
            logger.error("Unable to read",e);
            return null;
        }
    }

    public static void main(String[] args) {
        String folder = "C:\\Users\\dermot.obrien\\OneDrive\\Interests & Having Fun\\Travel & Adventure\\2024-09 Himalayas\\";
        String excelFilename = folder+"Himalayas 2024.xlsx";
        List<Day> days = DayIO.get().readDaysFromExcel(excelFilename);
        logger.info(String.format("%d days",days.size()));
        for (Day day : days) {
            logger.info(String.format("Day %02d: %s",day.getDayNo(),day.fromTo()));
            if (day.getStopovers().size() > 1) {
                for (Stopover stopover : day.getStopovers()) {
                    logger.info(String.format("   %s", stopover.fromTo()));
                }
            }
            logger.info(String.format("   Accommodation: %s",day.accommodation()));
        }
    }
}