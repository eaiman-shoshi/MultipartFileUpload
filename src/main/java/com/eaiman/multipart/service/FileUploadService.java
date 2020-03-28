package com.eaiman.multipart.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileUploadService {
    // this is for multiple file upload
    Flux<String> getLines(Flux<FilePart> filePartFlux);

    // this is for single file upload.
    Flux<String> getLines(Mono<FilePart> filePartMono);

    // this is for single file upload.
    Flux<String> getLines(FilePart filePart);
}
