package org.dobrien;

import lombok.Data;

@Data
public class Stopover {

    private Day day;
    private int stopNo;
    private String startFrom;
    private String endAt;
    private String itinerary;
    private String notes;
    private String accommodation;

    public String fromTo() {
        String text = startFrom == null || startFrom.equals(endAt) ? "" : startFrom +" - ";
        text = text + endAt;
        return text;
    }
}
