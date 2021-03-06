package org.innovateuk.ifs.threads.attachments.service;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.service.FileAndContents;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface AttachmentsService<T> {
    ServiceResult<T> findOne(Long attachmentId);

    ServiceResult<T> upload(String contentType, String contentLength, String originalFilename, Long contextId,
                            HttpServletRequest request);

    ServiceResult<Void> delete(Long attachmentId);

    ServiceResult<FileAndContents> attachmentFileAndContents(Long attachmentId);
}