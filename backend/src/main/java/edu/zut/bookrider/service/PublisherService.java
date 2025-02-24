package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.PublisherRequestDto;
import edu.zut.bookrider.dto.PublisherResponseDto;
import edu.zut.bookrider.exception.PublisherNotFoundException;
import edu.zut.bookrider.mapper.publisher.PublisherReadMapper;
import edu.zut.bookrider.model.Publisher;
import edu.zut.bookrider.repository.BookRepository;
import edu.zut.bookrider.repository.PublisherRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherReadMapper publisherReadMapper;
    private final BookRepository bookRepository;

    @Transactional
    public PublisherResponseDto addPublisher(@Valid PublisherRequestDto publisherRequestDto) {
        if (publisherRepository.existsByName(publisherRequestDto.getName())) {
            throw new IllegalStateException("Publisher with the provided name already exists");
        }

        Publisher publisher = Publisher.builder()
                .name(publisherRequestDto.getName())
                .build();

        Publisher savedPublisher = publisherRepository.save(publisher);
        return publisherReadMapper.map(savedPublisher);
    }

    @Transactional(readOnly = true)
    public List<PublisherResponseDto> getAllPublishers() {
        return publisherRepository.findAll().stream()
                .map(publisherReadMapper::map)
                .toList();
    }

    public PublisherResponseDto findPublisherById(Integer publisherId) {
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new PublisherNotFoundException("Publisher with the provided id " + publisherId + " not found"));
        return publisherReadMapper.map(publisher);
    }

    @Transactional
    public PublisherResponseDto updatePublisher(Integer publisherId, @Valid PublisherRequestDto publisherRequestDto) {
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new PublisherNotFoundException("Publisher with the provided id: " + publisherId + " not found"));

        publisher.setName(publisherRequestDto.getName());

        Publisher updatedPublisher = publisherRepository.save(publisher);

        return publisherReadMapper.map(updatedPublisher);
    }

    @Transactional
    public void deletePublisher(Integer publisherId) {
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new PublisherNotFoundException("Publisher with the provided id: " + publisherId + " not found"));

        publisher.getBooks().forEach(book -> book.setPublisher(null));
        bookRepository.saveAll(publisher.getBooks());

        publisher.getBooks().stream()
                .filter(book -> book.getPublisher() == null)
                .forEach(bookRepository::delete);

        publisherRepository.delete(publisher);
    }
}
