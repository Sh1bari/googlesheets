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
public class MultiLoginInfoVar {
    private String account;
    private String token;
    private String spent;
    private String creativeId;

    public MultiLoginInfoVar(String account, String token, String creativeId) {
        this.account = account;
        this.token = token;
        this.creativeId = creativeId;
    }
}
