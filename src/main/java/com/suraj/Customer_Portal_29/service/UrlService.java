package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.UrlRequestDto;
import com.suraj.Customer_Portal_29.dto.response.UrlResponseDto;
import com.suraj.Customer_Portal_29.entity.UrlEntity;
import com.suraj.Customer_Portal_29.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public interface UrlService {
    List<UrlResponseDto> getUserUrls(String userId);
    UrlResponseDto addUrl(String userId, UrlRequestDto request);
    UrlResponseDto updateUrl(String userId, Long id, UrlRequestDto request);
    void deleteUrl(String userId, Long id);
}

@Service
@RequiredArgsConstructor
class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;

    private static final List<String[]> DEFAULT_URLS = List.of(

            new String[]{"PM Consumer Login", "https://consumer.pmsuryaghar.gov.in/consumer/#/login"},
            new String[]{"PM Vendor Login", "https://vendor.pmsuryaghar.gov.in/vendor/#/signed-in/dashboard"},
            new String[]{"MOBILE NO AND EMAIL ID CHANGE", "https://pro.mahadiscom.in/ConsumerContact/consumer"},
            new String[]{"PV Application", "https://css.mahadiscom.in/UI/ROOFTOP/PVNewApplication.aspx"},
            new String[]{"RTS", "https://rts.mahadiscom.in/RoofTopSolarVendor/RoofTopSolar"},
            new String[]{"GRAS Chalan", "https://gras.mahakosh.gov.in/echallan/challan/views/frmguestechallan_entry.php"},
            new String[]{"Generation Meter Fee", "https://otheronlinepayment.mahadiscom.in/OtherReceipts/onlinePayment.jsp"},
            new String[]{"New Connection/Load Addition/Name Change", "https://wss.mahadiscom.in/wss/wss?uiActionName=getViewPayBill"},
            new String[]{"DCR login", "https://dcr.mahadiscom.in/"}
    );

    @Override
    public List<UrlResponseDto> getUserUrls(String userId) {
        if (!urlRepository.existsByUserId(userId)) {
            initDefaultUrlsForUser(userId);
        }
        return urlRepository.findByUserIdOrderByIsDefaultDescIdDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UrlResponseDto addUrl(String userId, UrlRequestDto request) {
        UrlEntity entity = UrlEntity.builder()
                .title(request.getTitle().trim())
                .url(request.getUrl().trim())
                .userId(userId)
                .isDefault(false)
                .build();
        return mapToDto(urlRepository.save(entity));
    }

    @Override
    @Transactional
    public UrlResponseDto updateUrl(String userId, Long id, UrlRequestDto request) {
        UrlEntity entity = urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        entity.setTitle(request.getTitle().trim());
        entity.setUrl(request.getUrl().trim());
        return mapToDto(urlRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteUrl(String userId, Long id) {
        UrlEntity entity = urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (entity.isDefault()) {
            throw new RuntimeException("Cannot delete system default URLs");
        }
        urlRepository.delete(entity);
    }

    private void initDefaultUrlsForUser(String userId) {
        List<UrlEntity> defaults = DEFAULT_URLS.stream()
                .map(arr -> UrlEntity.builder()
                        .title(arr[0])
                        .url(arr[1])
                        .userId(userId)
                        .isDefault(true)
                        .build())
                .collect(Collectors.toList());
        urlRepository.saveAll(defaults);
    }

    private UrlResponseDto mapToDto(UrlEntity entity) {
        return UrlResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .url(entity.getUrl())
                .isDefault(entity.isDefault())
                .build();
    }
}