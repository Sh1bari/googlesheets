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
public class MultiLoginAccounts {
    private String account;
    private String credentialsAccount;
    private String credentialsToken;
}
