package com.example.googlesheets.controllers;

import com.example.googlesheets.services.GoogleSheetService;
import com.example.googlesheets.services.RequestClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    private final Sheets sheetsService;

    private final GoogleSheetService service;

    private final RequestClientService reqService;

    @Value("${google-sheets.spreadsheet-id}")
    private String spreadsheetId;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/read")
    public List<List<Object>> readSheetData() throws IOException {
        final String range = "Лист1!A1:D3";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        }

        return values;
    }

    /*@GetMapping("/get")
    public Object get() throws JsonProcessingException {
        return reqService.getInfoFromKeitaro();
    }*/

    private final RequestClientService requestClientService;
    @SneakyThrows
    @GetMapping("initDate")
    public void init(@RequestParam(name = "date")String date){
        requestClientService.setData(date);
    }

    @SneakyThrows
    @GetMapping("initTableFrom7")
    public void initFrom(){
        for(Integer i = 7; i < 15; i++){
            requestClientService.setData("2024-03-" + (i.toString().length() == 1 ? "0" + i : i));
        }
    }

    @GetMapping("/get1")
    public Object get1() throws JsonProcessingException {
        return reqService.getInfoFromMultiLogin("Falcon_ENG2", "ITK_solakesao", "0993F1725D6EF787A326EBECF77DF499F4E95D93826DF7A32A340BFAA3C9F28DCED5B52583AD9138", "2024-01-16", null);
    }

    @GetMapping("/get2")
    public Object get2() throws IOException {
        return service.readGoogleSheetsMultilogin("Multilogin");
    }

    @PostMapping("/write")
    public void writeCellValue(@RequestParam String sheet, @RequestParam String cell, @RequestParam String value) throws IOException {
        service.writeCell(sheet, cell, value);
    }

    @PostMapping("/test")
    public void test() throws Exception {
        LocalDateTime currentDate = LocalDateTime.now();

        // Приводим к нужному формату
        String formattedDate = currentDate.format(formatter);

        reqService.setData(formattedDate);
    }

    @PostMapping("/createSheet")
    public void createSheet(@RequestParam String sheetTitle) throws IOException {
        service.createSheet(sheetTitle);
    }

    @PostMapping("/writeDataToRange")
    public void writeDataToRange() throws IOException {

        LocalDate currentDate = LocalDate.now();

        // Приводим к нужному формату
        String formattedDate = currentDate.format(formatter);
        service.createSheet(formattedDate);
        service.createTable(formattedDate);
    }
}
