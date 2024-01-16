package com.example.googlesheets.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class KeitaroRow {
    @JsonProperty("campaign")
    private String campaign;

    @JsonProperty("creative_id")
    private String creativeId;

    @JsonProperty("campaign_id")
    private int campaignId;

    @JsonProperty("lp_clicks")
    private int lpClicks;

    @JsonProperty("leads")
    private int leads;
}
