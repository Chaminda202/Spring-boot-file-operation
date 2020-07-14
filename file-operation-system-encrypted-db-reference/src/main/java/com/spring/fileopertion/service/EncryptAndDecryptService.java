package com.spring.fileopertion.service;

import java.io.File;

public interface EncryptAndDecryptService {
    void fileProcessor(int cipherMode, File inputFile, File outputFile);
}
