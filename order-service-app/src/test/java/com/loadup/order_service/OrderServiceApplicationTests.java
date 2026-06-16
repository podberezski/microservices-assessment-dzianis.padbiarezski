package com.loadup.order_service;

import com.loadup.order.OrderServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderServiceApplication.class)
class OrderServiceApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void tablesExistInH2Database() {
		// Query H2 information schema for table names
		List<Map<String, Object>> results = jdbcTemplate.queryForList(
			"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'"
		);

		List<String> tableNames = results.stream()
			.map(row -> (String) row.get("TABLE_NAME"))
			.toList();

		// Assert all schema tables exist
		assertThat(tableNames).containsExactlyInAnyOrder("ORDERS", "ORDER_ITEMS");
	}

}
