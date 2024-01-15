package com.example.googlesheets.services;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@Service
@RequiredArgsConstructor
public class GoogleSheetService {

    private final Sheets sheetsService;

    @Value("${google-sheets.spreadsheet-id}")
    private String spreadsheetId;

    public void writeData(String sheet, String cell, String value) throws IOException{
        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(Collections.singletonList(value)));

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, sheet + "!" + cell, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    public void createSheet(String title) throws IOException{
        List<Sheet> sheets = sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets();

        // Создаем объект SheetProperties с указанием заголовка нового листа
        SheetProperties sheetProperties = new SheetProperties().setTitle(title);

        // Создаем объект AddSheetRequest с указанием параметров нового листа
        AddSheetRequest addSheetRequest = new AddSheetRequest().setProperties(sheetProperties);

        // Создаем объект Request с указанием типа запроса "addSheet"
        Request request = new Request().setAddSheet(addSheetRequest);

        // Устанавливаем порядковый номер нового листа в 0 (первый лист)
        sheetProperties.setIndex(0);

        // Создаем объект BatchUpdateSpreadsheetRequest с добавлением запроса
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(request));

        // Выполняем запрос на добавление нового листа
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
    }

    public void createTable() throws IOException {
        // Текст для записи в ячейки
        String token = "Token (ENG)";
        String account = "Account";
        String campaignName = "Campaign name (Eng)";
        String creativeId = "Creative ID (both)";
        String spent = "Spent (Eng)";
        String lpClicks = "LP clicks (K)";
        String lpClickCost = "LP click cost + fee";
        String leads = "Leads (K)";
        String leadCost = "Lead cost + fee";
        String maxLeadCost = "Max lead cost";

        String sheet = "Лист1";

        // Создаем список значений для записи
        List<List<Object>> values = Collections.singletonList(Arrays.asList(
                token, account, campaignName, creativeId, spent, lpClicks, lpClickCost, leads, leadCost, "", "","","", maxLeadCost
                ));

        // Определяем диапазон ячеек, куда будем записывать данные (A1:I1)
        String range = sheet + "!A1:N1";

        // Создаем объект BatchUpdateValuesRequest с указанием диапазона и значений
        BatchUpdateValuesRequest batchUpdateRequest = new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(Collections.singletonList(new ValueRange().setRange(range).setValues(values)));

        // Выполняем запрос на запись данных в ячейки
        sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, batchUpdateRequest).execute();

        // Определяем стиль для заливки ячеек желтым цветом
        CellFormat yellowCellFormat = new CellFormat()
                .setBackgroundColor(new Color().setRed(1.0f).setGreen(1.0f).setBlue(0.0f))
                .setBorders(new Borders()
                        .setBottom(new Border().setStyle("SOLID").setWidth(1))
                        .setTop(new Border().setStyle("SOLID").setWidth(1))
                        .setLeft(new Border().setStyle("SOLID").setWidth(1))
                        .setRight(new Border().setStyle("SOLID").setWidth(1))
                );

        // Определяем GridRange для указания диапазона ячеек
        GridRange gridRange = new GridRange()
                .setSheetId(0)  // Идентификатор листа
                .setStartRowIndex(0)
                .setEndRowIndex(1)
                .setStartColumnIndex(0)
                .setEndColumnIndex(9);

        // Создаем объект RepeatCellRequest с указанием стиля и диапазона
        RepeatCellRequest repeatCellRequest = new RepeatCellRequest()
                .setCell(new CellData().setUserEnteredFormat(yellowCellFormat))
                .setFields("userEnteredFormat.backgroundColor,userEnteredFormat.borders")
                .setRange(gridRange);

        // Создаем объект Request с указанием типа запроса "repeatCell"
        Request request = new Request().setRepeatCell(repeatCellRequest);

        // Создаем объект BatchUpdateSpreadsheetRequest с добавлением запроса на выделение ячеек
        BatchUpdateSpreadsheetRequest updateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(request));

        // Выполняем запрос на выделение ячеек
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, updateRequest).execute();
    }
}
