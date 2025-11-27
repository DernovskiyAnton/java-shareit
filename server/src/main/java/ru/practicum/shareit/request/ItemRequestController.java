package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    /**
     * POST /requests — создать новый запрос вещи.
     */
    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestBody ItemRequestDto requestDto) {
        log.info("Creating item request from user {}", userId);
        return itemRequestService.createRequest(userId, requestDto);
    }

    /**
     * GET /requests — получить список своих запросов с ответами.
     */
    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Getting own requests for user {}", userId);
        return itemRequestService.getUserRequests(userId);
    }

    /**
     * GET /requests/all — получить запросы других пользователей.
     */
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Getting all requests except user {}", userId);
        return itemRequestService.getAllRequests(userId);
    }

    /**
     * GET /requests/{requestId} — получить один запрос c ответами.
     */
    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long requestId) {
        log.info("Getting request {} by user {}", requestId, userId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}

