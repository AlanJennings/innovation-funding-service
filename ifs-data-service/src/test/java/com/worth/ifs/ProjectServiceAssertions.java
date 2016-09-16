package com.worth.ifs;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.service.FileAndContents;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.file.domain.builders.FileEntryBuilder.newFileEntry;
import static com.worth.ifs.file.resource.builders.FileEntryResourceBuilder.newFileEntryResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Module: innovation-funding-service-dev
 **/
public class ProjectServiceAssertions extends BaseUnitTestMocksTest {

    public void assertGetFileContents(Consumer<FileEntry> fileSetter, Supplier<ServiceResult<FileAndContents>> getFileContentsFn) {

        FileEntry fileToGet = newFileEntry().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        FileEntryResource fileResourceToGet = newFileEntryResource().build();

        fileSetter.accept(fileToGet);
        when(fileServiceMock.getFileByFileEntryId(fileToGet.getId())).thenReturn(serviceSuccess(inputStreamSupplier));
        when(fileEntryMapperMock.mapToResource(fileToGet)).thenReturn(fileResourceToGet);

        ServiceResult<FileAndContents> result = getFileContentsFn.get();
        assertTrue(result.isSuccess());
        assertEquals(fileResourceToGet, result.getSuccessObject().getFileEntry());
        assertEquals(inputStreamSupplier, result.getSuccessObject().getContentsSupplier());
    }

    public void assertCreateFile(Supplier<FileEntry> fileGetter, BiFunction<FileEntryResource, Supplier<InputStream>, ServiceResult<FileEntryResource>> createFileFn) {

        FileEntryResource fileToCreate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        FileEntry createdFile = newFileEntry().build();
        FileEntryResource createdFileResource = newFileEntryResource().build();

        when(fileServiceMock.createFile(fileToCreate, inputStreamSupplier)).thenReturn(serviceSuccess(Pair.of(new File("blah"), createdFile)));
        when(fileEntryMapperMock.mapToResource(createdFile)).thenReturn(createdFileResource);

        ServiceResult<FileEntryResource> result = createFileFn.apply(fileToCreate, inputStreamSupplier);
        assertTrue(result.isSuccess());
        assertEquals(createdFileResource, result.getSuccessObject());
        assertEquals(createdFile, fileGetter.get());
    }

    public void assertGetFileDetails(Consumer<FileEntry> fileSetter, Supplier<ServiceResult<FileEntryResource>> getFileDetailsFn) {
        FileEntry fileToGet = newFileEntry().build();

        FileEntryResource fileResourceToGet = newFileEntryResource().build();

        fileSetter.accept(fileToGet);
        when(fileEntryMapperMock.mapToResource(fileToGet)).thenReturn(fileResourceToGet);

        ServiceResult<FileEntryResource> result = getFileDetailsFn.get();
        assertTrue(result.isSuccess());
        assertEquals(fileResourceToGet, result.getSuccessObject());
    }
}
