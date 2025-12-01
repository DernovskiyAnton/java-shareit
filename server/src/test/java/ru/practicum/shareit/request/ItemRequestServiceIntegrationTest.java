package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemService itemService;

    private UserDto requestor;
    private UserDto owner;

    @BeforeEach
    void setUp() {
        // Создаём пользователя-запрашивающего
        UserDto requestorDto = new UserDto(null, "Requestor", "requestor@example.com");
        requestor = userService.createUser(requestorDto);

        // Создаём пользователя-владельца вещи
        UserDto ownerDto = new UserDto(null, "Owner", "owner@example.com");
        owner = userService.createUser(ownerDto);
    }

    @Test
    void createRequest_shouldCreateAndReturnRequest() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        // When
        ItemRequestDto createdRequest = itemRequestService.createRequest(requestor.id(), requestDto);

        // Then
        assertNotNull(createdRequest);
        assertNotNull(createdRequest.getId());
        assertEquals("Нужна дрель", createdRequest.getDescription());
        assertNotNull(createdRequest.getCreated());
        assertNotNull(createdRequest.getItems());
        assertTrue(createdRequest.getItems().isEmpty());
    }

    @Test
    void createRequest_withNonExistentUser_shouldThrowNotFoundException() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.createRequest(999L, requestDto);
        });
    }

    @Test
    void getUserRequests_shouldReturnRequestsWithItems() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestDto createdRequest = itemRequestService.createRequest(requestor.id(), requestDto);

        // Создаём вещь в ответ на запрос
        ItemDto itemDto = new ItemDto(
                null,
                "Дрель Bosch",
                "Мощная дрель 600W",
                true,
                createdRequest.getId()
        );
        itemService.addItem(owner.id(), itemDto);

        // When
        List<ItemRequestDto> requests = itemRequestService.getUserRequests(requestor.id());

        // Then
        assertNotNull(requests);
        assertEquals(1, requests.size());

        ItemRequestDto request = requests.get(0);
        assertEquals("Нужна дрель", request.getDescription());
        assertNotNull(request.getItems());
        assertEquals(1, request.getItems().size());

        ItemRequestDto.ItemDto item = request.getItems().get(0);
        assertEquals("Дрель Bosch", item.getName());
        assertEquals(owner.id(), item.getOwnerId());
    }

    @Test
    void getUserRequests_shouldReturnEmptyListForUserWithoutRequests() {
        // When
        List<ItemRequestDto> requests = itemRequestService.getUserRequests(owner.id());

        // Then
        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void getUserRequests_withNonExistentUser_shouldThrowNotFoundException() {
        // When & Then
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getUserRequests(999L);
        });
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        itemRequestService.createRequest(requestor.id(), requestDto);

        // When - owner запрашивает чужие запросы
        List<ItemRequestDto> requests = itemRequestService.getAllRequests(owner.id());

        // Then
        assertNotNull(requests);
        assertEquals(1, requests.size());
        assertEquals("Нужна дрель", requests.get(0).getDescription());
    }

    @Test
    void getAllRequests_shouldNotReturnOwnRequests() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        itemRequestService.createRequest(requestor.id(), requestDto);

        // When - requestor запрашивает чужие запросы
        List<ItemRequestDto> requests = itemRequestService.getAllRequests(requestor.id());

        // Then - не должны вернуться свои запросы
        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void getAllRequests_shouldReturnRequestsSortedByCreatedDesc() {
        // Given
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Первый запрос");
        ItemRequestDto created1 = itemRequestService.createRequest(requestor.id(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Второй запрос");
        ItemRequestDto created2 = itemRequestService.createRequest(requestor.id(), request2);

        // When
        List<ItemRequestDto> requests = itemRequestService.getAllRequests(owner.id());

        // Then
        assertNotNull(requests);
        assertEquals(2, requests.size());
        // Проверяем сортировку - новые сначала
        assertTrue(requests.get(0).getCreated().isAfter(requests.get(1).getCreated()) ||
                requests.get(0).getCreated().isEqual(requests.get(1).getCreated()));
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestDto createdRequest = itemRequestService.createRequest(requestor.id(), requestDto);

        // Создаём вещь в ответ на запрос
        ItemDto itemDto = new ItemDto(
                null,
                "Дрель Bosch",
                "Мощная дрель 600W",
                true,
                createdRequest.getId()
        );
        itemService.addItem(owner.id(), itemDto);

        // When
        ItemRequestDto foundRequest = itemRequestService.getRequestById(owner.id(), createdRequest.getId());

        // Then
        assertNotNull(foundRequest);
        assertEquals(createdRequest.getId(), foundRequest.getId());
        assertEquals("Нужна дрель", foundRequest.getDescription());
        assertNotNull(foundRequest.getItems());
        assertEquals(1, foundRequest.getItems().size());
    }

    @Test
    void getRequestById_withNonExistentRequest_shouldThrowNotFoundException() {
        // When & Then
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getRequestById(requestor.id(), 999L);
        });
    }

    @Test
    void getRequestById_withNonExistentUser_shouldThrowNotFoundException() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestDto createdRequest = itemRequestService.createRequest(requestor.id(), requestDto);

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getRequestById(999L, createdRequest.getId());
        });
    }

    @Test
    void getRequestById_shouldBeAccessibleByAnyUser() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestDto createdRequest = itemRequestService.createRequest(requestor.id(), requestDto);

        // When - любой другой пользователь может просмотреть запрос
        ItemRequestDto foundRequest = itemRequestService.getRequestById(owner.id(), createdRequest.getId());

        // Then
        assertNotNull(foundRequest);
        assertEquals(createdRequest.getId(), foundRequest.getId());
    }
}