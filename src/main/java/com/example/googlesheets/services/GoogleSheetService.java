package com.example.googlesheets.services;

import com.example.googlesheets.models.MultiLoginAccounts;
import com.example.googlesheets.models.Result;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

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

    public void writeCell(String sheet, String cell, String value) throws IOException {
        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(Collections.singletonList(value)));

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, sheet + "!" + cell, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    public void createSheet(String title) throws IOException {
        List<Sheet> sheets = sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets();

        // Создаем объект SheetProperties с указанием заголовка нового листа
        SheetProperties sheetProperties = new SheetProperties().setTitle(title);

        // Создаем объект AddSheetRequest с указанием параметров нового листа
        AddSheetRequest addSheetRequest = new AddSheetRequest().setProperties(sheetProperties);

        // Создаем объект Request с указанием типа запроса "addSheet"
        Request request = new Request().setAddSheet(addSheetRequest);

        // Устанавливаем порядковый номер нового листа в 0 (первый лист)
        sheetProperties.setIndex(2);

        // Создаем объект BatchUpdateSpreadsheetRequest с добавлением запроса
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(request));

        // Выполняем запрос на добавление нового листа
        try {
            sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
            createTable(title);
        } catch (Exception e) {

        }
    }

    public List<MultiLoginAccounts> readGoogleSheetsMultilogin(String sheetName) throws IOException {
        String range = sheetName + "!A:C"; // Предполагаем, что данные находятся в столбцах A, B, C

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        List<MultiLoginAccounts> accountsList = new ArrayList<>();

        if (values != null && !values.isEmpty()) {
            int counter = 0;
            for (List<Object> row : values) {
                counter++;
                if (counter > 1) {
                    MultiLoginAccounts account = new MultiLoginAccounts();
                    account.setAccount(getStringValue(row.get(0)));
                    account.setCredentialsAccount(getStringValue(row.get(1)));
                    account.setCredentialsToken(getStringValue(row.get(2)));
                    accountsList.add(account);
                }
            }
        }

        return accountsList;
    }

    public Map<String, String> readGoogleSheetsMaxLeadCost(String sheetName) throws IOException {
        String range = sheetName + "!A:B"; // Предполагаем, что данные находятся в столбцах A, B

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        Map<String, String> costs = new HashMap<>();

        if (values != null && !values.isEmpty()) {
            int counter = 0;
            for (List<Object> row : values) {
                counter++;
                if (counter > 1) {
                    costs.put(getStringValue(getStringValue(row.get(0))), getStringValue(row.get(1)));
                }
            }
        }

        return costs;
    }

    private String getStringValue(Object value) {
        return value != null ? value.toString() : "";
    }

    DecimalFormat df = new DecimalFormat("0.00%");
    DecimalFormat decF = new DecimalFormat("#0.00");

    public void writeDataFromList(String sheet, List<Result> dataList) throws IOException {
        int dataSize = dataList.size();
        List<List<Object>> dataToWrite = new ArrayList<>();
        for (Result data : dataList) {
            List<Object> line = new ArrayList<>();
            line.add(data.getToken());
            line.add(data.getAccount());
            line.add(data.getCampaignName());
            line.add(data.getCreativeID());
            line.add(data.getLpClicks());
            line.add(data.getLpClickCostAndFee());
            line.add(data.getLeads());
            line.add(data.getLeadCostAndFee());
            Double roiLpClicks = (Double.parseDouble(data.getMaxLpClickCost().replace(",", ".").replace("Inf", "99999999999")) -
                    Double.parseDouble(data.getLpClickCostAndFee().replace(",", ".").replace("Inf", "99999999999"))) /
                    Double.parseDouble(data.getLpClickCostAndFee().replace(",", ".").replace("Inf", "99999999999"));
            line.add(df.format(roiLpClicks).replace(".", ","));

            line.add("");

            line.add(data.getSpent());
            Double revenueLead = Double.parseDouble(data.getMaxLeadCost().replace(",", ".").replace("Inf", "99999999999")) *
                    Double.parseDouble(data.getLeads().replace(",", ".").replace("Inf", "99999999999"));
            line.add(revenueLead.toString());
            Double profitLead = revenueLead -
                    Double.parseDouble(data.getSpent().replace(",", ".").replace("Inf", "99999999999"));
            line.add(decF.format(profitLead));

            Double roiLead = (Double.parseDouble(data.getMaxLeadCost().replace(",", ".").replace("Inf", "99999999999")) -
                    Double.parseDouble(data.getLeadCostAndFee().replace(",", ".").replace("Inf", "99999999999"))) /
                    Double.parseDouble(data.getLeadCostAndFee().replace(",", ".").replace("Inf", "99999999999"));
            line.add(df.format(roiLead).replace(".", ","));

            line.add("");

            line.add(data.getMaxLpClickCost());
            line.add(data.getMaxLeadCost());
            dataToWrite.add(line);
        }

        String range = "A3:Q" + (dataSize + 2);
        writeRangeData(sheet, range, dataToWrite);
    }

    private void writeRangeData(String sheet, String range, List<List<Object>> values) throws IOException {

        ValueRange body = new ValueRange().setValues(values);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, sheet + "!" + range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }


    public void clearGoogleSheet(String sheet) throws Exception {
        // Создание запроса на очистку значений
        ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
        String range = sheet + "!A3:Z1000";

        // Выполнение запроса на очистку
        sheetsService.spreadsheets().values().clear(spreadsheetId, range, clearValuesRequest).execute();
    }

    public void alignTextRight(String sheet) throws Exception {
        // Создание объекта CellFormat с указанием выравнивания текста справа
        CellFormat rightAlignmentFormat = new CellFormat()
                .setHorizontalAlignment("RIGHT")
                .setTextFormat(new TextFormat().setFontSize(10)); // Дополнительные параметры форматирования текста

        // Определение диапазона ячеек
        String range = sheet + "!D2:Z1000";

        // Создание запроса на обновление ячеек с указанием формата
        RepeatCellRequest repeatCellRequest = new RepeatCellRequest()
                .setCell(new CellData().setUserEnteredFormat(rightAlignmentFormat))
                .setFields("userEnteredFormat.horizontalAlignment,userEnteredFormat.textFormat.fontSize")
                .setRange(new GridRange().setSheetId(getSheetIdByName(sheet)).setStartRowIndex(1).setEndRowIndex(1000).setStartColumnIndex(3).setEndColumnIndex(25));

        Request request = new Request().setRepeatCell(repeatCellRequest);
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(Collections.singletonList(request));

        // Выполнение запроса на обновление ячеек
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
    }



    private Integer getSheetIdByName(String sheetName) throws IOException {

        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();

        for (Sheet sheet : sheets) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                return sheet.getProperties().getSheetId();
            }
        }

        throw new IllegalArgumentException("Лист с именем " + sheetName + " не найден.");
    }

    public void createTable(String sheet) throws IOException {
        // Текст для записи в ячейки
        String token = "Token (ENG)";
        String account = "Account";
        String campaignName = "Campaign name (Eng)";
        String creativeId = "Creative ID (both)";
        String spent = "Spent + fee (Eng)";
        String lpClicks = "LP clicks (K)";
        String lpClickCost = "LP click cost + fee";
        String leads = "Leads (K)";
        String leadCost = "Lead cost + fee";
        String maxLpClickCost = "Max LP click cost";
        String maxLeadCost = "Max lead cost";
        String roiClick = "ROI (LP clicks)";
        String roiLead = "ROI (Lead)";
        String revenueLead = "Revenue (Lead)";
        String profitLead = "Profit (Lead)";

        // Создаем список значений для записи
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(
                        token,
                        account,
                        campaignName,
                        creativeId,
                        lpClicks,
                        lpClickCost,
                        leads,
                        leadCost,
                        roiClick,
                        "",
                        spent,
                        revenueLead,
                        profitLead,
                        roiLead,
                        "",
                        maxLpClickCost,
                        maxLeadCost
                ),
                Arrays.asList("", "", "", "", "", "", "", "", "", "Total:", "", "", "", "", "", "", "", "")
        );

        String range = sheet + "!A1:R2";

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

        String sheetName = sheet;
        Integer sheetId = getSheetIdByName(sheetName);
        // Определяем GridRange для указания диапазона ячеек
        GridRange gridRange = new GridRange()
                .setSheetId(sheetId)  // Идентификатор листа
                .setStartRowIndex(0)
                .setEndRowIndex(1)
                .setStartColumnIndex(0)
                .setEndColumnIndex(values.get(0).size());

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

        CellFormat orangeCellFormat = new CellFormat()
                .setBackgroundColor(new Color().setRed(1.0f).setGreen(0.5f).setBlue(0.0f)); // Используйте оранжевый цвет

        GridRange gridRangeOrange = new GridRange()
                .setSheetId(sheetId)  // Идентификатор листа
                .setStartRowIndex(1)   // Начинаем со второй строки
                .setEndRowIndex(2)     // Заканчиваем на третьей строке (не включая)
                .setStartColumnIndex(0)
                .setEndColumnIndex(values.get(0).size());

        RepeatCellRequest repeatCellRequestOrange = new RepeatCellRequest()
                .setCell(new CellData().setUserEnteredFormat(orangeCellFormat))
                .setFields("userEnteredFormat.backgroundColor")
                .setRange(gridRangeOrange);


        Request requestOrange = new Request().setRepeatCell(repeatCellRequestOrange);

        BatchUpdateSpreadsheetRequest updateRequestOrange = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(requestOrange));

        sheetsService.spreadsheets().batchUpdate(spreadsheetId, updateRequestOrange).execute();

        CellFormat orangeCellFormatBordered = new CellFormat()
                .setBorders(new Borders()
                        .setBottom(new Border().setStyle("SOLID").setWidth(2))
                        .setTop(new Border().setStyle("SOLID").setWidth(2))
                        .setLeft(new Border().setStyle("SOLID").setWidth(2))
                        .setRight(new Border().setStyle("SOLID").setWidth(2))
                );

        GridRange gridRangeOrangeBordered = new GridRange()
                .setSheetId(sheetId)  // Идентификатор листа
                .setStartRowIndex(1)   // Начинаем со второй строки
                .setEndRowIndex(2)     // Заканчиваем на третьей строке (не включая)
                .setStartColumnIndex(9)
                .setEndColumnIndex(14);

        RepeatCellRequest repeatCellRequestOrangeBordered = new RepeatCellRequest()
                .setCell(new CellData().setUserEnteredFormat(orangeCellFormatBordered))
                .setFields("userEnteredFormat.borders")
                .setRange(gridRangeOrangeBordered);

        Request requestOrangeBordered = new Request().setRepeatCell(repeatCellRequestOrangeBordered);

        BatchUpdateSpreadsheetRequest updateRequestOrangeBordered = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(requestOrangeBordered));

        sheetsService.spreadsheets().batchUpdate(spreadsheetId, updateRequestOrangeBordered).execute();

        String formulaRange = sheetName + "!K2:N2";

        List<Object> formulas = Arrays.asList(
                "=SUM(K3:K1000)",
                "=SUM(L3:L1000)",
                "=SUM(M3:M1000)",
                "=TEXT(SUM(ARRAYFORMULA(VALUE(SUBSTITUTE(SUBSTITUTE(SUBSTITUTE(N3:N1000; \",\"; \"\"); \"%\"; \"\");\"∞\";\"\"))))/10000; \"0.00%\")"
        );

        // Создаем объект ValueRange с тремя формулами
        ValueRange valueRange = new ValueRange()
                .setValues(Collections.singletonList(formulas));

        // Выполняем запрос на обновление ячеек
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, formulaRange, valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }
}
