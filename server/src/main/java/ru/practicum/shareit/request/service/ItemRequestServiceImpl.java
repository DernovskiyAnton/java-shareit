package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        log.debug("Creating request for user {}", userId);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        request = itemRequestRepository.save(request);
        log.info("Request created with id: {}", request.getId());

        return ItemRequestMapper.toItemRequestDto(request, itemRepository.findByRequest(request.getId()));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.debug("Getting requests for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(userId, sort);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemRepository.findByRequest(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.debug("Getting all requests except user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNot(userId, sort);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemRepository.findByRequest(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.debug("Getting request {} by user {}", requestId, userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " not found"));

        return ItemRequestMapper.toItemRequestDto(request, itemRepository.findByRequest(requestId));
    }
}