package com.eaiman.multipart.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

public interface FileUploadService {
    Flux<String> getLines(Flux<FilePart> filePartFlux);
}
