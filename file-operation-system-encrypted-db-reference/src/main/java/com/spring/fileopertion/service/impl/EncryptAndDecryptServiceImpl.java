package com.spring.fileopertion.service.impl;

import com.spring.fileopertion.config.ApplicationProperties;
import com.spring.fileopertion.service.EncryptAndDecryptService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

@Service
@AllArgsConstructor
public class EncryptAndDecryptServiceImpl implements EncryptAndDecryptService {
    private static final Logger LOG = LoggerFactory.getLogger(EncryptAndDecryptServiceImpl.class);
    private final ApplicationProperties applicationProperties;

    @Override
    public void fileProcessor(int cipherMode, File inputFile, File outputFile) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            Key secretKey = new SecretKeySpec(this.applicationProperties.getSecretKey().getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException e) {
            LOG.error("Error in file processing {}", e.getMessage());
        }finally {
            try {
                if(inputStream != null)
                    inputStream.close();
                if(outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                LOG.error("Error in stream closing {}", e.getMessage());
            }
        }
    }
}
