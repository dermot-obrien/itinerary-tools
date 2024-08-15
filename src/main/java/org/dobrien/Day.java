package org.dobrien;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Day {

    private int dayNo;
    private List<Stopover> stopovers = new ArrayList<>();
    private Date date;
    private String keyInformation;
    private String adHoc;

    public String fromTo() {
        if (stopovers == null || stopovers.size() == 0) return "";
        if (stopovers.size() == 1) return stopovers.get(0).fromTo();
        String text = stopovers.get(0).getStartFrom();
        if (text == null) text = "";
        text = text + " - " + stopovers.get(stopovers.size()-1).getEndAt();
        return text;
    }

    public String accommodation() {
        return stopovers.get(stopovers.size()-1).getAccommodation();
    }
}
