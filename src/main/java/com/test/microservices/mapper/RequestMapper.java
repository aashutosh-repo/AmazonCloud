package com.test.microservices.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class RequestMapper {
	private String notificationId;
    private int activate;
}
