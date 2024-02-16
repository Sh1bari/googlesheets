package com.example.googlesheets.services;

import com.example.googlesheets.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@Component
@RequiredArgsConstructor
public class RequestClientService {

    private final RestTemplate restTemplate;

    private final GoogleSheetService googleSheetService;
    private final HashMap<String, KeitaroInfo> keitaroInfo = new HashMap<>();  //ключ - creative_id
    private final HashMap<String, MultiLoginInfoVar> multiLoginInfoVar = new HashMap<>(); //ключ - creative_id
    private final HashMap<String, MultiLoginInfo> multiLoginInfo = new HashMap<>(); //ключ - creative_id
    private final List<Result> result = new ArrayList<>();
    private final String KEITARO_BODY = "{\n" +
            "    \"range\": {\n" +
            "        \"interval\": \"today\",\n" +
            "        \"timezone\": \"Europe/Amsterdam\"\n" +
            "    },\n" +
            "    \"columns\": [],\n" +
            "    \"metrics\": [\n" +
            "        \"lp_clicks\",\n" +
            "        \"leads\"\n" +
            "    ],\n" +
            "    \"grouping\": [\n" +
            "        \"campaign\",\n" +
            "        \"creative_id\"\n" +
            "    ],\n" +
            "    \"filters\": [\n" +
            "        {\n" +
            "            \"name\": \"creative_id\",\n" +
            "            \"operator\": \"NOT_MATCH_REGEXP\",\n" +
            "            \"expression\": \"\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"sort\": [\n" +
            "        {\n" +
            "            \"name\": \"creative_id\",\n" +
            "            \"order\": \"desc\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"summary\": true,\n" +
            "    \"limit\": 1000,\n" +
            "    \"offset\": 0\n" +
            "}";
    @Value("${api-key.keitaro}")
    private String KEITARO_TOKEN;

    private static String encodeBase64(String value) {
        return java.util.Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public void clearLocalData() {
        keitaroInfo.clear();
        multiLoginInfoVar.clear();
        multiLoginInfo.clear();
        result.clear();
    }

    public String getInfoFromKeitaro() throws JsonProcessingException {
        ResponseEntity<String> json = postRequest("http://45.136.50.92/admin_api/v1/report/build", KEITARO_TOKEN, KEITARO_BODY, "", "");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json.getBody());

        // Извлекаем только массив "rows"
        JsonNode rowsNode = jsonNode.get("rows");
        List<KeitaroRow> list = List.of(objectMapper.treeToValue(rowsNode, KeitaroRow[].class));
        list.stream().filter(o -> !o.getCreativeId().equals(""))
                .forEach(o -> {
                    keitaroInfo.put(o.getCreativeId(), new KeitaroInfo(o.getCampaign(), String.valueOf(o.getLpClicks()), String.valueOf(o.getLeads())));
                });
        return keitaroInfo.toString();
    }

    //String name, String token
    public String getInfoFromMultiLogin(String account, String credentialsAccount, String credentialsToken, String time) throws JsonProcessingException {
        String body = MULTILOGIN_BODY(time);
        ResponseEntity<String> responseEntity = postRequest("https://platform.engageya.com/dashboard-api/api/reports/v1/reports/getCampaignPostsStatisticsForLast30Days",
                KEITARO_TOKEN,
                body,
                credentialsAccount,
                credentialsToken);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());

        // Извлекаем только массив "rows"
        JsonNode rowsNode = jsonNode.get("data").get("tableData");

        //Date, Campaign ID, Campaign Name, Creative ID, Creative Name, Creative Image Thumbnail URL, Views, Clicks, Spent
        List<List<String>> list = objectMapper.readValue(
                rowsNode.toString(),
                new TypeReference<List<List<String>>>() {
                }
        );

        list.forEach(o -> {
            multiLoginInfoVar.put(o.get(3), new MultiLoginInfoVar(credentialsAccount, credentialsToken, o.get(3), o.get(8), o.get(2)));
        });

        multiLoginInfoVar.forEach((k, v) -> {
                    MultiLoginInfoVar var = v;
                    if (keitaroInfo.get(var.getCreativeId()) != null) {
                        KeitaroInfo keitaroVar = keitaroInfo.get(var.getCreativeId());
                        multiLoginInfo.put(var.getCreativeId(), new MultiLoginInfo(var.getAccount(), var.getToken(), var.getSpent()));
                        double spent = Double.parseDouble(var.getSpent().substring(1).replace(",", "")) * 1.2;
                        DecimalFormat decimalFormat = new DecimalFormat("0.000");
                        String spentString = decimalFormat.format(spent);
                        String lpClickCostAndFee = !keitaroVar.getLpClicks().equals("0") ?
                                decimalFormat.format(spent / Double.parseDouble(keitaroVar.getLpClicks())) :
                                "Inf";

                        String leadsCostAndFee = !keitaroVar.getLeads().equals("0") ?
                                decimalFormat.format(spent / Double.parseDouble(keitaroVar.getLeads())) :
                                "Inf";
                        String maxLeadCost = "";

                        if (var.getCampaignName().contains("DE")) {
                            maxLeadCost = "165";
                        } else if (var.getCampaignName().contains("AT")) {
                            maxLeadCost = "160";
                        } else if (var.getCampaignName().contains("UK")) {
                            maxLeadCost = "100";
                        } else {
                            maxLeadCost = "Error";
                        }
                        String maxLpClickCost = "";
                        if (!maxLeadCost.equals("Error")) {
                            maxLpClickCost = String.format("%.2f", Double.parseDouble(maxLeadCost) * 0.08);
                        }


                        result.add(new Result(
                                var.getToken(),
                                account,
                                var.getCampaignName(),
                                var.getCreativeId(),
                                spentString, // spent
                                keitaroVar.getLpClicks(),
                                lpClickCostAndFee,
                                keitaroVar.getLeads(),
                                leadsCostAndFee,
                                maxLpClickCost,
                                maxLeadCost
                        ));
                    }
                }
        );

        return multiLoginInfo.toString();
    }

    public void setData(String time) throws Exception {
        googleSheetService.createSheet(time);
        List<MultiLoginAccounts> accounts = googleSheetService.readGoogleSheets("Multilogin");
        clearLocalData();
        getInfoFromKeitaro();
        accounts.forEach(o -> {
            try {
                getInfoFromMultiLogin(o.getAccount(), o.getCredentialsAccount(), o.getCredentialsToken(), time);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        googleSheetService.clearGoogleSheet(time);
        googleSheetService.writeDataFromList(time, result);
    }

    public ResponseEntity<String> postRequest(String apiUrl, String apiKey, String jsonData, String credentialsAccount, String credentialsToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-key", apiKey);
        headers.set("Connection", "keep-alive");
        headers.set("Authorization", "Basic " + encodeBase64(credentialsAccount + ":" + credentialsToken));

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

        try {
            // Используйте exchange, чтобы получить ResponseEntity<String>
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
            return responseEntity;
        } catch (HttpClientErrorException e) {
            // Поймаем исключение HttpClientErrorException и вернем его информацию
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }

    private String MULTILOGIN_BODY(String time) {
        return "{\n" +
                " \"queryParams\": {\n" +
                "  \"Page\": 1,\n" +
                "  \"ResultsPerPage\": 500,\n" +
                "  \"StartDate\": \"" + time + "\",\n" +
                "  \"EndDate\": \"" + time + "\"\n" +
                " },\n" +
                " \"requirePublisherReport\": false\n" +
                "}";
    }
}