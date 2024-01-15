package com.example.googlesheets.controllers;

import com.example.googlesheets.services.GoogleSheetService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @Value("${google-sheets.spreadsheet-id}")
    private String spreadsheetId;

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

    @PostMapping("/write")
    public void writeCellValue(@RequestParam String sheet, @RequestParam String cell, @RequestParam String value) throws IOException {
        service.writeData(sheet, cell, value);
    }

    @PostMapping("/createSheet")
    public void createSheet(@RequestParam String sheetTitle) throws IOException {
        service.createSheet(sheetTitle);
    }

    @PostMapping("/writeDataToRange")
    public void writeDataToRange() throws IOException {
        service.createTable();
    }
}
