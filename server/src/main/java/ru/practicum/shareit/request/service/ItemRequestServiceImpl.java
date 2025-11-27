package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        log.debug("Creating request for user {}", userId);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.description());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        request = requestRepository.save(request);
        log.info("Request created with id: {}", request.getId());

        return ItemRequestMapper.toItemRequestDto(request, List.of());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.debug("Getting requests for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> requests = requestRepository.findByRequestorId(userId, sort);

        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.debug("Getting all requests except for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> requests = requestRepository.findByRequestorIdNot(userId, sort);

        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.debug("Getting request {} by user {}", requestId, userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " not found"));

        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> requestId.equals(item.getRequest()))
                .toList();

        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private List<ItemRequestDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> allItems = itemRepository.findAll();
        Map<Long, List<Item>> itemsByRequest = allItems.stream()
                .filter(item -> item.getRequest() != null && requestIds.contains(item.getRequest()))
                .collect(Collectors.groupingBy(Item::getRequest));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())))
                .toList();
    }
}