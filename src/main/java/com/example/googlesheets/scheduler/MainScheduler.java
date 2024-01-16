package com.example.googlesheets.scheduler;

import com.example.googlesheets.services.GoogleSheetService;
import com.example.googlesheets.services.RequestClientService;
import lombok.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
public class MainScheduler {

    private final GoogleSheetService googleSheetService;
    private final RequestClientService requestClientService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Scheduled(fixedRate = 300000) // 300000 миллисекунд = 5 минут
    public void mainMethod() throws Exception {
        LocalDateTime currentDate = LocalDateTime.now();

        // Приводим к нужному формату
        String formattedDate = currentDate.format(formatter);

        requestClientService.setData(formattedDate);
    }
}
