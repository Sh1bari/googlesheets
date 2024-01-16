package com.example.googlesheets.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@Data
@Builder
@NoArgsConstructor
public class MultiLoginInfo {
    private String account;
    private String token;
    private String spent;

    public MultiLoginInfo(String account, String token, String spent) {
        this.account = account;
        this.token = token;
        this.spent = spent;
    }
}
