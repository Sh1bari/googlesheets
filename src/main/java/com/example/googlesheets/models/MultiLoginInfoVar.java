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
public class MultiLoginInfoVar {
    private String account;
    private String token;
    private String spent;
    private String creativeId;
    private String campaignName;

    public MultiLoginInfoVar(String account, String token, String creativeId, String spent, String campaignName) {
        this.account = account;
        this.token = token;
        this.creativeId = creativeId;
        this.spent = spent;
        this.campaignName = campaignName;
    }
}
