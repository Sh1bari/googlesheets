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
public class KeitaroInfo {
    private String campaign;
    private String lpClicks;
    private String leads;

    public KeitaroInfo(String campaign, String lpClicks, String leads){
        this.campaign = campaign;
        this.lpClicks = lpClicks;
        this.leads = leads;
    }
}
