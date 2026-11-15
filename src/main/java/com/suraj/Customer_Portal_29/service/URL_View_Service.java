package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.URL_View_Request_Dto;
import com.suraj.Customer_Portal_29.dto.response.URL_View_Response_Dto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.URL_View;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.repository.URL_View_Repository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class URL_View_Service {

    private final URL_View_Repository repository;
    private final OwnerRepository ownerRepository;

    public URL_View_Service(URL_View_Repository repository, OwnerRepository ownerRepository) {
        this.repository = repository;
        this.ownerRepository = ownerRepository;
    }

    private Owner getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ownerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private URL_View_Response_Dto mapToResponse(URL_View entity) {
        URL_View_Response_Dto dto = new URL_View_Response_Dto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setUrl(entity.getUrl());
        dto.setSortOrder(entity.getSortOrder());
        dto.setOwnerId(entity.getOwnerId());
        dto.setGlobal(entity.getOwnerId() == null);
        return dto;
    }

    /**
     * Seed the 8 mandatory default URLs ONCE (global, owner_id = NULL).
     * Runs on app startup. Skipped if globals already exist.
     */
    @PostConstruct
    @Transactional
    public void seedDefaultsOnce() {
        if (repository.existsByOwnerIdIsNull()) return;

        String[][] defaults = {
                {"MOBILE NO AND EMAIL ID CHANGE", "https://pro.mahadiscom.in/ConsumerContact/consumer"},
                {"PM Consumer Login", "https://consumer.pmsuryaghar.gov.in/consumer/#/login"},
                {"PM Vendor Login", "https://vendor.pmsuryaghar.gov.in/vendor/#/signed-in/dashboard"},
                {"PV Application", "https://css.mahadiscom.in/UI/ROOFTOP/PVNewApplication.aspx"},
                {"RTS", "https://rts.mahadiscom.in/RoofTopSolarVendor/RoofTopSolar"},
                {"GRAS Chalan", "https://gras.mahakosh.gov.in/echallan/challan/views/frmguestechallan_entry.php"},
                {"Generation Meter Fee", "https://otheronlinepayment.mahadiscom.in/OtherReceipts/onlinePayment.jsp"},
                {"New Connection/Load Addition/Name Change", "https://wss.mahadiscom.in/wss/wss?uiActionName=getViewPayBill"}
        };

        for (int i = 0; i < defaults.length; i++) {
            URL_View e = new URL_View();
            e.setTitle(defaults[i][0]);
            e.setUrl(defaults[i][1]);
            e.setSortOrder(i);
            e.setOwnerId(null);
            repository.save(e);
        }
    }

    /**
     * Returns URLs visible to current user:
     *  - all global URLs (owner_id = NULL)
     *  - plus the user's own private URLs
     */
    public List<URL_View_Response_Dto> findAllForCurrentUser() {
        Owner current = getCurrentUser();
        return repository.findVisibleTo(current.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a URL:
     *  - SUPER_ADMIN → global (ownerId = null)
     *  - USER        → private to that user
     */
    @Transactional
    public URL_View_Response_Dto create(URL_View_Request_Dto req) {
        Owner current = getCurrentUser();

        URL_View entity = new URL_View();
        entity.setTitle(req.getTitle().trim());
        entity.setUrl(req.getUrl().trim());
        entity.setOwnerId(current.getRole() == UserRole.SUPER_ADMIN ? null : current.getId());

        if (req.getSortOrder() != null) {
            entity.setSortOrder(req.getSortOrder());
        } else {
            // append at the end of current user's list
            int next = (int) repository.findVisibleTo(current.getId()).stream()
                    .mapToInt(URL_View::getSortOrder)
                    .max().orElse(-1) + 1;
            entity.setSortOrder(next);
        }

        return mapToResponse(repository.save(entity));
    }

    /**
     * Update: only the owner (or SUPER_ADMIN for any URL) may update.
     * Global URLs are editable ONLY by SUPER_ADMIN.
     */
    @Transactional
    public URL_View_Response_Dto update(Long id, URL_View_Request_Dto req) {
        Owner current = getCurrentUser();
        URL_View entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found: " + id));

        boolean isGlobal = entity.getOwnerId() == null;
        boolean isSuperAdmin = current.getRole() == UserRole.SUPER_ADMIN;
        boolean isOwn = entity.getOwnerId() != null && entity.getOwnerId().equals(current.getId());

        if (!isSuperAdmin && !isOwn) {
            throw new RuntimeException("You are not allowed to modify this URL");
        }
        if (isGlobal && !isSuperAdmin) {
            throw new RuntimeException("Global URLs can be modified only by SUPER_ADMIN");
        }

        entity.setTitle(req.getTitle().trim());
        entity.setUrl(req.getUrl().trim());
        if (req.getSortOrder() != null) entity.setSortOrder(req.getSortOrder());

        return mapToResponse(repository.save(entity));
    }

    /**
     * Delete: same rule as update.
     */
    @Transactional
    public void delete(Long id) {
        Owner current = getCurrentUser();
        URL_View entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found: " + id));

        boolean isGlobal = entity.getOwnerId() == null;
        boolean isSuperAdmin = current.getRole() == UserRole.SUPER_ADMIN;
        boolean isOwn = entity.getOwnerId() != null && entity.getOwnerId().equals(current.getId());

        if (!isSuperAdmin && !isOwn) {
            throw new RuntimeException("You are not allowed to delete this URL");
        }
        if (isGlobal && !isSuperAdmin) {
            throw new RuntimeException("Global URLs can be deleted only by SUPER_ADMIN");
        }

        repository.delete(entity);
    }
}