package ua.com.valexa.oc.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.valexa.oc.model.OcUserProfile;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
public class OcAccountService {

    private static final Logger logger = LoggerFactory.getLogger(OcAccountService.class);

    @Value("${oc.accounts.file}")
    private String accountFilePath;

    private static List<OcUserProfile> userProfiles = new ArrayList<>();

    private static final Random random = new Random();

    @PostConstruct
    private void initAccounts(){
        logger.info("Reading accounts file: {}", accountFilePath);

        try {
            userProfiles = readCsvToBean(accountFilePath);
            logger.info("Readed {} profiles", userProfiles.size());
        } catch (IOException e) {
            logger.error("Error reading CSV file: {}; Error: {}", userProfiles, e.getMessage());
        }
    }

    private List<OcUserProfile> readCsvToBean(String filePath) throws IOException {
        List<OcUserProfile> profiles = new ArrayList<>();

        try (Reader reader = new FileReader(filePath)) {
            CsvToBean<OcUserProfile> csvToBean = new CsvToBeanBuilder<OcUserProfile>(reader)
                    .withType(OcUserProfile.class)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            csvToBean.iterator().forEachRemaining(profile -> {
                try {
                    profiles.add(profile);
                } catch (Exception e) {
                    logger.warn("Skipping row due to field mismatch: {}", e.getMessage());
                }
            });
        }

        return profiles;
    }

    public static OcUserProfile getRandomProfile(){
        return userProfiles.get(random.nextInt(userProfiles.size()));
    }

}
