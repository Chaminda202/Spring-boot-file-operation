package com.spring.fileopertion.validator;

import com.spring.fileopertion.config.ApplicationProperties;
import com.spring.fileopertion.util.JacksonUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class FileExtensionValidator {
	private static final Logger LOG = LoggerFactory.getLogger(FileExtensionValidator.class);
	private final ApplicationProperties applicationProperties;
	
	public boolean validateFileExtension(MultipartFile file) {
		String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

		LOG.info("File extension {}", extension);
		List<String> extensionList = Arrays.stream(
				applicationProperties.getAllowedExtensions()
						.split(","))
				.map(String::trim)
				.collect(Collectors.toList());
		return extensionList.stream().anyMatch(x -> x.equalsIgnoreCase(extension));
	}

	public boolean validateFileExtensions(MultipartFile[] files) {

		Set<String> filesExtensionSet = Arrays.stream(files)
				.map(file -> {
					String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
					return StringUtils.getFilenameExtension(originalFileName).toLowerCase();
				})
				.collect(Collectors.toSet());

		LOG.info("File extension {}", JacksonUtil.getToString(filesExtensionSet));
		List<String> predefineExtensionList = Arrays.stream(
				applicationProperties.getAllowedExtensions()
						.split(","))
				.map(ex -> ex.trim().toLowerCase())
				.collect(Collectors.toList());

		return predefineExtensionList.containsAll(filesExtensionSet);
	}
}
