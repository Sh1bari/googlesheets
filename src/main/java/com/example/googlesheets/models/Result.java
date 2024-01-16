package com.example.googlesheets.models;

import lombok.*;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private String token;
    private String account;
    private String campaignName;
    private String creativeID;
    private String spent;
    private String lpClicks;
    private String lpClickCostAndFee;
    private String leads;
    private String leadCostAndFee;
    private String maxLpClickCost;
    private String maxLeadCost;

}
