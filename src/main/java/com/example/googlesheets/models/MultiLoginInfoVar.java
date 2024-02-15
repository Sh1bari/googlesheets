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

    public MultiLoginInfoVar(String account, String token, String creativeId, String spent) {
        this.account = account;
        this.token = token;
        this.creativeId = creativeId;
        this.spent = spent;
    }
}
