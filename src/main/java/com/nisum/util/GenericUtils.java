package com.nisum.util;

import com.nisum.auth.domain.dto.ApiResponse;
import com.nisum.exception.custom.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GenericUtils {
    private GenericUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericUtils.class);

    public static void main(String[] args) {
        convertToSeconds(212731560);
    }

    public static String formatIndex(String value) {
        LOGGER.info("Converting Project to Index Name... Project: '{}'...", value);
        return value.toLowerCase().trim().replace(" ", "-");
    }

    public static float formatPercentage(float value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return Float.parseFloat(df.format(value));
    }

    public static String formatDateForELK(long date) {
        LOGGER.info("Changing date format for ELK...");
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date(date));
    }

    public static String formatDateForEmail(long date) {
        LOGGER.info("Changing date format for Email...");
        return new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z").format(new Date(date));
    }

    public static String convertToSeconds(long nanoSeconds) {
        return new DecimalFormat("0.00").format(TimeUnit.SECONDS.convert(Math.abs(nanoSeconds), TimeUnit.MICROSECONDS) / 1000.0) + " s";
    }

    public static <T> List<T> getPages(Collection<T> c, Integer page, Integer pageSize) {
        if (c == null)
            return Collections.emptyList();
        List<T> list = new ArrayList<>(c);
        int totalRecords = list.size();
        if (totalRecords > 0) {
            int numPages = getTotalPages(totalRecords, pageSize);
            if (pageSize <= 0 || page < 0) {
                LOGGER.error("Invalid Page Size: {}", pageSize);
                throw new BadRequestException(new ApiResponse(Boolean.FALSE, "Invalid Page Size: " + pageSize));
            }
            if (page >= numPages) {

                LOGGER.error("Invalid Page Size: {}", pageSize);
                throw new BadRequestException(new ApiResponse(Boolean.FALSE, "Invalid Page Size: " + pageSize));
            }

            if (pageSize == null || pageSize <= 0 || pageSize > totalRecords)
                pageSize = totalRecords;
            List<List<T>> pages = new ArrayList<>(numPages);
            for (int pageNum = 0; pageNum < numPages; )
                pages.add(list.subList(pageNum * pageSize, Math.min(++pageNum * pageSize, list.size())));
            return pages.get(page);
        }
        return list;
    }

    public static int getTotalPages(int totalRecords, int pageSize) {
        LOGGER.info("Returning Total Pages Count...");
        return (int) Math.ceil((double) totalRecords / (double) pageSize);
    }

    public static <T> Map<String, Object> getPaginatedResponse(Collection<T> data, int page, int limit, int totalRecords) {
        Map<String, Object> response = new HashMap<>();
        if (totalRecords > 0) {
            response.put("data", data);
            response.put("currentPage", page);
            response.put("limit", limit);
            response.put("totalRecords", totalRecords);
            response.put("totalPages", getTotalPages(totalRecords, limit));
        } else {
            response.put("data", new HashSet<>());
            response.put("currentPage", 0);
            response.put("limit", limit);
            response.put("totalRecords", 0L);
            response.put("totalPages", 0);
        }
        LOGGER.info("Returning Paginated Response...");
        return response;
    }
}