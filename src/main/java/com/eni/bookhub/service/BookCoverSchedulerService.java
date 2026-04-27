package com.eni.bookhub.service;

import com.eni.bookhub.entity.Book;
import com.eni.bookhub.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BookCoverSchedulerService {

    private final BookRepository bookRepository;
    private final RestClient restClient;

    public BookCoverSchedulerService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        this.restClient = RestClient.create();
    }

    @Scheduled(fixedDelay = 30000)
    public void fetchMissingCovers() {
        List<Book> books = bookRepository.findByUrlCouvertureIsNull();
        if (books.isEmpty()) {
            return;
        }
        log.info("Recherche de couvertures pour {} livre(s) sans image", books.size());
        for (Book book : books) {
            try {
                String coverUrl = findCoverUrl(book);
                if (coverUrl != null) {
                    bookRepository.updateCoverUrl(book.getId(), coverUrl);
                    log.info("Couverture trouvée pour '{}': {}", book.getTitre(), coverUrl);
                } else {
                    log.debug("Aucune couverture trouvée pour '{}' (ISBN: {})", book.getTitre(), book.getIsbn());
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Erreur lors de la récupération de la couverture pour '{}': {}", book.getTitre(),
                        e.getMessage());
            }
        }
    }

    private String findCoverUrl(Book book) {
        String url = findCoverByIsbn(book.getIsbn());
        return url;
    }

    private String findCoverByIsbn(String isbn) {
        URI uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("openlibrary.org").path("/search.json")
                .queryParam("isbn", isbn)
                .queryParam("fields", "cover_i")
                .queryParam("limit", 1)
                .build().toUri();
        return fetchCoverUrl(uri);
    }

    private String fetchCoverUrl(URI uri) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return extractCoverUrl(response);
        } catch (Exception e) {
            log.debug("Requête OpenLibrary échouée: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractCoverUrl(Map<String, Object> response) {
        if (response == null)
            return null;
        List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");
        if (docs == null || docs.isEmpty())
            return null;
        Object coverId = docs.get(0).get("cover_i");
        if (coverId == null)
            return null;
        return "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
    }
}
